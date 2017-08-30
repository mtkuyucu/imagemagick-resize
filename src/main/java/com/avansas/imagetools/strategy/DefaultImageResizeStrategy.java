package com.avansas.imagetools.strategy;

import java.io.IOException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.avansas.imagetools.dto.ConversionMediaFormatWsDTO;
import com.avansas.imagetools.event.ResizeFailEvent;
import com.google.common.base.Joiner;
import com.google.common.eventbus.EventBus;

@Component
public class DefaultImageResizeStrategy implements ImageResizeStrategy {
	private static final Logger LOG = LoggerFactory.getLogger(DefaultImageResizeStrategy.class);
	@Autowired
	private EventBus eventBus;
	
	@Autowired
	private Environment env;
	
	@Override
	public boolean resize(String inputFile, String outputFile, ConversionMediaFormatWsDTO format) {
		String conversion = format.getConversion();
		boolean success = false;
		try {
			String imageMagickExecutable = env.getProperty("imageMagick.executable.path");
			int exitValue = execute(imageMagickExecutable, inputFile, outputFile, conversion);
			success = exitValue == 0;
		} catch (Exception e) {
			LOG.error(StringUtils.join("Failed to implement command ",conversion , " for  image: {", inputFile, "}"), e );
		}
		
		if(!success) {
			eventBus.post(new ResizeFailEvent(inputFile, outputFile));
		}
		
		return success;
	}

	private int execute(String imageMagickExecutable, String inputFile, String outputFile, String command)
			throws IOException {
		String commandText = Joiner.on(' ').join(imageMagickExecutable, "\"", inputFile, "\"", command, "\"", outputFile, "\"");
		CommandLine commandLine = CommandLine.parse(commandText);
		DefaultExecutor executor = new DefaultExecutor();
		int exitValue = executor.execute(commandLine);
		LOG.debug(StringUtils.join("Command: {", commandText, "} has been executed with result: {", exitValue, "}"));
		return exitValue;
	}
}
