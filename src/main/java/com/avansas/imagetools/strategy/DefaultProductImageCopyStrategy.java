package com.avansas.imagetools.strategy;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.google.common.base.Joiner;

public class DefaultProductImageCopyStrategy implements ProductImageCopyStrategy {
	private static final Logger LOG = LoggerFactory.getLogger(DefaultProductImageCopyStrategy.class);
	
	@Value("${product.image.folder.path}")
	private String productImagePath;
	
	@Override
	public void copy(File file, String productCode) {
		try {
			String productDir = Joiner.on(File.separator).join(productImagePath, productCode);
			createDirectoryIfRequired(productDir);
			String newPath = Joiner.on(File.separator).join(productDir, file.getName());
			Files.copy(Paths.get(file.getAbsolutePath()), Paths.get(newPath), StandardCopyOption.REPLACE_EXISTING);
			LOG.debug("File: " + file.getName() + " was moved to location: " + newPath);
		} catch (Exception e) {
			LOG.error("Failed to move file: " + file.getName(), e);
		}
	}
	
	private File createDirectoryIfRequired(String path) {
		File directory = new File(path);
		if (! directory.exists()) {
			directory.mkdirs();
		}
		return directory;
	}

}
