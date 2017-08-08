package com.avansas.imagetools.job;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

import com.avansas.imagetools.util.FileNameFilter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.avansas.imagetools.client.ProductImageInfoClient;
import com.avansas.imagetools.dto.ConversionGroupWsDTO;
import com.avansas.imagetools.dto.ConversionMediaFormatWsDTO;
import com.avansas.imagetools.strategy.ImageResizeStrategy;
import com.avansas.imagetools.strategy.ModifiedFileLookUpStrategy;
import com.avansas.imagetools.strategy.ProductImageCopyStrategy;
import com.avansas.imagetools.util.RuntimeData;
import com.google.common.base.Joiner;

@Component
public class ImageResizeJob {
	private static final Logger LOG = LoggerFactory.getLogger(ImageResizeJob.class);

	@Autowired
	private ImageResizeStrategy imageResizeStrategy;
	@Autowired
	@Qualifier("modifiedFileLookUpStrategy")
	private ModifiedFileLookUpStrategy modifiedFileDetectionStrategy;
	@Autowired
	private ProductImageInfoClient productImageInfoClient;
	private ProductImageCopyStrategy productImageCopyStrategy;
	private Map<String,String> imageTypeMap;

	@Value("${drop.foler.path}")
	private String dropFolderPath;
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
	private static Map<String,Integer> archivedImageCountEachProduct;

	public synchronized void perform() {
		archivedImageCountEachProduct = new HashMap<>();
		Date lastLookUpDate = new Date();
		File lookupImageDir = new File(dropFolderPath);
		List<File> latestModifications = modifiedFileDetectionStrategy
				.findLatestModifications(lookupImageDir, Optional.ofNullable(fileExtension));
		if(CollectionUtils.isNotEmpty(latestModifications)) {
			//Map<String, String> nameTemplatesForModifiedImages = getNameTemplatesForModifiedImages(latestModifications);
			Map<String, String> nameTemplatesForModifiedImages = new HashMap<>();
			List<ConversionMediaFormatWsDTO> supportedFormats = getSupportedFormats();
			latestModifications.stream().forEach(file ->
					createConversions(file, nameTemplatesForModifiedImages, supportedFormats));
			latestModifications.stream().forEach(this::moveFileToArchive);
			archivedImageCountEachProduct.forEach((key,value) -> {
				LOG.info("Update CDN Image -> Product:{"+key+"} count:{"+value+"}");
				updateProductCDNImageCount(key,value);
			});
		}
		RuntimeData.saveLastLookUpDate(lastLookUpDate);
	}

	private void updateProductCDNImageCount(String productCode, int imageCount) {

		productImageInfoClient.updateProductImageCount(productCode,imageCount);
	}

	private List<ConversionMediaFormatWsDTO> getSupportedFormats() {
		return productImageInfoClient.getImageConversionInfo().getConversionGroups().parallelStream()
				.map(ConversionGroupWsDTO::getSupportedFormats)
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
	}

	private void moveFileToArchive(File file) {
		try {
			String archiveDirPath = file.getParentFile().getAbsolutePath() + File.separator + archiveDirName;
			createDirectoryIfRequired(archiveDirPath);
			Path formerFilePath = Paths.get(file.getAbsolutePath());
			Path newDirectoryPath = Paths.get(archiveDirPath + File.separator + file.getName());
			Files.move(formerFilePath, newDirectoryPath, StandardCopyOption.REPLACE_EXISTING);
			LOG.info(StringUtils.join("File: {" , file.getAbsolutePath(), "} has been moved to archivie" ));
		} catch (IOException e) {
			LOG.error("Failed to move file: {" + file.getAbsolutePath() + "} to archive", e);
		}

	}

	private int getArchivedProductImageCount(String path) {
		File archiveDir = new File(path);
		int length = archiveDir.listFiles((dir, filename) -> filename.endsWith(".jpeg") || filename.endsWith(".jpg")).length;
		LOG.info("FOUND File Count { " +path+ " } : " + length);
		return length;
	}

		private Map<String, String> getNameTemplatesForModifiedImages(List<File> latestModifications) {
		Set<String> productCodes = latestModifications.parallelStream()
				.map(file -> findProductCodeForFile(file))
				.filter(Optional::isPresent).map(Optional::get)
				.collect(Collectors.toSet());
		return getNameTemplatesForProductCodes(productCodes);
	}

	private Map<String, String> getNameTemplatesForProductCodes(Collection<String> productCodes) {
		if(CollectionUtils.isNotEmpty(productCodes)) {
			try {
				return productImageInfoClient.getProductCodesWithImageNameTemplates(productCodes);
			} catch (Exception e) {
				LOG.warn("Unable to get name templates for product codes: {" + Joiner.on(",").join(productCodes)+ "}", e);
				return Collections.emptyMap();
			}
		}
		return Collections.emptyMap();
	}

	private void createConversions(File file, Map<String, String> nameTemplatesForModifiedImages
			, List<ConversionMediaFormatWsDTO> supportedFormats) {
		LOG.debug("Implementing formatting operation to image:{ "+ file.getAbsolutePath(), "}");
		try {
			String productCode = findProductCodeForFile(file).get();
			String nameTemplate = Optional.ofNullable(nameTemplatesForModifiedImages.get(productCode))
					.orElseGet(() -> getNameTemplateForProductCode(productCode));
			if(Objects.nonNull(nameTemplate)) {
				supportedFormats.parallelStream().forEach(format ->
						createConversionForFormat(file, nameTemplate, productCode, format));
				archivedImageCountEachProduct.putIfAbsent(productCode,0);
				archivedImageCountEachProduct.computeIfPresent(productCode,(p,c)-> c + 1);
				LOG.info("Image: {"+ file.getAbsolutePath() + "} has been formatted");
			} else {
				LOG.error("Name template for product: {" + productCode +"} is empty and it won't be formatted");
			}
		} catch (Exception e) {
			LOG.error("Failed to convert image " + file.getAbsolutePath(), e);
		}
	}

	private String getNameTemplateForProductCode(String productCode) {
		return getNameTemplatesForProductCodes(Collections.singleton(productCode)).get(productCode);
	}

	private File createDirectoryIfRequired(String path) {
		File directory = new File(path);
		if (! directory.exists()) {
			directory.mkdirs();
		}
		return directory;
	}

	private void createConversionForFormat(File file, String nameTemplate, String productCode, ConversionMediaFormatWsDTO format) {
		try {
			String index = file.getName().replaceAll("([0-9]+)\\." + fileExtension, "$1");
			String imageType = getImageTypeMap().get(format.getQualifier());
			String outputFileName = StringUtils.replaceEach(nameTemplate, new String[]{indexPlaceHolder, imageTypePlaceHolder}
					, new String []{index, imageType});
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

	private Optional<String> findProductCodeForFile(File file) {
		File imageDir = file.getParentFile();
		File lookupDir = new File(dropFolderPath);
		if(!imageDir.getParentFile().equals(lookupDir)) {
			LOG.warn(StringUtils.join("Directory " , imageDir.getAbsoluteFile(), " is not a product image directory"));
			return Optional.empty();
		}

		return Optional.of(imageDir.getName());
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
