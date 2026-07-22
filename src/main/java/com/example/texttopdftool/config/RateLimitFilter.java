package com.example.texttopdftool.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class RateLimitFilter extends OncePerRequestFilter {

	private final int requestsPerMinute;
	private final Map<String, Window> windows = new ConcurrentHashMap<>();

	public RateLimitFilter(@Value("${app.rate-limit.requests-per-minute:60}") int requestsPerMinute) {
		this.requestsPerMinute = Math.max(1, requestsPerMinute);
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();
		if (path.startsWith("/actuator")) {
			return true;
		}
		// Polling / download / shared PDF must not burn the rate budget.
		if ("GET".equalsIgnoreCase(request.getMethod()) && (
				path.startsWith("/api/generate/status/")
						|| path.startsWith("/api/generate/result/")
						|| path.startsWith("/api/share/"))) {
			return true;
		}
		return "GET".equalsIgnoreCase(request.getMethod()) && !path.startsWith("/api/");
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		cleanup();
		String key = clientKey(request);
		Window window = windows.computeIfAbsent(key, ignored -> new Window());
		synchronized (window) {
			long now = Instant.now().getEpochSecond();
			if (now - window.startedAt >= 60) {
				window.startedAt = now;
				window.count.set(0);
			}
			if (window.count.incrementAndGet() > requestsPerMinute) {
				response.setStatus(429);
				response.setContentType(MediaType.APPLICATION_JSON_VALUE);
				response.getWriter().write("{\"error\":\"rate_limited\",\"message\":\"Too many requests\"}");
				return;
			}
		}
		filterChain.doFilter(request, response);
	}

	private String clientKey(HttpServletRequest request) {
		String forwarded = request.getHeader("X-Forwarded-For");
		if (forwarded != null && !forwarded.isBlank()) {
			return forwarded.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}

	private void cleanup() {
		long now = Instant.now().getEpochSecond();
		Iterator<Map.Entry<String, Window>> it = windows.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Window> entry = it.next();
			if (now - entry.getValue().startedAt > 120) {
				it.remove();
			}
		}
	}

	private static final class Window {
		private long startedAt = Instant.now().getEpochSecond();
		private final AtomicInteger count = new AtomicInteger();
	}
}
