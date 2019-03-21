package com.renova.imagetools.strategy;

import com.renova.imagetools.dto.ConversionMediaFormatWsDTO;

public interface ImageResizeStrategy {

	boolean resize(String inputFile, String outputFile, ConversionMediaFormatWsDTO format);

}
