package com.renova.imagetools.dto;

public class ProductWsDTO implements java.io.Serializable {

	private static final long serialVersionUID = -4837417267608889661L;

	private String code;

	private String url;

	public ProductWsDTO() {
		// default constructor
	}

	public void setCode(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public void setUrl(final String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

}
