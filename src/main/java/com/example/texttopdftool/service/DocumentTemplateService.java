package com.example.texttopdftool.service;

import com.example.texttopdftool.dto.PdfRequest;
import com.example.texttopdftool.model.DocumentTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class DocumentTemplateService {

	private static final Set<String> LEGACY_ALIASES = Set.of("report", "letter", "resume-lite", "resume");

	private final Map<String, DocumentTemplate> templates = new LinkedHashMap<>();

	public DocumentTemplateService() {
		templates.put("notes", new DocumentTemplate("notes", "Text to PDF", 40, 40, 40, 40, 1.35f, "LEFT", 14, false));
		templates.put("programming",
				new DocumentTemplate("programming", "Program to PDF", 40, 40, 40, 40, 1.3f, "LEFT", 12, true));
	}

	public Collection<DocumentTemplate> list() {
		return templates.values();
	}

	public DocumentTemplate get(String id) {
		if (id == null || id.isBlank()) {
			return templates.get("notes");
		}
		String key = id.toLowerCase(Locale.ROOT).trim();
		if (LEGACY_ALIASES.contains(key)) {
			return templates.get("notes");
		}
		return templates.getOrDefault(key, templates.get("notes"));
	}

	public void applyDefaults(PdfRequest request) {
		DocumentTemplate template = get(request.getTemplate());
		request.setTemplate(template.getId());
		request.setMarginTop(template.getMarginTop());
		request.setMarginBottom(template.getMarginBottom());
		request.setMarginLeft(template.getMarginLeft());
		request.setMarginRight(template.getMarginRight());
		request.setLineSpacing(template.getLineSpacing());
		request.setAlignment(template.getAlignment());
		if (request.getFontSize() <= 0) {
			request.setFontSize(template.getFontSize());
		}
	}
}
