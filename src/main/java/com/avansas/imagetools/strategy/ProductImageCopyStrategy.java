package com.avansas.imagetools.strategy;

import java.io.File;

public interface ProductImageCopyStrategy {

	void copy(File file, String productCode);

}
