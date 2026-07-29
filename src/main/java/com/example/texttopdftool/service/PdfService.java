package com.example.texttopdftool.service;

import com.example.texttopdftool.dto.PdfRequest;
import com.example.texttopdftool.model.DocumentTemplate;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.function.IntConsumer;

@Service
public class PdfService {

	private final FontService fontService;
	private final DocumentTemplateService templateService;
	private final PdfValidationService validationService;
	private final String defaultOrgName;

	public PdfService(
			FontService fontService,
			DocumentTemplateService templateService,
			PdfValidationService validationService,
			@Value("${app.branding.org-name:CodePDF}") String defaultOrgName) {
		this.fontService = fontService;
		this.templateService = templateService;
		this.validationService = validationService;
		this.defaultOrgName = defaultOrgName;
	}

	public byte[] generate(PdfRequest request) throws Exception {
		return generate(request, null);
	}

	public byte[] generate(PdfRequest request, IntConsumer progress) throws Exception {
		report(progress, 5);
		validationService.validate(request);
		templateService.applyDefaults(request);
		DocumentTemplate template = templateService.get(request.getTemplate());
		report(progress, 15);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PdfWriter writer = new PdfWriter(baos);
		PdfDocument pdf = new PdfDocument(writer);
		pdf.setTagged();
		pdf.getCatalog().setLang(new com.itextpdf.kernel.pdf.PdfString(
				request.getLanguage() == null || request.getLanguage().isBlank() ? "en" : request.getLanguage()));
		String documentTitle = !isBlank(request.getFileName())
				? request.getFileName()
				: (!isBlank(request.getHeading()) ? request.getHeading() : "CodePDF Document");
		pdf.getDocumentInfo().setTitle(documentTitle);
		pdf.getDocumentInfo().setAuthor(orgName(request));
		pdf.getDocumentInfo().setCreator("CodePDF");
		pdf.setDefaultPageSize(resolvePageSize(request.getPageSize()));

		Document document = new Document(pdf);
		document.setMargins(request.getMarginTop(), request.getMarginRight(), request.getMarginBottom(),
				request.getMarginLeft());

		PdfFont regular = fontService.createRegular();
		PdfFont bold = fontService.createBold();
		int bodySize = request.getFontSize() > 0 ? request.getFontSize() : template.getFontSize();
		TextAlignment alignment = resolveAlignment(request.getAlignment());
		report(progress, 30);

		if (!isBlank(request.getHeading())) {
			document.add(new Paragraph(request.getHeading())
					.setFont(bold)
					.setBold()
					.setFontSize(bodySize + 5)
					.setTextAlignment(TextAlignment.CENTER)
					.setMarginBottom(18));
		}

		report(progress, 55);
		if (template.isCodeMode()) {
			// Clean programming PDF: same readable font as notes, keep every line.
			String normalized = request.getContent().replace("\r\n", "\n").replace('\r', '\n');
			String[] lines = normalized.split("\n", -1);
			for (String line : lines) {
				String text = line.replace("\t", "    ");
				if (text.isEmpty()) {
					text = "\u00A0";
				}
				document.add(new Paragraph(text)
						.setFont(regular)
						.setFontSize(bodySize)
						.setTextAlignment(TextAlignment.LEFT)
						.setMultipliedLeading(1.25f)
						.setMarginTop(0)
						.setMarginBottom(0));
			}
		} else {
			for (String block : request.getContent().split("\\R{2,}")) {
				String paragraphText = block.trim();
				if (paragraphText.isEmpty()) {
					continue;
				}
				document.add(new Paragraph(paragraphText)
						.setFont(regular)
						.setFontSize(bodySize)
						.setTextAlignment(alignment)
						.setMultipliedLeading(request.getLineSpacing())
						.setMarginBottom(10));
			}
		}
		report(progress, 80);

		document.flush();
		int numberOfPages = pdf.getNumberOfPages();
		for (int i = 1; i <= numberOfPages; i++) {
			PdfPage page = pdf.getPage(i);
			float width = page.getPageSize().getWidth();
			document.showTextAligned(
					new Paragraph("Page " + i + " of " + numberOfPages)
							.setFont(regular)
							.setFontSize(10),
					width / 2,
					20,
					i,
					TextAlignment.CENTER,
					VerticalAlignment.BOTTOM,
					0);
		}

		document.close();
		report(progress, 100);
		return baos.toByteArray();
	}

	private PageSize resolvePageSize(String pageSize) {
		if ("LETTER".equalsIgnoreCase(pageSize)) {
			return PageSize.LETTER;
		}
		return PageSize.A4;
	}

	private TextAlignment resolveAlignment(String alignment) {
		return switch (alignment.toUpperCase()) {
			case "CENTER" -> TextAlignment.CENTER;
			case "RIGHT" -> TextAlignment.RIGHT;
			case "JUSTIFIED" -> TextAlignment.JUSTIFIED;
			default -> TextAlignment.LEFT;
		};
	}

	private String orgName(PdfRequest request) {
		if (request.getOrgName() != null && !request.getOrgName().isBlank()) {
			return request.getOrgName().trim();
		}
		return defaultOrgName;
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	private void report(IntConsumer progress, int value) {
		if (progress != null) {
			progress.accept(value);
		}
	}
}
