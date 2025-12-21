package com.problemio.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.problemio.global.exception.BusinessException;
import com.problemio.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestTemplateBuilder;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class GmsOpenAiClient {

    private static final Pattern DATA_URL_PATTERN =
            Pattern.compile("data:image/[^;]+;base64,([A-Za-z0-9+/=\\s]+)");
    private static final Pattern BASE64_BLOCK_PATTERN =
            Pattern.compile("([A-Za-z0-9+/=]{200,})");

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gms.base-url}")
    private String baseUrl;

    @Value("${gms.api-key}")
    private String apiKey;

    @Value("${gms.model:gpt-4o}")
    private String model;

    public GmsOpenAiClient(RestTemplateBuilder restTemplateBuilder, ObjectMapper objectMapper) {
        this.restTemplate = restTemplateBuilder.build();
        this.objectMapper = objectMapper;
    }

    public byte[] generatePngBytes(String title, String description, String styleHint) {
        String prompt = buildPrompt(title, description, styleHint);
        String base64 = requestImageBase64(prompt);
        try {
            return Base64.getDecoder().decode(base64);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private String requestImageBase64(String prompt) {
        for (int attempt = 0; attempt < 2; attempt++) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(apiKey);

                Map<String, Object> payload = Map.of(
                        "model", model,
                        "messages", List.of(
                                Map.of("role", "developer", "content", "You are a helpful assistant."),
                                Map.of("role", "user", "content", prompt)
                        )
                );

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
                ResponseEntity<String> response = restTemplate.exchange(
                        baseUrl,
                        HttpMethod.POST,
                        entity,
                        String.class
                );

                String content = extractContent(response.getBody());
                String base64 = extractBase64(content);
                if (base64 != null) {
                    return base64;
                }
            } catch (Exception ignored) {
                // Fall through to retry once
            }
        }

        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private String extractContent(String body) throws Exception {
        if (body == null || body.isBlank()) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        JsonNode root = objectMapper.readTree(body);
        JsonNode choices = root.get("choices");
        if (choices == null || !choices.isArray() || choices.isEmpty()) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        JsonNode message = choices.get(0).get("message");
        JsonNode content = message != null ? message.get("content") : null;
        if (content == null || content.isNull()) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        return content.asText();
    }

    private String extractBase64(String content) {
        if (content == null || content.isBlank()) {
            return null;
        }

        Matcher dataMatcher = DATA_URL_PATTERN.matcher(content);
        if (dataMatcher.find()) {
            return normalizeBase64(dataMatcher.group(1));
        }

        if (content.trim().startsWith("{")) {
            try {
                JsonNode node = objectMapper.readTree(content);
                if (node.has("image_base64")) {
                    return normalizeBase64(node.get("image_base64").asText());
                }
                if (node.has("base64")) {
                    return normalizeBase64(node.get("base64").asText());
                }
                if (node.has("image")) {
                    return normalizeBase64(node.get("image").asText());
                }
            } catch (Exception ignored) {
                // Continue to regex fallback
            }
        }

        Matcher blockMatcher = BASE64_BLOCK_PATTERN.matcher(content);
        if (blockMatcher.find()) {
            return normalizeBase64(blockMatcher.group(1));
        }

        return null;
    }

    private String normalizeBase64(String raw) {
        if (raw == null) return null;
        return raw.replaceAll("\\s+", "");
    }

    private String buildPrompt(String title, String description, String styleHint) {
        return String.join("\n",
                "Create a single thumbnail image as a PNG, then respond with only the base64-encoded PNG bytes.",
                "No markdown, no extra text, no data URL, only base64.",
                "Do not include any text, logos, or copyrighted characters in the image.",
                "Make it suitable as a quiz thumbnail (simple, high contrast, easy to recognize).",
                "Style hint: " + styleHint,
                "Title: " + title,
                "Description: " + description
        );
    }
}
