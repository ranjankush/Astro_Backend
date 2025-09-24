
package com.example.AiAstrologer.service;
import com.example.AiAstrologer.response.RashiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class RashiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.model}")
    private String model;

    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
    private final RestTemplate restTemplate = new RestTemplate();
    private static final Map<String, String> SYLLABLE_TO_RASHI = new HashMap<>();

    static {
        // Same syllable mapping as before
        Arrays.asList("A","L","E","CH","CHH").forEach(s -> SYLLABLE_TO_RASHI.put(s,"Mesha (Aries)"));
        Arrays.asList("B","BH","V","U").forEach(s -> SYLLABLE_TO_RASHI.put(s,"Vrishabha (Taurus)"));
        Arrays.asList("K","KH","G","GH").forEach(s -> SYLLABLE_TO_RASHI.put(s,"Mithuna (Gemini)"));
        Arrays.asList("D","DH","H").forEach(s -> SYLLABLE_TO_RASHI.put(s,"Karka (Cancer)"));
        Arrays.asList("M","T").forEach(s -> SYLLABLE_TO_RASHI.put(s,"Simha (Leo)"));
        Arrays.asList("P","PH","Y").forEach(s -> SYLLABLE_TO_RASHI.put(s,"Kanya (Virgo)"));
        Arrays.asList("R","RA","TA").forEach(s -> SYLLABLE_TO_RASHI.put(s,"Tula (Libra)"));
        Arrays.asList("N","NA","F","FA","BH").forEach(s -> SYLLABLE_TO_RASHI.put(s,"Vrishchika (Scorpio)"));
        Arrays.asList("D","J","JA","JI").forEach(s -> SYLLABLE_TO_RASHI.put(s,"Dhanu (Sagittarius)"));
        Arrays.asList("J","SH","SHA").forEach(s -> SYLLABLE_TO_RASHI.put(s,"Makara (Capricorn)"));
        Arrays.asList("G","SU","S","GA").forEach(s -> SYLLABLE_TO_RASHI.put(s,"Kumbha (Aquarius)"));
        Arrays.asList("D","Z","ZA","ZI").forEach(s -> SYLLABLE_TO_RASHI.put(s,"Meena (Pisces)"));
    }

    public String getRashiByName(String name) {
        String n = name.trim().toUpperCase();
        if (n.isEmpty()) return "Unknown";

        String firstTwo = n.length() >= 2 ? n.substring(0,2) : n.substring(0,1);
        String firstOne = n.substring(0,1);

        if (SYLLABLE_TO_RASHI.containsKey(firstTwo)) return SYLLABLE_TO_RASHI.get(firstTwo);
        if (SYLLABLE_TO_RASHI.containsKey(firstOne)) return SYLLABLE_TO_RASHI.get(firstOne);

        return "Unknown";
    }

    private String generateRashiDescription(String rashi) {
        String prompt = """
            You are a friendly Vedic astrologer.
            The user's Rashi is: %s.
            Give:
            1) 2-sentence personality summary
            2) 1-line luck tip
            Keep it short and positive.
            Always end with: "✨ AI can made mistake." in bold letter
            """.formatted(rashi);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> userMessage = Map.of(
                    "role", "user",
                    "parts", List.of(Map.of("text", prompt))
            );
            Map<String, Object> requestBody = Map.of("contents", List.of(userMessage));
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String url = BASE_URL + model + ":generateContent?key=" + apiKey;
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            Map body = response.getBody();
            if (body == null || !body.containsKey("candidates")) return "✨ AI can made mistake.";

            List candidates = (List) body.get("candidates");
            if (candidates.isEmpty()) return "✨ AI can made mistake.";

            Map candidate = (Map) candidates.get(0);
            Map content = (Map) candidate.get("content");
            List parts = (List) content.get("parts");
            if (parts.isEmpty()) return "✨ AI can made mistake.";

            Map firstPart = (Map) parts.get(0);
            return (String) firstPart.getOrDefault("text", "✨AI can made mistake.");

        } catch (Exception e) {
            return "Friendly, practical personality. Focus on balance and positivity. ✨AI can made mistake.";
        }
    }

    // Public method used by controller
    public RashiResponse getRashiDescription(String name) {
        if (name == null || name.trim().isEmpty())
            return new RashiResponse("", "Unknown", "Please provide a valid name.");

        String rashi = getRashiByName(name);
        if (rashi.equals("Unknown"))
            return new RashiResponse(name, rashi, "Could not determine Rashi for this name.");

        String description = generateRashiDescription(rashi);
        return new RashiResponse(name, rashi, description);
    }
}

