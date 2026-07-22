package com.example.texttopdftool.controller;

import com.example.texttopdftool.dto.AiAssistRequest;
import com.example.texttopdftool.dto.AiAssistResponse;
import com.example.texttopdftool.service.AiAssistService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AiController {

	private final AiAssistService aiAssistService;

	public AiController(AiAssistService aiAssistService) {
		this.aiAssistService = aiAssistService;
	}

	@PostMapping("/api/ai/assist")
	public AiAssistResponse assist(@Valid @RequestBody AiAssistRequest request) {
		return aiAssistService.assist(request);
	}
}
