package com.example.texttopdftool;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PdfControllerTests {

	private static final Pattern JOB_ID = Pattern.compile("\"jobId\"\\s*:\\s*\"([^\"]+)\"");
	private static final Pattern STATUS = Pattern.compile("\"status\"\\s*:\\s*\"([^\"]+)\"");

	@Autowired
	private MockMvc mockMvc;

	@Test
	void generatePdfReturnsPdfBytes() throws Exception {
		MvcResult result = mockMvc.perform(multipart("/generate")
						.param("fileName", "JAVA OOPS Notes")
						.param("heading", "Introduction to OOPS")
						.param("content", "Hello ShiftEduTech PDF")
						.param("fontSize", "14")
						.param("pageSize", "A4")
						.param("alignment", "LEFT")
						.param("template", "notes"))
				.andExpect(status().isOk())
				.andReturn();

		byte[] body = result.getResponse().getContentAsByteArray();
		assertThat(body.length).isGreaterThan(100);
		assertThat(new String(body, 0, 4)).isEqualTo("%PDF");

		MvcResult code = mockMvc.perform(multipart("/generate")
						.param("fileName", "Java Demo")
						.param("heading", "Hello World")
						.param("content", "public class Main {\n  public static void main(String[] args) {\n    System.out.println(\"Hi\");\n  }\n}")
						.param("fontSize", "11")
						.param("pageSize", "A4")
						.param("template", "programming"))
				.andExpect(status().isOk())
				.andReturn();
		assertThat(new String(code.getResponse().getContentAsByteArray(), 0, 4)).isEqualTo("%PDF");
	}

	@Test
	void generateRejectsEmptyContent() throws Exception {
		mockMvc.perform(multipart("/generate")
						.param("fileName", "Demo")
						.param("heading", "Demo Heading")
						.param("content", "   ")
						.param("fontSize", "14"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").exists());
	}

	@Test
	void asyncGenerateCompletes() throws Exception {
		MvcResult start = mockMvc.perform(multipart("/api/generate/async")
						.param("fileName", "Async Notes")
						.param("heading", "Async Heading")
						.param("content", "Async content for PDF generation.")
						.param("fontSize", "12")
						.param("pageSize", "Letter"))
				.andExpect(status().isOk())
				.andReturn();

		String jobId = match(JOB_ID, start.getResponse().getContentAsString());
		assertThat(jobId).isNotBlank();

		String status = "QUEUED";
		for (int i = 0; i < 50 && ("QUEUED".equals(status) || "RUNNING".equals(status)); i++) {
			Thread.sleep(100);
			MvcResult poll = mockMvc.perform(get("/api/generate/status/" + jobId))
					.andExpect(status().isOk())
					.andReturn();
			status = match(STATUS, poll.getResponse().getContentAsString());
		}
		assertThat(status).isEqualTo("DONE");

		MvcResult pdf = mockMvc.perform(get("/api/generate/result/" + jobId))
				.andExpect(status().isOk())
				.andReturn();
		assertThat(pdf.getResponse().getContentAsByteArray()[0]).isEqualTo((byte) '%');
	}

	@Test
	void aiAssistWorks() throws Exception {
		mockMvc.perform(post("/api/ai/assist")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"action":"smart-format","title":"","content":"My Title\\n\\nhello world"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").exists())
				.andExpect(jsonPath("$.mode").value("local"));
	}

	private static String match(Pattern pattern, String text) {
		Matcher matcher = pattern.matcher(text);
		assertThat(matcher.find()).isTrue();
		return matcher.group(1);
	}
}
