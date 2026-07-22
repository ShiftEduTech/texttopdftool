package com.example.texttopdftool.dto;

public class JobStatusResponse {

	private final String jobId;
	private final String status;
	private final int progress;
	private final String message;

	public JobStatusResponse(String jobId, String status, int progress, String message) {
		this.jobId = jobId;
		this.status = status;
		this.progress = progress;
		this.message = message;
	}

	public String getJobId() {
		return jobId;
	}

	public String getStatus() {
		return status;
	}

	public int getProgress() {
		return progress;
	}

	public String getMessage() {
		return message;
	}
}
