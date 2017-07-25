package com.avansas.imagetools.strategy;

import java.io.File;
import java.util.List;
import java.util.Optional;

public interface ModifiedFileLookUpStrategy {

	List<File> findLatestModifications(File rootDir, Optional<String> fileExtension);

}
