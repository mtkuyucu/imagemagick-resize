package com.avansas.imagetools.strategy;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.springframework.beans.factory.annotation.Value;

import com.avansas.imagetools.util.FileNameFilter;

public class DefaultModifiedFileLookUpStrategy implements ModifiedFileLookUpStrategy{
	@Value("${exclusive.directory.name.pattern}")
	String exclusiveDirectoryNamePattern;
	
	@Override
	public List<File> findLatestModifications(File rootDir, Optional<String> fileExtension) {
		IOFileFilter fileFilter = createFileFilter(fileExtension);
		Iterator<File> iterator = FileUtils.iterateFiles(rootDir, fileFilter, createDirFilter());
		return IteratorUtils.toList(iterator);
	}

	protected IOFileFilter createDirFilter() {
		IOFileFilter dirFilter = new NotFileFilter(new FileNameFilter(exclusiveDirectoryNamePattern));
		return dirFilter;
	}

	protected IOFileFilter createFileFilter(Optional<String> fileExtension) {
		List<IOFileFilter> filters = new ArrayList<>();
		filters.add(FileFileFilter.FILE);
		filters.add(new FileNameFilter("[0-9]+\\..+"));
		fileExtension.ifPresent(extension -> filters.add(new FileNameFilter(".*\\." + extension)));
		return new AndFileFilter(filters);
	}

	
}
