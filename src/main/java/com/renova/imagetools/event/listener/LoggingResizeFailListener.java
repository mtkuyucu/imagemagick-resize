package com.renova.imagetools.event.listener;

import com.renova.imagetools.event.EventListener;
import com.renova.imagetools.event.ResizeFailEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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
