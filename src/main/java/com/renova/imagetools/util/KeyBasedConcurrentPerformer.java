package com.renova.imagetools.util;

import java.util.function.Supplier;

public interface KeyBasedConcurrentPerformer {

	<T> T supply(Object key, Supplier<T> supplier);

	void run(Object key, Runnable runnable);

}
