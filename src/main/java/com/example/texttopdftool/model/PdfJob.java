package com.example.texttopdftool.model;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

public class PdfJob {

	public enum Status {
		QUEUED, RUNNING, DONE, FAILED
	}

	private final String id;
	private volatile Status status = Status.QUEUED;
	private final AtomicInteger progress = new AtomicInteger(0);
	private volatile String message = "Queued";
	private volatile byte[] pdfBytes;
	private volatile String error;
	private final Instant createdAt = Instant.now();

	public PdfJob(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public int getProgress() {
		return progress.get();
	}

	public void setProgress(int value) {
		progress.set(Math.max(0, Math.min(100, value)));
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public byte[] getPdfBytes() {
		return pdfBytes;
	}

	public void setPdfBytes(byte[] pdfBytes) {
		this.pdfBytes = pdfBytes;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
