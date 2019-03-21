package com.renova.imagetools.util;

import java.util.ResourceBundle;

public final class Parameters {
	
	private Parameters() {}
	private ResourceBundle bundle = ResourceBundle.getBundle("parameters");
	private static final Parameters instance = new Parameters();
	
	public String getString(String key) {
		return bundle.getString(key);
	}
	
	public static Parameters getInstance() {
		return instance;
	}
}
