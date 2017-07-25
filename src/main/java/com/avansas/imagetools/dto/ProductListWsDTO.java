package com.avansas.imagetools.dto;

import java.util.List;

public class ProductListWsDTO implements java.io.Serializable {

	private List<ProductWsDTO> products;

	public ProductListWsDTO() {
		// default constructor
	}

	public void setProducts(final List<ProductWsDTO> products) {
		this.products = products;
	}

	public List<ProductWsDTO> getProducts() {
		return products;
	}
}