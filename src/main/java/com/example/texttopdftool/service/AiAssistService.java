package com.example.texttopdftool.service;

import com.example.texttopdftool.dto.AiAssistRequest;
import com.example.texttopdftool.dto.AiAssistResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AiAssistService {

	private static final Pattern CONTENT_PATTERN = Pattern.compile("\"content\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");

	private final boolean enabled;
	private final String openAiApiKey;
	private final SmartFormatService smartFormatService;
	private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

	public AiAssistService(
			@Value("${app.ai.enabled:true}") boolean enabled,
			@Value("${app.ai.openai-api-key:}") String openAiApiKey,
			SmartFormatService smartFormatService) {
		this.enabled = enabled;
		this.openAiApiKey = openAiApiKey == null ? "" : openAiApiKey.trim();
		this.smartFormatService = smartFormatService;
	}

	public AiAssistResponse assist(AiAssistRequest request) {
		if (!enabled) {
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "AI assist is disabled");
		}
		String action = request.getAction().trim().toLowerCase(Locale.ROOT);
		if (!openAiApiKey.isBlank()) {
			try {
				String remote = callOpenAi(action, request.getTitle(), request.getContent());
				var smart = smartFormatService.format(request.getTitle(), remote);
				return new AiAssistResponse(action, remote, "openai", smart.getTitle(), smart.getSuggestedFontSize());
			} catch (Exception ignored) {
				// Fall back to local heuristics
			}
		}
		return localAssist(action, request.getTitle(), request.getContent());
	}

	private AiAssistResponse localAssist(String action, String title, String content) {
		return switch (action) {
			case "summarize" -> {
				String summary = summarize(content);
				yield new AiAssistResponse(action, summary, "local", title, 14);
			}
			case "rewrite" -> {
				var smart = smartFormatService.format(title, rewrite(content));
				yield new AiAssistResponse(action, smart.getContent(), "local", smart.getTitle(),
						smart.getSuggestedFontSize());
			}
			case "expand" -> {
				String expanded = expandOutline(content);
				yield new AiAssistResponse(action, expanded, "local", title, 12);
			}
			case "smart-format" -> {
				var smart = smartFormatService.format(title, content);
				yield new AiAssistResponse(action, smart.getContent(), "local", smart.getTitle(),
						smart.getSuggestedFontSize());
			}
			default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"action must be summarize, rewrite, expand, or smart-format");
		};
	}

	private String summarize(String content) {
		String[] sentences = content.split("(?<=[.!?])\\s+");
		int limit = Math.min(sentences.length, Math.max(2, sentences.length / 4));
		return Arrays.stream(sentences).limit(limit).map(String::trim).filter(s -> !s.isEmpty())
				.collect(Collectors.joining(" "));
	}

	private String rewrite(String content) {
		return content
				.replaceAll("[ \\t]+", " ")
				.replaceAll("\\s+([,.!?;:])", "$1")
				.replaceAll("\\bi\\b", "I")
				.replaceAll("(?i)\\bteh\\b", "the")
				.replaceAll("(?i)\\badn\\b", "and")
				.trim();
	}

	private String expandOutline(String content) {
		return Arrays.stream(content.split("\\R"))
				.map(String::trim)
				.filter(line -> !line.isEmpty())
				.map(line -> {
					String clean = line.replaceFirst("^([-*•]|\\d+\\.)\\s*", "");
					return "## " + clean + "\n\n"
							+ clean + " is an important point. Expand this section with supporting details, examples, "
							+ "and a short conclusion before moving to the next topic.\n";
				})
				.collect(Collectors.joining("\n"));
	}

	private String callOpenAi(String action, String title, String content) throws Exception {
		String prompt = "Action: " + action + "\nTitle: " + (title == null ? "" : title)
				+ "\nReturn only the improved document text.\n\n" + content;
		String body = """
				{
				  "model": "gpt-4o-mini",
				  "messages": [
				    {"role":"system","content":"You improve document text for PDF generation. Return plain text only."},
				    {"role":"user","content":%s}
				  ]
				}
				""".formatted(toJsonString(prompt));

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("https://api.openai.com/v1/chat/completions"))
				.timeout(Duration.ofSeconds(45))
				.header("Authorization", "Bearer " + openAiApiKey)
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(body))
				.build();
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() >= 300) {
			throw new IllegalStateException("OpenAI error " + response.statusCode());
		}
		Matcher matcher = CONTENT_PATTERN.matcher(response.body());
		String last = content;
		while (matcher.find()) {
			last = unescapeJson(matcher.group(1));
		}
		return last;
	}

	private static String toJsonString(String value) {
		String escaped = value
				.replace("\\", "\\\\")
				.replace("\"", "\\\"")
				.replace("\n", "\\n")
				.replace("\r", "\\r")
				.replace("\t", "\\t");
		return "\"" + escaped + "\"";
	}

	private static String unescapeJson(String value) {
		return value
				.replace("\\n", "\n")
				.replace("\\r", "\r")
				.replace("\\t", "\t")
				.replace("\\\"", "\"")
				.replace("\\\\", "\\");
	}
}
