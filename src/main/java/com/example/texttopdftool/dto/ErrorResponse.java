package com.example.texttopdftool.dto;

import java.time.Instant;

public class ErrorResponse {

	private final String error;
	private final String message;
	private final Instant timestamp = Instant.now();

	public ErrorResponse(String error, String message) {
		this.error = error;
		this.message = message;
	}

	public String getError() {
		return error;
	}

	public String getMessage() {
		return message;
	}

	public Instant getTimestamp() {
		return timestamp;
	}
}
