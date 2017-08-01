package com.avansas.imagetools.util;

import java.io.File;

import org.apache.commons.io.filefilter.AbstractFileFilter;

public class FileNameFilter extends AbstractFileFilter{
	private String pattern;

	public FileNameFilter(String pattern) {
		super();
		this.pattern = pattern;
	}

    @Override
	public boolean accept(File file) {
		return file.getName().matches(pattern);
	}

}
