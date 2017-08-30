package com.avansas.imagetools.event;

public class ResizeFailEvent {

	private String inputFile;
	private String outputFile;
	
	public ResizeFailEvent(String inputFile, String outputFile) {
		super();
		this.inputFile = inputFile;
		this.outputFile = outputFile;
	}

	public String getInputFile() {
		return inputFile;
	}

	public String getOutputFile() {
		return outputFile;
	}
}

