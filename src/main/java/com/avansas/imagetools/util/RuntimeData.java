package com.avansas.imagetools.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Date;

import org.apache.commons.lang3.ObjectUtils;

import com.google.common.base.Throwables;
import com.thoughtworks.xstream.XStream;

public class RuntimeData {
	private static LazyInitializer<RuntimeData> instanceInit = LazyInitializer.newLazyInitializer(RuntimeData::createInstance);
	private static Long lastReloadMillis = 0L;
	private static final Long RELOAD_AFTER_WRITE_MILLIS = Long.valueOf(Parameters.getInstance().getString("runtimeData.relod.after.write.millis"));
	private static final String XML_FILE_NAME = Parameters.getInstance().getString("runtimeData.xml.file");
	private static final XStream XSTREAM = new XStream();
	private Date lastLookUpDate;
	
	private RuntimeData () {
		// Hidden constructor
	}
	
	public Date getLastLookUpDate() {
		return lastLookUpDate;
	}

	protected void setLastLookUpDate(Date lastLookUpDate) {
		this.lastLookUpDate = ObjectUtils.defaultIfNull(lastLookUpDate, new Date(0));
	}
	
	public static RuntimeData getInstance() {
		long currentTimeMillis = System.currentTimeMillis();
		if(currentTimeMillis - lastReloadMillis > RELOAD_AFTER_WRITE_MILLIS) {
			instanceInit.reset();
		}
		return instanceInit.getInstance();
	}

	public static void saveLastLookUpDate(Date lastLookUpDate) {
		RuntimeData instance = instanceInit.getInstance();
		instance.setLastLookUpDate(lastLookUpDate);
		saveInstance();
	}

	private static void saveInstance() {
		RuntimeData instance = instanceInit.getInstance();
		try {
			XSTREAM.toXML(instance, new BufferedOutputStream(new FileOutputStream(new File(XML_FILE_NAME))));
			instanceInit.reset();
		} catch (FileNotFoundException e) {
			Throwables.propagate(e);
		}
	}
	
	private static RuntimeData createInstance() {
		RuntimeData runtimeData;
		File file = new File(XML_FILE_NAME);
		if(file.exists()) {
			runtimeData = (RuntimeData) XSTREAM.fromXML(file);
		} else {
			runtimeData = new RuntimeData();
			runtimeData.setLastLookUpDate(new Date(0));
		}
		lastReloadMillis = System.currentTimeMillis();
		return runtimeData;
	}
	
}
