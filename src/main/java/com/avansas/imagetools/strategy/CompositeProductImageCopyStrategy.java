package com.avansas.imagetools.strategy;

import java.io.File;
import java.util.List;

import org.springframework.beans.factory.annotation.Required;

public class CompositeProductImageCopyStrategy implements ProductImageCopyStrategy{

	List<ProductImageCopyStrategy> imageCopyStrategies;
	@Override
	public void copy(File file, String productCode) {
		imageCopyStrategies.forEach(strategy -> strategy.copy(file, productCode));
		
	}
	public List<ProductImageCopyStrategy> getImageCopyStrategies() {
		return imageCopyStrategies;
	}
	
	@Required
	public void setImageCopyStrategies(List<ProductImageCopyStrategy> imageCopyStrategies) {
		this.imageCopyStrategies = imageCopyStrategies;
	}

}
