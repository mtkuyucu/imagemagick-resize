package com.renova.imagetools.strategy;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Required;

public class CompositeProductImageCopyStrategy implements ProductImageCopyStrategy{

	List<ProductImageCopyStrategy> imageCopyStrategies;
	@Override
	public void copy(File file, String productCode) {
		imageCopyStrategies.forEach(strategy -> {
			try {
				strategy.copy(file, productCode);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		
	}
	public List<ProductImageCopyStrategy> getImageCopyStrategies() {
		return imageCopyStrategies;
	}
	
	@Required
	public void setImageCopyStrategies(List<ProductImageCopyStrategy> imageCopyStrategies) {
		this.imageCopyStrategies = imageCopyStrategies;
	}

}
