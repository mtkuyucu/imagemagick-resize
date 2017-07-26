package com.avansas.imagetools.job;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
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
	
	public synchronized void perform() {
		Date lastLookUpDate = new Date();
		File lookupImageDir = new File(dropFolderPath);
		List<File> latestModifications = modifiedFileDetectionStrategy
				.findLatestModifications(lookupImageDir, Optional.ofNullable(fileExtension));
		Map<String, String> nameTemplatesForModifiedImages = getNameTemplatesForModifiedImages(latestModifications);
		if(MapUtils.isNotEmpty(nameTemplatesForModifiedImages)) {
			List<ConversionMediaFormatWsDTO> supportedFormats = getSupportedFormats();
			latestModifications.parallelStream().forEach(file -> 
					createConversions(file, nameTemplatesForModifiedImages, supportedFormats));
			latestModifications.parallelStream().forEach(this::moveFileToArchive);
		}
		RuntimeData.saveLastLookUpDate(lastLookUpDate);
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
			LOG.error("Failed to move file: " + file.getAbsolutePath(), e);
		}
		
	}
	private Map<String, String> getNameTemplatesForModifiedImages(List<File> latestModifications) {
		Set<String> productCodes = latestModifications.parallelStream()
		.map(file -> findProductCodeForFile(file))
		.filter(Optional::isPresent).map(Optional::get)
		.collect(Collectors.toSet());
		if(CollectionUtils.isNotEmpty(productCodes)) {
			return productImageInfoClient.getProductCodesWithImageNameTemplates(productCodes);
		}
		return Collections.emptyMap();
	}
	
	private void createConversions(File file, Map<String, String> nameTemplatesForModifiedImages
			, List<ConversionMediaFormatWsDTO> supportedFormats) {
		LOG.debug("Implementing formatting operation to image:{ "+ file.getAbsolutePath(), "}");
		try {
			String productCode = findProductCodeForFile(file).get();
			String nameTemplate = nameTemplatesForModifiedImages.get(productCode);
			supportedFormats.parallelStream().forEach(format -> 
					createConversionForFormat(file, nameTemplate, productCode, format));
		} catch (Exception e) {
			LOG.error("Failed to convert image " + file.getAbsolutePath());
		}
		LOG.info("Image: {"+ file.getAbsolutePath() + "} has been formatted");
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
			String imageType = format.getQualifier();
			String outputFileName = StringUtils.replaceEach(nameTemplate, new String[]{indexPlaceHolder, imageTypePlaceHolder}
					, new String []{index, imageType});
			createDirectoryIfRequired(tempImagePath);
			String outputFilePath = tempImagePath + File.separator + outputFileName;
			boolean success = imageResizeStrategy.resize(file.getAbsolutePath(), outputFilePath, format);
			if (success) {
				File outputFile = new File(outputFilePath);
				productImageCopyStrategy.copy(outputFile, productCode);
				Files.deleteIfExists(Paths.get(outputFile.getAbsolutePath()));
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
   
}
