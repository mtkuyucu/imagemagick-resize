package com.avansas.imagetools.strategy;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.avansas.imagetools.client.ProductImageInfoClient;
import com.avansas.imagetools.dto.ConversionGroupListWsDTO;
import com.avansas.imagetools.dto.ConversionGroupWsDTO;
import com.avansas.imagetools.dto.ConversionMediaFormatWsDTO;
import com.avansas.imagetools.util.FileNameFilter;
import com.avansas.imagetools.util.KeyBasedConcurrentPerformer;

@Component
public class DefaultImageRenameStrategy implements ImageRenameStrategy{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultImageRenameStrategy.class);
	@Value("${product.image.folder.path}")
	private String productImagePath;
	@Value("${image.pattern.placeholder.imagetype}")
	private String imageTypePlaceHolder;
	@Value("${image.pattern.placeholder.index}")
	private String indexPlaceHolder;
	@Value("${image.expected.pattern}")
	private String expectedImagePattern;
	@Autowired
	protected ProductImageInfoClient productImageInfoClient;
	@Autowired
	private KeyBasedConcurrentPerformer keyBasedConcurrentPerformer;
	
	@Override
	public void renameAll(String productCode, String namePattern) {
		System.out.println(productImagePath);
		File productImageDirectory = new File(productImagePath + File.separatorChar + productCode);
		if (!productImageDirectory.exists()) {
			throw new IllegalStateException(StringUtils
					.join("Image files folder for product {code:", productCode, "} does not exist"));
		}
		ConversionGroupListWsDTO imageConversionInfo = productImageInfoClient.getImageConversionInfo();
		String matchString = createMatchString(productCode, imageConversionInfo);
		String replacement = createReplacementText(namePattern);
		Iterator<File> fileIterator = getAllImagesForProduct(productImageDirectory, matchString);
		while(fileIterator.hasNext()) {
			File file = fileIterator.next();
			keyBasedConcurrentPerformer.run(file.getParentFile(), () -> renameFile(matchString, replacement, file));
		}
	}

	private void renameFile(String matchString, String replacement, File file) {
		String formerName = file.getName();
		try {
			String newName = formerName.replaceAll(matchString, replacement);
			Path formerFilePath = Paths.get(file.getAbsolutePath());
			Files.move(formerFilePath, formerFilePath.resolveSibling(newName), StandardCopyOption.REPLACE_EXISTING);
			LOG.info(StringUtils.join("File ", formerName, " was renamed to ", newName));
		} catch (Exception e) {
			LOG.error("Failed to rename file " + formerName, e);
		}
	}

	private Iterator<File> getAllImagesForProduct(File productImageDirectory, String matchString) {
		IOFileFilter fileFilter = new AndFileFilter(FileFileFilter.FILE, new FileNameFilter(matchString));
		return FileUtils.iterateFiles(productImageDirectory, fileFilter, FalseFileFilter.INSTANCE);
	}

	protected String createReplacementText(String namePattern) {
		String[] searchList = new String[]{indexPlaceHolder, imageTypePlaceHolder};
		String[] replacementList = new String []{"$1", "$2"};
		return StringUtils.replaceEach(namePattern, searchList, replacementList);
	}
	
	protected String createMatchString(String productCode, ConversionGroupListWsDTO conversions) {
		String formatQualifierMatcher = conversions.getConversionGroups().stream()
				.map(ConversionGroupWsDTO::getSupportedFormats)
				.flatMap(Collection::stream)
				.map(ConversionMediaFormatWsDTO::getQualifier)
				.collect(Collectors.joining("|", "(", ")"));
		Map<String, String> values = new HashMap<String, String>();
		values.put("pCode", productCode);
		values.put("index", "([0-9]+)");
		values.put("pName", ".+");
		values.put("imageType", formatQualifierMatcher);
				
		StrSubstitutor sub = new StrSubstitutor(values, "%(", ")");
		return sub.replace(expectedImagePattern);
	}
	
}
