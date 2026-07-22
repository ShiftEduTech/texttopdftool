package com.example.texttopdftool.service;

import com.example.texttopdftool.dto.JobStatusResponse;
import com.example.texttopdftool.dto.PdfRequest;
import com.example.texttopdftool.model.PdfJob;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class JobService {

	private final Map<String, PdfJob> jobs = new ConcurrentHashMap<>();
	private final ExecutorService executor = Executors.newFixedThreadPool(2);
	private final PdfService pdfService;

	public JobService(PdfService pdfService) {
		this.pdfService = pdfService;
	}

	public JobStatusResponse enqueue(PdfRequest request) {
		cleanup();
		String id = UUID.randomUUID().toString();
		PdfJob job = new PdfJob(id);
		jobs.put(id, job);
		executor.submit(() -> run(job, request));
		return toStatus(job);
	}

	public JobStatusResponse status(String id) {
		return toStatus(require(id));
	}

	public byte[] result(String id) {
		PdfJob job = require(id);
		if (job.getStatus() != PdfJob.Status.DONE || job.getPdfBytes() == null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Job is not complete");
		}
		return job.getPdfBytes();
	}

	private void run(PdfJob job, PdfRequest request) {
		job.setStatus(PdfJob.Status.RUNNING);
		job.setMessage("Generating PDF");
		try {
			byte[] pdf = pdfService.generate(request, progress -> {
				job.setProgress(progress);
				job.setMessage("Generating PDF (" + progress + "%)");
			});
			job.setPdfBytes(pdf);
			job.setProgress(100);
			job.setStatus(PdfJob.Status.DONE);
			job.setMessage("Done");
		} catch (Exception ex) {
			job.setStatus(PdfJob.Status.FAILED);
			job.setError(ex.getMessage() == null ? "Generation failed" : ex.getMessage());
			job.setMessage(job.getError());
		}
	}

	private PdfJob require(String id) {
		PdfJob job = jobs.get(id);
		if (job == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found");
		}
		return job;
	}

	private JobStatusResponse toStatus(PdfJob job) {
		return new JobStatusResponse(job.getId(), job.getStatus().name(), job.getProgress(),
				job.getStatus() == PdfJob.Status.FAILED ? job.getError() : job.getMessage());
	}

	private void cleanup() {
		Instant cutoff = Instant.now().minusSeconds(3600);
		jobs.entrySet().removeIf(entry -> entry.getValue().getCreatedAt().isBefore(cutoff));
	}
}
