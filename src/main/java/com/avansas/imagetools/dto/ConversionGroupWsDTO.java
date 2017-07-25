package com.avansas.imagetools.dto;

import java.util.List;

public class ConversionGroupWsDTO implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	private List<ConversionMediaFormatWsDTO> supportedFormats;
	private String code;

	public ConversionGroupWsDTO() {
		// default constructor
	}

	public void setSupportedFormats(final List<ConversionMediaFormatWsDTO> supportedFormats) {
		this.supportedFormats = supportedFormats;
	}

	public List<ConversionMediaFormatWsDTO> getSupportedFormats() {
		return supportedFormats;
	}

	public void setCode(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

}