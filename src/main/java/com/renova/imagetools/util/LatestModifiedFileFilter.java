package com.renova.imagetools.util;

import java.io.File;
import java.util.Date;

import org.apache.commons.io.filefilter.AbstractFileFilter;

public class LatestModifiedFileFilter extends AbstractFileFilter{
	private Date lastLookupDate;
	
	
	public LatestModifiedFileFilter(Date lastLookupDate) {
		super();
		this.lastLookupDate = lastLookupDate;
	}


	@Override
	public boolean accept(File file) {
		return file.lastModified() > lastLookupDate.getTime(); 
	}

}
