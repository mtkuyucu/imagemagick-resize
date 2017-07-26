package com.avansas.imagetools;

import java.io.File;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StartUpInit {

	@Value("${drop.foler.path}")
	private String imageDropFolderPath;
	@Value("${product.image.folder.path}")
	private String productImageFolderPath;
	
	@PostConstruct
	public void init() {
		makeMissingDirectories();
	}

	private void makeMissingDirectories() {
		File imageDropFolder = new File(imageDropFolderPath);
		if(!imageDropFolder.exists()) {
			imageDropFolder.mkdirs();
		}
		
		File productImageFolder = new File(productImageFolderPath);
		if(!productImageFolder.exists()) {
			productImageFolder.mkdirs();
		}
	}
}
