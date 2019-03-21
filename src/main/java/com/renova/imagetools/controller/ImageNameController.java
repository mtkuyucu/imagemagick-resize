package com.renova.imagetools.controller;

import com.renova.imagetools.strategy.ImageRenameStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class ImageNameController {

    private ImageRenameStrategy imageRenameStrategy;
    @Autowired
    private CacheManager cacheManager;

    @RequestMapping(path = "/{store}/product/{productCode:.*}/image/name"
            , method = {RequestMethod.PUT, RequestMethod.PATCH}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public boolean updateImageName(@RequestBody Map<String, Object> parameters, @PathVariable("productCode") String productCode, @PathVariable String store) {
        String pattern = (String) parameters.get("template");
        new Thread(() -> imageRenameStrategy.renameAll(store,productCode, pattern)).start();
        return true;
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

    public ImageRenameStrategy getImageRenameStrategy() {
        return imageRenameStrategy;
    }

    @Required
    public void setImageRenameStrategy(ImageRenameStrategy imageRenameStrategy) {
        this.imageRenameStrategy = imageRenameStrategy;
    }
}
