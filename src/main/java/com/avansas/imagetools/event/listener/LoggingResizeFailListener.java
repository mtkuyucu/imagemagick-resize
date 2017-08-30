package com.avansas.imagetools.event.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.avansas.imagetools.event.EventListener;
import com.avansas.imagetools.event.ResizeFailEvent;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

@Component
@EventListener
public class LoggingResizeFailListener {
	private static final Logger LOG = LoggerFactory.getLogger(LoggingResizeFailListener.class);
	
	@Subscribe
	@AllowConcurrentEvents
	public void handle(ResizeFailEvent resizeFailEvent) {
		LOG.error("Input:" + resizeFailEvent.getInputFile() + " | Output:" + resizeFailEvent.getInputFile());
	}
}
