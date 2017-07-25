package com.avansas.imagetools.util;

import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import com.google.common.util.concurrent.Striped;

@Component
public class DefaultKeyBasedConcurrentPerformer implements KeyBasedConcurrentPerformer {
	private Striped<Lock> locks = Striped.lazyWeakLock(32);
	@Override
	public void run(Object key, Runnable runnable) {
		Lock lock = locks.get(key);
		lock.lock();
		try {
			runnable.run();
		} finally {
			lock.unlock();
		}
	}
	@Override
	public <T> T supply(Object key, Supplier<T> supplier) {
		Lock lock = locks.get(key);
		lock.lock();
		T t;
		try {
			t = supplier.get();
		} finally {
			lock.unlock();
		}
		
		return t;
	}
}
