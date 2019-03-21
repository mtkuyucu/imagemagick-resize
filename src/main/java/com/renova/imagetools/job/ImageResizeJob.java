package com.renova.imagetools.job;

import com.google.common.base.Joiner;
import com.renova.imagetools.client.ProductImageInfoClient;
import com.renova.imagetools.dto.ConversionGroupWsDTO;
import com.renova.imagetools.dto.ConversionMediaFormatWsDTO;
import com.renova.imagetools.exception.MediaFormatNotFoundException;
import com.renova.imagetools.strategy.ImageResizeStrategy;
import com.renova.imagetools.strategy.ModifiedFileLookUpStrategy;
import com.renova.imagetools.strategy.ProductImageCopyStrategy;
import com.renova.imagetools.util.RuntimeData;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ImageResizeJob {
    private static final Logger LOG = LoggerFactory.getLogger(ImageResizeJob.class);
    @Value("${expected.image.extension}")
    String extension;
    @Autowired
    private ImageResizeStrategy imageResizeStrategy;
    @Autowired
    @Qualifier("modifiedFileLookUpStrategy")
    private ModifiedFileLookUpStrategy modifiedFileDetectionStrategy;
    @Autowired
    private ProductImageInfoClient productImageInfoClient;
    private ProductImageCopyStrategy productImageCopyStrategy;
    private Map<String, String> imageTypeMap;

    @Value("${drop.foler.path}")
    private String dropFolderPath;
    @Value("${stores}")
    private String stores;
    @Value("${product.image.folder.path}")
    private String productImagePath;
    @Value("${temp.image.folder.path}")
    private String tempImagePath;
    @Value("${expected.image.extension}")
    private String fileExtension;
    @Value("${image.pattern.placeholder.imagetype}")
    private String imageTypePlaceHolder;
    @Value("${image.pattern.placeholder.index}")
    private String indexPlaceHolder;
    @Value("${archive.directory.name}")
    private String archiveDirName;
    private static Set<String> archivedImagePath;

    public synchronized void perform() throws Exception {
        archivedImagePath = new HashSet<>();
        Date lastLookUpDate = new Date();
//        if (StringUtils.isNoneBlank(stores)) {
//            for (String store : stores.split(",")) {
//                proceed("/" + store);
//            }
//        } else {
//            throw new Exception("Basestore must be setted initial");
//        }
        proceed("/");
        RuntimeData.saveLastLookUpDate(lastLookUpDate);
    }

    private void proceed(String store) throws MediaFormatNotFoundException {
        File lookupImageDir = new File(dropFolderPath + store);
        List<File> latestModifications = modifiedFileDetectionStrategy
                .findLatestModifications(lookupImageDir, Optional.ofNullable(fileExtension));
        LOG.info("Found " + latestModifications.size() + " files");
        if (CollectionUtils.isNotEmpty(latestModifications)) {
            Map<String, String> nameTemplatesForModifiedImages = new HashMap<>();
            List<ConversionMediaFormatWsDTO> supportedFormats = getSupportedFormats(store);
            if (CollectionUtils.isEmpty(supportedFormats)) {
                throw new MediaFormatNotFoundException("Supported Media Format not found store : " + store);
            }
            latestModifications.forEach(file ->
                    createConversions(file, nameTemplatesForModifiedImages, supportedFormats, store));
            latestModifications.forEach(this::moveFileToArchive);
            archivedImagePath.forEach(p -> this.getArchivedProductImageCount(p, store));
        }
    }

    private List<ConversionMediaFormatWsDTO> getSupportedFormats(String storePath) {
        return productImageInfoClient.getImageConversionInfo(storePath).getConversionGroups().parallelStream()
                .map(ConversionGroupWsDTO::getSupportedFormats)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private void moveFileToArchive(File file) {
        try {
            String archiveDirPath = file.getParentFile().getAbsolutePath() + File.separator + archiveDirName;
            archivedImagePath.add(archiveDirPath);
            createDirectoryIfRequired(archiveDirPath);
            Path formerFilePath = Paths.get(file.getAbsolutePath());
            Path newDirectoryPath = Paths.get(archiveDirPath + File.separator + file.getName());
            Files.move(formerFilePath, newDirectoryPath, StandardCopyOption.REPLACE_EXISTING);
            LOG.info(StringUtils.join("File: {", file.getAbsolutePath(), "} has been moved to archivie"));
        } catch (IOException e) {
            LOG.error("Failed to move file: {" + file.getAbsolutePath() + "} to archive", e);
        }
    }

    private Map<String, Integer> getArchivedProductImageCount(String path, String store) {
        File archiveDir = new File(path);
        int length = archiveDir.listFiles((dir, filename) -> filename.endsWith("." + extension)).length;
        final HashMap<String, Integer> tupple = new HashMap<>();
        final String productCode = archiveDir.getParentFile().getName();
        tupple.put(productCode, Integer.valueOf(length));
        productImageInfoClient.updateProductImageCount(store, productCode, length);
        LOG.info("Update CDN Image -> Product:{" + productCode + "} count:{" + length + "}");
        return tupple;
    }

    private Map<String, String> getNameTemplatesForProductCodes(Collection<String> productCodes, final String store) {
        if (CollectionUtils.isNotEmpty(productCodes)) {
            try {
                return productImageInfoClient.getProductCodesWithImageNameTemplates(productCodes, store);
            } catch (Exception e) {
                LOG.warn("Unable to get name templates for product codes: {" + Joiner.on(",").join(productCodes) + "}", e);
                return Collections.emptyMap();
            }
        }
        return Collections.emptyMap();
    }

    private void createConversions(File file, Map<String, String> nameTemplatesForModifiedImages
            , List<ConversionMediaFormatWsDTO> supportedFormats, String store) {
        LOG.debug("Implementing formatting operation to image:{ " + file.getAbsolutePath(), "}");
        try {
            String productCode = findProductCodeForFile(file, store).get();
            String nameTemplate = Optional.ofNullable(nameTemplatesForModifiedImages.get(productCode))
                    .orElseGet(() -> getNameTemplateForProductCode(productCode, store));
            if (Objects.nonNull(nameTemplate)) {
                supportedFormats.stream().forEach(format ->
                        createConversionForFormat(file, nameTemplate, productCode, format));
                LOG.info("Image: {" + file.getAbsolutePath() + "} has been formatted");
            } else {
                LOG.error("Name template for product: {" + productCode + "} is empty and it won't be formatted");
            }
        } catch (Exception e) {
            LOG.error("Failed to convert image " + file.getAbsolutePath(), e);
        }
    }

    private String getNameTemplateForProductCode(String productCode, final String store) {
        return getNameTemplatesForProductCodes(Collections.singleton(productCode), store).get(productCode);
    }

    private File createDirectoryIfRequired(String path) {
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return directory;
    }

    private void createConversionForFormat(File file, String nameTemplate, String productCode, ConversionMediaFormatWsDTO format) {
        try {
            String index = file.getName().replaceAll("([0-9]+)\\." + fileExtension, "$1");
            String imageType = Optional.ofNullable(format.getName()).filter(StringUtils::isNotBlank).orElse(getImageTypeMap().get(format.getQualifier()));
            String outputFileName = StringUtils.replaceEach(nameTemplate, new String[]{indexPlaceHolder, imageTypePlaceHolder}
                    , new String[]{index, imageType});
            createDirectoryIfRequired(tempImagePath);
            String outputFilePath = tempImagePath + File.separator + outputFileName;
            boolean success = imageResizeStrategy.resize(file.getAbsolutePath(), outputFilePath, format);
            if (success) {
                File outputFile = new File(outputFilePath);
                productImageCopyStrategy.copy(outputFile, productCode);
                //Files.deleteIfExists(Paths.get(outputFile.getAbsolutePath()));
            }
        } catch (Exception e) {
            LOG.error("Failed to format image: {" + file.getAbsolutePath() + "} for image type: {" + format.getQualifier() + "}", e);
        }
    }

    private Optional<String> findProductCodeForFile(File file, final String store) {
        final String productCode = file.getName().split("_")[0];
        LOG.info("File Name  : {} - {}", imageDir.getName(), imageDir.getAbsolutePath());
        return Optional.of(productCode);
    }

    public ProductImageCopyStrategy getProductImageCopyStrategy() {
        return productImageCopyStrategy;
    }

    @Required
    public void setProductImageCopyStrategy(ProductImageCopyStrategy productImageCopyStrategy) {
        this.productImageCopyStrategy = productImageCopyStrategy;
    }

    public Map<String, String> getImageTypeMap() {
        return imageTypeMap;
    }

    public void setImageTypeMap(Map<String, String> imageTypeMap) {
        this.imageTypeMap = imageTypeMap;
    }
}
