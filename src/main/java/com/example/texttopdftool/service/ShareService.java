package com.example.texttopdftool.service;

import com.example.texttopdftool.dto.ShareResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ShareService {

	private final Map<String, SharedPdf> store = new ConcurrentHashMap<>();
	private final long ttlMinutes;
	private final int maxEntries;

	public ShareService(
			@Value("${app.share.ttl-minutes:60}") long ttlMinutes,
			@Value("${app.share.max-entries:200}") int maxEntries) {
		this.ttlMinutes = ttlMinutes;
		this.maxEntries = maxEntries;
	}

	public ShareResponse create(byte[] pdfBytes) {
		cleanup();
		if (store.size() >= maxEntries) {
			throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Share store is full, try again later");
		}
		if (pdfBytes == null || pdfBytes.length == 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PDF content is required");
		}
		if (pdfBytes.length > 8_000_000) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PDF exceeds share size limit");
		}
		String id = UUID.randomUUID().toString().replace("-", "");
		Instant expiresAt = Instant.now().plusSeconds(ttlMinutes * 60);
		store.put(id, new SharedPdf(pdfBytes, expiresAt));
		return new ShareResponse(id, "/api/share/" + id, expiresAt);
	}

	public byte[] get(String id) {
		cleanup();
		SharedPdf shared = store.get(id);
		if (shared == null || shared.expiresAt().isBefore(Instant.now())) {
			store.remove(id);
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Share link expired or not found");
		}
		return shared.bytes();
	}

	private void cleanup() {
		Instant now = Instant.now();
		Iterator<Map.Entry<String, SharedPdf>> it = store.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, SharedPdf> entry = it.next();
			if (entry.getValue().expiresAt().isBefore(now)) {
				it.remove();
			}
		}
	}

	private record SharedPdf(byte[] bytes, Instant expiresAt) {
	}
}
