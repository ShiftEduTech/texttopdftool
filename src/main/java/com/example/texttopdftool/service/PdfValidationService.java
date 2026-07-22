package com.example.texttopdftool.service;

import com.example.texttopdftool.dto.PdfRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@Service
public class PdfValidationService {

	private static final Set<String> PAGE_SIZES = Set.of("A4", "LETTER");
	private static final Set<String> ALIGNMENTS = Set.of("LEFT", "CENTER", "RIGHT", "JUSTIFIED");

	private final int maxContentChars;
	private final int maxFileNameChars;
	private final int maxHeadingChars;

	public PdfValidationService(
			@Value("${app.pdf.max-content-chars:100000}") int maxContentChars,
			@Value("${app.pdf.max-file-name-chars:200}") int maxFileNameChars,
			@Value("${app.pdf.max-heading-chars:300}") int maxHeadingChars) {
		this.maxContentChars = maxContentChars;
		this.maxFileNameChars = maxFileNameChars;
		this.maxHeadingChars = maxHeadingChars;
	}

	public void validate(PdfRequest request) {
		if (request == null) {
			throw badRequest("Request body is required");
		}
		String content = request.getContent() == null ? "" : request.getContent().trim();
		if (content.isEmpty()) {
			throw badRequest("Content is required");
		}
		if (content.length() > maxContentChars) {
			throw badRequest("Content exceeds maximum of " + maxContentChars + " characters");
		}
		String fileName = request.getFileName() == null ? "" : request.getFileName().trim();
		if (fileName.length() > maxFileNameChars) {
			throw badRequest("File name exceeds maximum of " + maxFileNameChars + " characters");
		}
		String heading = request.getHeading() == null ? "" : request.getHeading().trim();
		if (heading.length() > maxHeadingChars) {
			throw badRequest("Heading exceeds maximum of " + maxHeadingChars + " characters");
		}
		if (request.getFontSize() < 8 || request.getFontSize() > 36) {
			throw badRequest("Font size must be between 8 and 36");
		}
		String pageSize = request.getPageSize() == null ? "A4" : request.getPageSize().trim().toUpperCase();
		if (!PAGE_SIZES.contains(pageSize)) {
			throw badRequest("pageSize must be A4 or Letter");
		}
		request.setPageSize(pageSize);
		String alignment = request.getAlignment() == null ? "LEFT" : request.getAlignment().trim().toUpperCase();
		if (!ALIGNMENTS.contains(alignment)) {
			throw badRequest("alignment must be LEFT, CENTER, RIGHT, or JUSTIFIED");
		}
		request.setAlignment(alignment);
		if (request.getMarginTop() < 0 || request.getMarginBottom() < 0 || request.getMarginLeft() < 0
				|| request.getMarginRight() < 0 || request.getMarginTop() > 200 || request.getMarginBottom() > 200
				|| request.getMarginLeft() > 200 || request.getMarginRight() > 200) {
			throw badRequest("Margins must be between 0 and 200");
		}
		if (request.getLineSpacing() < 0.8f || request.getLineSpacing() > 3.0f) {
			throw badRequest("lineSpacing must be between 0.8 and 3.0");
		}
		request.setContent(content);
		request.setFileName(fileName);
		request.setHeading(heading);
	}

	private ResponseStatusException badRequest(String message) {
		return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
	}
}
