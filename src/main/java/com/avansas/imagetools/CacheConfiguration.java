package com.avansas.imagetools;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.springframework.cache.CacheManager;
import org.springframework.cache.guava.GuavaCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.cache.CacheBuilder;

@Configuration
public class CacheConfiguration {

    @Bean
    public CacheManager cacheManager() {
    	SimpleCacheManager simpleCacheManager = new SimpleCacheManager();
        GuavaCache shortTerm = new GuavaCache("shortTerm", CacheBuilder.newBuilder().expireAfterWrite(60, TimeUnit.SECONDS).build());
        GuavaCache hourly = new GuavaCache("hourly", CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build());
        
        simpleCacheManager.setCaches(Arrays.asList(shortTerm, hourly));
        return simpleCacheManager;
    }

}