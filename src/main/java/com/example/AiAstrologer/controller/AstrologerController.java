
package com.example.AiAstrologer.controller;

import com.example.AiAstrologer.response.RashiResponse;
import com.example.AiAstrologer.service.RashiService;
import com.example.AiAstrologer.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@CrossOrigin(origins = "${FRONTEND_API}")
@RestController
@RequestMapping("api/astrologer/")
public class AstrologerController {

    @Autowired
    private RashiService rashiService;

    @Autowired
    private ChatService chatService;

    /** Get Rashi based on name (first letter) using AI */
    @GetMapping("/rashi")
    public ResponseEntity<RashiResponse> getRashi(@RequestParam String name) {
        RashiResponse response = rashiService.getRashiDescription(name);
        return ResponseEntity.ok(response);
    }

    /** Chat with AI Astrologer (session auto-managed) **/

    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, Object> payload) {
        String name = (String) payload.get("name");
        String dob = (String) payload.get("dob");
        String place = (String) payload.get("place");
        String userMessage = (String) payload.get("message");
        String sessionId = (String) payload.get("sessionId");
        if (sessionId == null || sessionId.isEmpty()) sessionId = UUID.randomUUID().toString();

        String rashi = rashiService.getRashiByName(name);
        String prompt = chatService.buildPrompt(name, rashi, dob, place, userMessage);
        String reply = chatService.chat(sessionId, prompt);

        return ResponseEntity.ok(Map.of(
                "sessionId", sessionId, "name", name, "rashi", rashi,
                "userMessage", userMessage, "reply", reply
        ));
    }

    /** Reset chat session */
    @DeleteMapping("/chat/{sessionId}/reset")
    public ResponseEntity<Map<String, String>> resetChat(@PathVariable String sessionId) {
        chatService.resetConversation(sessionId);
        return ResponseEntity.ok(Map.of(
                "sessionId", sessionId,
                "status", "Conversation reset successfully ✅"
        ));
    }
}
