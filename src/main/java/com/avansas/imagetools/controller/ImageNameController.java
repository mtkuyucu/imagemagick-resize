package com.avansas.imagetools.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.avansas.imagetools.strategy.ImageRenameStrategy;

@RestController
public class ImageNameController {
	
	private ImageRenameStrategy imageRenameStrategy;

	@Autowired
	private CacheManager cacheManager;

	@RequestMapping(path = "product/{productCode:.*}/image/name"
			, method = {RequestMethod.PUT, RequestMethod.PATCH}, consumes = MediaType.APPLICATION_JSON_VALUE)
	public boolean updateImageName(@RequestBody Map<String, Object> parameters, @PathVariable("productCode") String productCode) {
		String pattern = (String) parameters.get("template");
		new Thread(() -> imageRenameStrategy.renameAll(productCode, pattern)).start();
		return true;
	}

	public ImageRenameStrategy getImageRenameStrategy() {
		return imageRenameStrategy;
	}
	@Required
	public void setImageRenameStrategy(ImageRenameStrategy imageRenameStrategy) {
		this.imageRenameStrategy = imageRenameStrategy;
	}

	@RequestMapping(path = "clear-cache"
			, method = {RequestMethod.POST}, consumes = MediaType.APPLICATION_JSON_VALUE)
	public boolean clearCache() {
		cacheManager.getCacheNames()
				.stream()
				.map(cacheManager::getCache)
				.forEach(Cache::clear);
		return true;
	}
}
