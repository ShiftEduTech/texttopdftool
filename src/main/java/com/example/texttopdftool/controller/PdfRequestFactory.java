package com.example.texttopdftool.controller;

import com.example.texttopdftool.dto.PdfRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class PdfRequestFactory {

	public PdfRequest fromRequest(HttpServletRequest request) {
		Map<String, String> values = new LinkedHashMap<>();
		request.getParameterMap().forEach((key, arr) -> {
			if (arr != null && arr.length > 0) {
				values.put(key, arr[0]);
			}
		});
		if (request instanceof MultipartHttpServletRequest multipart) {
			multipart.getParameterMap().forEach((key, arr) -> {
				if (arr != null && arr.length > 0) {
					values.put(key, arr[0]);
				}
			});
		}
		return fromMap(values);
	}

	public PdfRequest fromMap(Map<String, String> form) {
		PdfRequest request = new PdfRequest();
		String fileName = value(form, "fileName", null);
		if (fileName == null || fileName.isBlank()) {
			fileName = value(form, "title", "");
		}
		request.setFileName(fileName);
		request.setHeading(value(form, "heading", ""));
		request.setContent(value(form, "content", ""));
		request.setFontSize(intValue(form, "fontSize", 14));
		request.setPageSize(value(form, "pageSize", "A4"));
		request.setMarginTop(floatValue(form, "marginTop", 36f));
		request.setMarginBottom(floatValue(form, "marginBottom", 36f));
		request.setMarginLeft(floatValue(form, "marginLeft", 36f));
		request.setMarginRight(floatValue(form, "marginRight", 36f));
		request.setLineSpacing(floatValue(form, "lineSpacing", 1.2f));
		request.setAlignment(value(form, "alignment", "LEFT"));
		request.setTemplate(value(form, "template", "notes"));
		request.setOrgName(value(form, "orgName", null));
		request.setLanguage(value(form, "language", "en"));
		return request;
	}

	private String value(Map<String, String> form, String key, String defaultValue) {
		String value = form.get(key);
		return value == null ? defaultValue : value;
	}

	private int intValue(Map<String, String> form, String key, int defaultValue) {
		try {
			String value = form.get(key);
			return value == null || value.isBlank() ? defaultValue : Integer.parseInt(value.trim());
		} catch (NumberFormatException ex) {
			return defaultValue;
		}
	}

	private float floatValue(Map<String, String> form, String key, float defaultValue) {
		try {
			String value = form.get(key);
			return value == null || value.isBlank() ? defaultValue : Float.parseFloat(value.trim());
		} catch (NumberFormatException ex) {
			return defaultValue;
		}
	}
}
