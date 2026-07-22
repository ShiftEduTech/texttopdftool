package com.example.texttopdftool.controller;

import com.example.texttopdftool.dto.JobStatusResponse;
import com.example.texttopdftool.dto.PdfRequest;
import com.example.texttopdftool.dto.ShareResponse;
import com.example.texttopdftool.service.JobService;
import com.example.texttopdftool.service.PdfService;
import com.example.texttopdftool.service.ShareService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PdfController {

	private final PdfService pdfService;
	private final PdfRequestFactory requestFactory;
	private final JobService jobService;
	private final ShareService shareService;

	public PdfController(
			PdfService pdfService,
			PdfRequestFactory requestFactory,
			JobService jobService,
			ShareService shareService) {
		this.pdfService = pdfService;
		this.requestFactory = requestFactory;
		this.jobService = jobService;
		this.shareService = shareService;
	}

	@PostMapping(value = "/generate", consumes = {
			MediaType.APPLICATION_FORM_URLENCODED_VALUE,
			MediaType.MULTIPART_FORM_DATA_VALUE
	})
	public ResponseEntity<byte[]> generateForm(HttpServletRequest httpRequest) throws Exception {
		PdfRequest request = requestFactory.fromRequest(httpRequest);
		return pdfResponse(pdfService.generate(request), request.getFileName());
	}

	@PostMapping(value = "/generate", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<byte[]> generateJson(@RequestBody PdfRequest request) throws Exception {
		return pdfResponse(pdfService.generate(request), request.getFileName());
	}

	@PostMapping(value = "/api/generate/async", consumes = {
			MediaType.APPLICATION_FORM_URLENCODED_VALUE,
			MediaType.MULTIPART_FORM_DATA_VALUE
	})
	public JobStatusResponse generateAsync(HttpServletRequest httpRequest) {
		return jobService.enqueue(requestFactory.fromRequest(httpRequest));
	}

	@PostMapping(value = "/api/generate/async", consumes = MediaType.APPLICATION_JSON_VALUE)
	public JobStatusResponse generateAsyncJson(@RequestBody PdfRequest request) {
		return jobService.enqueue(request);
	}

	@GetMapping("/api/generate/status/{jobId}")
	public JobStatusResponse jobStatus(@PathVariable String jobId) {
		return jobService.status(jobId);
	}

	@GetMapping("/api/generate/result/{jobId}")
	public ResponseEntity<byte[]> jobResult(@PathVariable String jobId) {
		return pdfResponse(jobService.result(jobId), null);
	}

	@PostMapping(value = "/api/share", consumes = {
			MediaType.APPLICATION_FORM_URLENCODED_VALUE,
			MediaType.MULTIPART_FORM_DATA_VALUE
	})
	public ShareResponse share(HttpServletRequest httpRequest) throws Exception {
		PdfRequest request = requestFactory.fromRequest(httpRequest);
		return shareService.create(pdfService.generate(request));
	}

	@PostMapping(value = "/api/share", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ShareResponse shareJson(@RequestBody PdfRequest request) throws Exception {
		return shareService.create(pdfService.generate(request));
	}

	@GetMapping("/api/share/{id}")
	public ResponseEntity<byte[]> getShare(@PathVariable String id) {
		return pdfResponse(shareService.get(id), null);
	}

	private ResponseEntity<byte[]> pdfResponse(byte[] pdfBytes, String fileName) {
		String safeName = sanitizeFileName(fileName);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + safeName + "\"")
				.contentType(MediaType.APPLICATION_PDF)
				.contentLength(pdfBytes.length)
				.body(pdfBytes);
	}

	private String sanitizeFileName(String fileName) {
		String base = fileName == null || fileName.isBlank() ? "TextDocument" : fileName.trim();
		base = base.replaceAll("[\\\\/:*?\"<>|]+", "").replaceAll("\\s+", "_");
		if (base.isBlank()) {
			base = "TextDocument";
		}
		return base.toLowerCase().endsWith(".pdf") ? base : base + ".pdf";
	}
}
