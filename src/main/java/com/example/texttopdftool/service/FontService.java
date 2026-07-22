package com.example.texttopdftool.service;

import com.itextpdf.io.font.FontProgram;
import com.itextpdf.io.font.FontProgramFactory;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class FontService {

	private final byte[] regularFontBytes;
	private final byte[] boldFontBytes;

	public FontService() throws IOException {
		this.regularFontBytes = loadFirstAvailable(
				"fonts/SEGUIEMJ.TTF",
				"fonts/NotoSans-Regular.ttf");
		byte[] bold = tryLoad("fonts/SEGUIEMJ-BOLD.TTF");
		this.boldFontBytes = bold != null ? bold : this.regularFontBytes;
	}

	public PdfFont createRegular() throws IOException {
		return create(regularFontBytes);
	}

	public PdfFont createBold() throws IOException {
		return create(boldFontBytes);
	}

	private PdfFont create(byte[] bytes) throws IOException {
		FontProgram program = FontProgramFactory.createFont(bytes);
		return PdfFontFactory.createFont(program, PdfEncodings.IDENTITY_H,
				PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
	}

	private static byte[] loadFirstAvailable(String... paths) throws IOException {
		for (String path : paths) {
			byte[] bytes = tryLoad(path);
			if (bytes != null) {
				return bytes;
			}
		}
		throw new IOException("No embedded font found under classpath fonts/");
	}

	private static byte[] tryLoad(String path) {
		try {
			ClassPathResource resource = new ClassPathResource(path);
			if (!resource.exists()) {
				return null;
			}
			try (InputStream in = resource.getInputStream()) {
				return in.readAllBytes();
			}
		} catch (IOException ex) {
			return null;
		}
	}
}
