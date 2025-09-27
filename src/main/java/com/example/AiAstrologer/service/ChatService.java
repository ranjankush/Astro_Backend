
package com.example.AiAstrologer.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.model}")
    private String model;

    private static final String BASE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/";

    // In-memory session store: sessionId -> conversation history
    private final Map<String, List<Map<String, Object>>> conversations = new ConcurrentHashMap<>();

    /**
     * Build AI prompt based on user info and question
     */

    public String buildPrompt(String name, String rashi, String dob, String place, String userMessage) {
        StringBuilder p = new StringBuilder();
        p.append("You are a Vedic AI astrologer.\n")
                .append("Answer only astrology/palmistry/future questions. Keep it concise.\n")
                .append("Return Markdown with these exact H2 headings:\n")
                .append("## Overview\n## Future Prediction\n## Health\n## Career\n## Wealth\n")
                .append("Use bullet points under each section.\n")
                .append("After first time answer you should ignore the heading and focused on answering actual questions in ten lines.\n")
                .append("End with a final line:\n\n")
                .append("**✨ AI can make mistakes. This guidance is for reference only.**\n\n")
                .append("User details:\n")
                .append("- Name: ").append(Optional.ofNullable(name).orElse("Unknown")).append("\n")
                .append("- Rashi: ").append(Optional.ofNullable(rashi).orElse("Unknown")).append("\n")
                .append("- DOB: ").append(Optional.ofNullable(dob).orElse("Unknown")).append("\n")
                .append("- Place: ").append(Optional.ofNullable(place).orElse("Unknown")).append("\n")
                .append("- Question: ").append(Optional.ofNullable(userMessage).orElse("")).append("\n");

        return p.toString();
    }

    /**
     * Chat with AI using session memory
     */
    public String chat(String sessionId, String prompt) {
        List<Map<String, Object>> conversation =
                conversations.computeIfAbsent(sessionId, k -> new ArrayList<>());

        // Add user message
        conversation.add(Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", prompt))
        ));

        // Build request
        Map<String, Object> request = Map.of("contents", conversation);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        String url = BASE_URL + model + ":generateContent?key=" + apiKey;
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            Map candidate = (Map) ((List) response.getBody().get("candidates")).get(0);
            Map content = (Map) candidate.get("content");
            List<Map> parts = (List<Map>) content.get("parts");
            String modelReply = (String) parts.get(0).get("text");

            // Store AI reply in session
            conversation.add(Map.of(
                    "role", "model",
                    "parts", List.of(Map.of("text", modelReply))
            ));

            return modelReply;
        } catch (Exception e) {
            return "✨ Sorry, AI astrologer is temporarily unavailable. Please try again later.";
        }
    }

    /**
     * Reset chat session
     */
    public void resetConversation(String sessionId) {
        conversations.remove(sessionId);
    }
}
