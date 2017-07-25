package com.avansas.imagetools.strategy;

import java.util.List;

import org.springframework.beans.factory.annotation.Required;

public class CompositeImageRenameStrategy implements ImageRenameStrategy {

	private List<ImageRenameStrategy> imageRenameStrategies;
	
	@Override
	public void renameAll(String productCode, String newName) {
		imageRenameStrategies.forEach(strategy -> strategy.renameAll(productCode, newName));
	}
	public List<ImageRenameStrategy> getImageRenameStrategies() {
		return imageRenameStrategies;
	}
	
	@Required
	public void setImageRenameStrategies(List<ImageRenameStrategy> imageRenameStrategies) {
		this.imageRenameStrategies = imageRenameStrategies;
	}

}
