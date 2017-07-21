package com.avansas.image.resize.imageresize;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ImageresizeApplication {

	public static void main(String[] args) {
		SpringApplication.run(ImageresizeApplication.class, args);
	}
}
