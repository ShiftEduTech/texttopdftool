package com.example.texttopdftool.service;

import com.example.texttopdftool.dto.SmartFormatResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SmartFormatService {

	private static final Pattern BULLET = Pattern.compile("^\\s*([-*•]|\\d+\\.)\\s+.+");

	public SmartFormatResponse format(String rawTitle, String rawContent) {
		String content = rawContent == null ? "" : rawContent.replace("\r\n", "\n").trim();
		String title = rawTitle == null ? "" : rawTitle.trim();

		List<String> lines = Arrays.stream(content.split("\n"))
				.map(String::stripTrailing)
				.collect(Collectors.toCollection(ArrayList::new));

		if (title.isBlank() && !lines.isEmpty()) {
			String first = lines.get(0).replaceFirst("^#+\\s*", "").trim();
			if (first.length() <= 80) {
				title = first;
				lines.remove(0);
				while (!lines.isEmpty() && lines.get(0).isBlank()) {
					lines.remove(0);
				}
			}
		}

		StringBuilder formatted = new StringBuilder();
		List<String> paragraph = new ArrayList<>();
		for (String line : lines) {
			if (line.isBlank()) {
				flushParagraph(formatted, paragraph);
				continue;
			}
			if (BULLET.matcher(line).matches() || line.startsWith("#")) {
				flushParagraph(formatted, paragraph);
				formatted.append(line.trim()).append('\n');
				continue;
			}
			paragraph.add(line.trim());
		}
		flushParagraph(formatted, paragraph);

		String result = formatted.toString().trim();
		int words = result.isBlank() ? 0 : result.split("\\s+").length;
		int fontSize = words > 1200 ? 11 : words > 600 ? 12 : 14;
		return new SmartFormatResponse(title, result, fontSize);
	}

	private void flushParagraph(StringBuilder out, List<String> paragraph) {
		if (paragraph.isEmpty()) {
			return;
		}
		if (out.length() > 0) {
			out.append('\n');
		}
		out.append(String.join(" ", paragraph)).append("\n\n");
		paragraph.clear();
	}
}
