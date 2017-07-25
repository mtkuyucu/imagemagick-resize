package com.avansas.imagetools.strategy;

import com.avansas.imagetools.dto.ConversionMediaFormatWsDTO;

public interface ImageResizeStrategy {

	boolean resize(String inputFile, String outputFile, ConversionMediaFormatWsDTO format);

}
