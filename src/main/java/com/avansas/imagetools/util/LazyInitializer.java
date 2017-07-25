package com.avansas.imagetools.util;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class LazyInitializer<T> implements Serializable {
	private static final long serialVersionUID = -4540436163480846045L;
	private T instance;

	abstract protected T init();

	public T getInstance() {
		if(null == instance) {
			synchronized (this) {
				if(null == instance) {
					instance = init();
				}
			}
		}
		return instance;
	}
	
	public void reset() {
		if(Objects.nonNull(instance)) {
			synchronized (this) {
				instance = null;
			}
		}
	}
	
	public static <T> LazyInitializer<T> newLazyInitializer(final Supplier<T> supplier) {
		return new LazyInitializer<T>() {
			private static final long serialVersionUID = -382711454474584530L;
			
			protected T init() {
				return supplier.get();
			}
		};
	}
}