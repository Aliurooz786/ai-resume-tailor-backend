package com.urooz.resumetailor.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.urooz.resumetailor.dto.ResumeData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfGenerationService {

    private final TemplateEngine templateEngine;

    /**
     * Generates a PDF file from the Structured ResumeData object.
     * Uses Thymeleaf to inject data into HTML and OpenHTMLToPDF to render it.
     */
    public byte[] generatePdf(ResumeData resumeData) {
        log.info("Starting PDF Generation for user: {}", resumeData.getFullName());

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {

            Context context = new Context();
            context.setVariable("data", resumeData);

            String processedHtml = templateEngine.process("resume_template", context);

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(processedHtml, null);
            builder.toStream(os);
            builder.run();

            log.info("PDF Generated successfully. Size: {} bytes", os.size());
            return os.toByteArray();

        } catch (Exception e) {
            log.error("PDF Generation Failed: {}", e.getMessage());
            throw new RuntimeException("Error generating PDF", e);
        }
    }
}