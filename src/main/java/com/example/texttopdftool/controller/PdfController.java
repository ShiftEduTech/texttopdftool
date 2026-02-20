package com.example.texttopdftool.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.*;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.*;
import com.itextpdf.kernel.font.*;
import com.itextpdf.io.font.*;

import java.io.*;

@RestController
public class PdfController {

    @PostMapping("/generate")
    public ResponseEntity<byte[]> generatePdf(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("fontSize") int fontSize,
            @RequestParam(defaultValue = "36") float marginTop,
            @RequestParam(defaultValue = "36") float marginBottom,
            @RequestParam(defaultValue = "36") float marginLeft,
            @RequestParam(defaultValue = "36") float marginRight,
            @RequestParam(defaultValue = "1.2") float lineSpacing,
            @RequestParam(defaultValue = "LEFT") String alignment
    ) throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        pdf.setDefaultPageSize(PageSize.A4);

        Document document = new Document(pdf);

        // ✅ Apply Margins
        document.setMargins(marginTop, marginRight, marginBottom, marginLeft);

        // ✅ Load Safe Unicode Font (Use NotoSans for stability)
        InputStream fontStream = getClass()
                .getClassLoader()
                .getResourceAsStream("fonts/SEGUIEMJ.TTF");

        byte[] fontBytes = fontStream.readAllBytes();
        FontProgram fontProgram = FontProgramFactory.createFont(fontBytes);

        PdfFont font = PdfFontFactory.createFont(
                fontProgram,
                PdfEncodings.IDENTITY_H,
                PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED
        );

        // ✅ Title
        if (title != null && !title.trim().isEmpty()) {
            Paragraph titlePara = new Paragraph(title)
                    .setFont(font)
                    .setBold()
                    .setFontSize(fontSize + 6)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);

            document.add(titlePara);
        }

        // ✅ Alignment Selection
        TextAlignment textAlignment = TextAlignment.LEFT;

        switch (alignment.toUpperCase()) {
            case "CENTER":
                textAlignment = TextAlignment.CENTER;
                break;
            case "RIGHT":
                textAlignment = TextAlignment.RIGHT;
                break;
            case "JUSTIFIED":
                textAlignment = TextAlignment.JUSTIFIED;
                break;
        }

        // ✅ Content
        Paragraph contentPara = new Paragraph(content)
                .setFont(font)
                .setFontSize(fontSize)
                .setTextAlignment(textAlignment)
                .setMultipliedLeading(lineSpacing);

        document.add(contentPara);

        // ✅ Add Page Numbers
        int numberOfPages = pdf.getNumberOfPages();
        for (int i = 1; i <= numberOfPages; i++) {
            PdfPage page = pdf.getPage(i);
            float x = page.getPageSize().getWidth() / 2;
            float y = 20;

            document.showTextAligned(
                    new Paragraph("Page " + i + " of " + numberOfPages)
                            .setFont(font)
                            .setFontSize(10),
                    x, y, i,
                    TextAlignment.CENTER,
                    VerticalAlignment.BOTTOM,
                    0
            );
        }

        document.close();

        byte[] pdfBytes = baos.toByteArray();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=TextDocument.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(pdfBytes);
    }
}
