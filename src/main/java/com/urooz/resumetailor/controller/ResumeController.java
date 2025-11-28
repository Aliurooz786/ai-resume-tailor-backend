package com.urooz.resumetailor.controller;

import com.urooz.resumetailor.dto.ResumeData;
import com.urooz.resumetailor.service.GeminiAIService;
import com.urooz.resumetailor.service.PdfExtractionService;
import com.urooz.resumetailor.service.PdfGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/resume")
@RequiredArgsConstructor
@Slf4j
public class ResumeController {

    private final PdfExtractionService pdfExtractionService;
    private final GeminiAIService geminiAIService;
    private final PdfGenerationService pdfGenerationService;

    @PostMapping(value = "/tailor", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> tailorResume(
            @RequestParam("file") MultipartFile file,
            @RequestParam("jobDescription") String jobDescription
    ) {
        log.info("Request received to tailor resume. JD Length: {}", jobDescription.length());

        try {
            String rawResumeText = pdfExtractionService.extractTextFromPdf(file);
            log.debug("Raw text extracted. Length: {}", rawResumeText.length());

            ResumeData structuredData = geminiAIService.parseResumeTextToStructure(rawResumeText);
            log.debug("Resume parsed into structure. Name: {}", structuredData.getFullName());

            ResumeData tailoredData = geminiAIService.tailorResumeData(structuredData, jobDescription);
            log.debug("Resume data optimized for JD.");

            byte[] pdfBytes = pdfGenerationService.generatePdf(tailoredData);
            log.info("Final PDF generated. Size: {} bytes", pdfBytes.length);

            String userName = tailoredData.getFullName();

            if (userName == null || userName.trim().isEmpty()) {
                userName = "Candidate";
            }

            String safeName = userName.trim().replaceAll("[^a-zA-Z0-9]", "_");
            String filename = safeName + "_ATS_Optimized_Resume.pdf";


            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (Exception e) {
            log.error("Error processing request: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}