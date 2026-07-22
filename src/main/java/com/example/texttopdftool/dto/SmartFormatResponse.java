package com.example.texttopdftool.dto;

public class SmartFormatResponse {

	private final String title;
	private final String content;
	private final int suggestedFontSize;

	public SmartFormatResponse(String title, String content, int suggestedFontSize) {
		this.title = title;
		this.content = content;
		this.suggestedFontSize = suggestedFontSize;
	}

	public String getTitle() {
		return title;
	}

	public String getContent() {
		return content;
	}

	public int getSuggestedFontSize() {
		return suggestedFontSize;
	}
}
