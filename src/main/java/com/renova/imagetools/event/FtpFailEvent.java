package com.renova.imagetools.event;

import java.io.File;

public class FtpFailEvent {

	private File file;
	
	public FtpFailEvent(File file) {
		this.file = file;
	}

	public File getFile() {
		return file;
	}

}

