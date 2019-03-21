package com.renova.imagetools;

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
	@Value("${product.video.folder.path}")
	private String videoImageFolderPath;
	
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
		File productImageVideoFolder = new File(videoImageFolderPath);
		if(!productImageVideoFolder.exists()) {
			productImageVideoFolder.mkdirs();
		}
	}
}
