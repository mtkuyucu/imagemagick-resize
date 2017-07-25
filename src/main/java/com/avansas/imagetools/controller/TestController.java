package com.avansas.imagetools.controller;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

	@RequestMapping(path = "test"
			,produces= MediaType.APPLICATION_JSON_VALUE
			, method = {RequestMethod.POST}, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public boolean test(MultiValueMap parameters) {
		return true;
	}
}
