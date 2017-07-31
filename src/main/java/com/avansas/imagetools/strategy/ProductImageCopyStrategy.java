package com.avansas.imagetools.strategy;

import java.io.File;
import java.io.IOException;

public interface ProductImageCopyStrategy {

	void copy(File file, String productCode) throws IOException;

}
