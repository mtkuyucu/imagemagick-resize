package com.avansas.imagetools.dto;

import java.util.List;

public class ConversionGroupListWsDTO implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	private List<ConversionGroupWsDTO> conversionGroups;

	public ConversionGroupListWsDTO() {
		// default constructor
	}

	public void setConversionGroups(final List<ConversionGroupWsDTO> conversionGroups) {
		this.conversionGroups = conversionGroups;
	}

	public List<ConversionGroupWsDTO> getConversionGroups() {
		return conversionGroups;
	}

}