package com.avansas.imagetools.event.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.avansas.imagetools.event.EventListener;
import com.avansas.imagetools.event.FtpFailEvent;
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
