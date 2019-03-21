package com.renova.imagetools.client;

import com.renova.imagetools.dto.ConversionGroupListWsDTO;
import com.renova.imagetools.dto.ProductListWsDTO;
import com.renova.imagetools.util.RestClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ProductImageInfoClient {
    private final static Logger LOG = LoggerFactory.getLogger(ProductImageInfoClient.class);
    @Value("${webservice.image.get.conversion.info.url}")
    private String conversionInfoUrl;
    @Value("${webservice.image.update.image.count.url}")
    private String updateProductImageCountUrl;
    @Value("${webservice.image.get.product.image.name.template}")
    private String productImageNameTemplatesUrl;

    @Cacheable("hourly")
    public ConversionGroupListWsDTO getImageConversionInfo(String store) {
        return RestClientUtil.getRestResponse(MessageFormat.format(conversionInfoUrl, store), ConversionGroupListWsDTO.class);
    }

    public Map<String, String> getProductCodesWithImageNameTemplates(Collection<String> productCodes, final String store) {
        LOG.info("ProdutCodes Count : {}", productCodes.size());
        Map<String, String> productCodeWithNameTemplateMap = new HashMap<>();
        String url = MessageFormat.format(productImageNameTemplatesUrl, store, productCodes.stream().collect(Collectors.joining(",")));
        LOG.info("Base Template URL : {}", MessageFormat.format(productImageNameTemplatesUrl, store));
        LOG.info("Name Template URL : {}", url);
        ProductListWsDTO productList = RestClientUtil.getRestResponse(url, ProductListWsDTO.class);
        productList.getProducts().forEach(product -> productCodeWithNameTemplateMap.put(product.getCode(), product.getUrl()));
        return productCodeWithNameTemplateMap;
    }

    public void updateProductImageCount(final String store, String productCode, int imageCount) {
        RestClientUtil.putRestRequest(MessageFormat.format(updateProductImageCountUrl, store), productCode, imageCount);
    }
}
