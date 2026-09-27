package com.urooz.resumetailor.service;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.urooz.resumetailor.dto.ResumeData;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiAIService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.model}")
    private String model;

    @PostConstruct
    public void init() {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);
    }

    public ResumeData parseResumeTextToStructure(String rawPdfText) {
        log.info("Parsing raw text into Structured Resume Data...");

        String prompt = String.format("""
            Act as a Data Parser. Extract data from this raw resume text into JSON.
            
            RAW TEXT:
            "%s"
            
            REQUIRED JSON SCHEMA:
            {
              "fullName": "Name",
              "contactInfo": "Phone | Email | Location",
              "linkedin": "url",
              "github": "url",
              "summary": "Full summary text",
              "experience": [
                { "company": "Name", "role": "Title", "duration": "Dates", "bullets": ["point 1"] }
              ],
              "projects": [
                { "name": "Title", "duration": "Dates", "techStack": "Java...", "bullets": ["point 1"] }
              ],
              "education": [
                 { "institution": "Name", "degree": "Degree", "duration": "Dates", "gpa": "8.2" }
              ],
              "skills": [
                 { "category": "Backend", "values": "Java, Spring" },
                 { "category": "Frontend", "values": "React" }
              ]
            }
            
            CRITICAL RULES:
            1. **Projects vs Experience:** If an entry lists a "Tech Stack" or seems like a personal project (e.g., Voxy, Resume Analyzer), put it in the "projects" array, NOT "experience".
            2. **Quotes:** Do NOT use double quotes (") inside any value string. Use single quotes (') instead.
               - BAD: "Engineered a "Resume Analyzer" tool"
               - GOOD: "Engineered a 'Resume Analyzer' tool"
            3. **Skills:** Group them into categories (Backend, AI, DevOps, Frontend).
            
            Return ONLY raw JSON.
            """, rawPdfText.substring(0, Math.min(rawPdfText.length(), 7000)));

        String jsonResponse = callOpenAiApi(prompt);
        return convertJsonToResumeData(jsonResponse);
    }

    public ResumeData tailorResumeData(ResumeData currentData, String jobDescription) {
        log.info("Performing Surgical Optimization on Resume Data...");

        try {
            String currentDataJson = objectMapper.writeValueAsString(currentData);

            String prompt = String.format("""
        Act as an Expert ATS Resume Writer.
        
        INPUT JSON:
        %s
        
        TARGET JD:
        "%s"
        
        TASK:
        1. Rewrite 'summary' to match JD keywords (Max 3 lines).
        2. Rewrite 'experience' and 'projects' bullets to be Impactful & Result-Oriented.
           - DO NOT use labels like 'Situation:', 'Task:', 'Action:'.
           - Start directly with strong action verbs (e.g., "Engineered...", "Optimized...", "Reduced...").
           - Keep bullets concise (Max 2 lines per bullet).
        3. Optimize 'skills' if needed.
        
        CRITICAL CONSTRAINTS:
        - DO NOT change Company Names, Dates, or Project Titles.
        - DO NOT use double quotes (") inside strings. Use single quotes (') only.
        - Return valid JSON matching the exact input structure.
        """, currentDataJson, jobDescription);
            String tailoredJson = callOpenAiApi(prompt);
            return convertJsonToResumeData(tailoredJson);

        } catch (Exception e) {
            log.error("Error preparing AI request: {}", e.getMessage());
            throw new RuntimeException("Optimization Failed", e);
        }
    }

    private String callOpenAiApi(String prompt) {
        try {
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", prompt);

            JSONArray messages = new JSONArray();
            messages.put(message);

            JSONObject responseFormat = new JSONObject();
            responseFormat.put("type", "json_object");

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", model);
            requestBody.put("messages", messages);
            requestBody.put("response_format", responseFormat);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
            return extractTextFromResponse(response.getBody());
        } catch (Exception e) {
            log.error("API Call Failed: {}", e.getMessage());
            throw new RuntimeException("OpenAI API Error", e);
        }
    }

    private String extractTextFromResponse(String rawJson) {
        try {
            JSONObject root = new JSONObject(rawJson);
            String text = root.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

            int startIndex = text.indexOf("{");
            int endIndex = text.lastIndexOf("}");
            if (startIndex != -1 && endIndex != -1) {
                return text.substring(startIndex, endIndex + 1);
            }
            return text;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse OpenAI Response", e);
        }
    }

    private ResumeData convertJsonToResumeData(String jsonString) {
        try {
            return objectMapper.readValue(jsonString, ResumeData.class);
        } catch (Exception e) {
            log.error("JSON Mapping Failed. The AI returned:\n{}", jsonString);
            throw new RuntimeException("Failed to map JSON to Java Object", e);
        }
    }
}