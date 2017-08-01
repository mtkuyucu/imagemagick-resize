package com.avansas.imagetools.client;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.avansas.imagetools.dto.ConversionGroupListWsDTO;
import com.avansas.imagetools.dto.ProductListWsDTO;
import com.avansas.imagetools.util.RestClientUtil;

@Component
public class ProductImageInfoClient {
	
	@Value("${webservice.image.get.conversion.info.url}")
	private String conversionInfoUrl;
	@Value("${webservice.image.update.image.count.url}")
	private String updateProductImageCountUrl;
	@Value("${webservice.image.get.product.image.name.template}")
	private String productImageNameTemplatesUrl;
	
	@Cacheable("hourly")
	public ConversionGroupListWsDTO getImageConversionInfo(){
		return RestClientUtil.getRestResponse(conversionInfoUrl, ConversionGroupListWsDTO.class);
	}
	
	public Map<String, String> getProductCodesWithImageNameTemplates(Collection<String> productCodes) {
		Map<String, String> productCodeWithNameTemplateMap = new HashMap<>();
		String url = MessageFormat.format(productImageNameTemplatesUrl, productCodes.stream().collect(Collectors.joining(",")));
		ProductListWsDTO productList = RestClientUtil.getRestResponse(url, ProductListWsDTO.class);
		productList.getProducts().forEach(product -> productCodeWithNameTemplateMap.put(product.getCode(), product.getUrl()));
		return productCodeWithNameTemplateMap;
	}

	public void updateProductImageCount(String productCode, int imageCount) {
		RestClientUtil.putRestRequest(updateProductImageCountUrl,productCode,imageCount);
	}
}
