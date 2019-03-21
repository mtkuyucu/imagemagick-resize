package com.renova.imagetools;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.resource.GzipResourceResolver;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.util.concurrent.TimeUnit;

/**
 * @author Mehmet Tayyar Kuyucu - (bluenesman)
 * @on 26/09/2017 - 12:57
 */
@Configuration
public class ResourceConfig extends WebMvcConfigurerAdapter {

    @Value("${product.image.folder.path}")
    private String imageResourcePath;
    @Value("${product.video.folder.path}")
    private String videoResourcePath;
    @Value("${product.image.url}")
    private String imageMediaUrl;
    @Value("${product.video.url}")
    private String videoMediaUrl;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(imageMediaUrl + "**")
                .addResourceLocations("file:" + imageResourcePath)
                .setCacheControl(CacheControl.maxAge(3600, TimeUnit.MICROSECONDS))
                .resourceChain(true)
                .addResolver(new GzipResourceResolver())
                .addResolver(new PathResourceResolver());

        registry.addResourceHandler(videoMediaUrl + "**")
                .addResourceLocations("file:" + videoResourcePath)
                .setCacheControl(CacheControl.maxAge(3600, TimeUnit.MICROSECONDS))
                .resourceChain(true)
                .addResolver(new GzipResourceResolver())
                .addResolver(new PathResourceResolver());
        super.addResourceHandlers(registry);
    }
}
