package com.example.texttopdftool.dto;

public class AiAssistResponse {

	private final String action;
	private final String content;
	private final String mode;
	private final String suggestedTitle;
	private final Integer suggestedFontSize;

	public AiAssistResponse(String action, String content, String mode, String suggestedTitle,
			Integer suggestedFontSize) {
		this.action = action;
		this.content = content;
		this.mode = mode;
		this.suggestedTitle = suggestedTitle;
		this.suggestedFontSize = suggestedFontSize;
	}

	public String getAction() {
		return action;
	}

	public String getContent() {
		return content;
	}

	public String getMode() {
		return mode;
	}

	public String getSuggestedTitle() {
		return suggestedTitle;
	}

	public Integer getSuggestedFontSize() {
		return suggestedFontSize;
	}
}
