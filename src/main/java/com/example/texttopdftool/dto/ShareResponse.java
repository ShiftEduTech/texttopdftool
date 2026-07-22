package com.example.texttopdftool.dto;

import java.time.Instant;

public class ShareResponse {

	private final String id;
	private final String url;
	private final Instant expiresAt;

	public ShareResponse(String id, String url, Instant expiresAt) {
		this.id = id;
		this.url = url;
		this.expiresAt = expiresAt;
	}

	public String getId() {
		return id;
	}

	public String getUrl() {
		return url;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}
}
