package com.renova.imagetools.event.listener;

import com.renova.imagetools.event.EventListener;
import com.renova.imagetools.event.FtpFailEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

@Component
@EventListener
public class LoggingFtpFailListener {
	private static final Logger LOG = LoggerFactory.getLogger(LoggingFtpFailListener.class);
	
	@Subscribe
	@AllowConcurrentEvents
	public void handle(FtpFailEvent ftpFailEvent) {
		LOG.error(ftpFailEvent.getFile().getName());
	}

}
