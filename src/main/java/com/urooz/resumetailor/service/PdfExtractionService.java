package com.urooz.resumetailor.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Service class responsible for handling PDF operations.
 * Primarily uses Apache PDFBox to parse and extract raw text from resume files.
 */
@Service
@Slf4j
public class PdfExtractionService {

    /**
     * Extracts raw text from a given PDF file.
     *
     * @param file The MultipartFile (PDF) uploaded by the user.
     * @return Extracted text as a String.
     * @throws RuntimeException if parsing fails.
     */
    public String extractTextFromPdf(MultipartFile file) {
        log.info("Starting text extraction for file: {}", file.getOriginalFilename());

        if (file.isEmpty()) {
            log.warn("Uploaded file is empty!");
            throw new IllegalArgumentException("File must not be empty");
        }

         try (PDDocument document = PDDocument.load(file.getInputStream())) {

            log.debug("PDF Document loaded successfully into memory.");

            PDFTextStripper pdfStripper = new PDFTextStripper();
            String extractedText = pdfStripper.getText(document);

            log.info("Text extraction completed. Extracted {} characters.", extractedText.length());
            return extractedText;

        } catch (IOException e) {
            log.error("Error occurred while parsing PDF: {}", e.getMessage());
            throw new RuntimeException("Failed to extract text from PDF", e);
        }
    }
}