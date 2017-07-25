package com.avansas.imagetools.dto;

public class ConversionMediaFormatWsDTO implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	private String mimeType;
	private String conversion;
	private String inputFormat;
	private String qualifier;

	public ConversionMediaFormatWsDTO() {
		// default constructor
	}

	public void setMimeType(final String mimeType) {
		this.mimeType = mimeType;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setConversion(final String conversion) {
		this.conversion = conversion;
	}

	public String getConversion() {
		return conversion;
	}

	public void setInputFormat(final String inputFormat) {
		this.inputFormat = inputFormat;
	}

	public String getInputFormat() {
		return inputFormat;
	}

	public void setQualifier(final String qualifier) {
		this.qualifier = qualifier;
	}

	public String getQualifier() {
		return qualifier;
	}

}