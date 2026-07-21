package com.rpm.remotepatientmonitoring.controller;

import com.rpm.remotepatientmonitoring.dto.AiChatRequest;
import com.rpm.remotepatientmonitoring.dto.AiChatResponse;
import com.rpm.remotepatientmonitoring.dto.AiSearchRequest;
import com.rpm.remotepatientmonitoring.dto.AiSearchResponse;
import com.rpm.remotepatientmonitoring.dto.AiSummaryResponse;
import com.rpm.remotepatientmonitoring.service.AiChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@RestController
@RequestMapping("/api/chatbot")
public class AiChatController {

    @Autowired
    private AiChatService aiChatService;

    @PostMapping("/ask")
    public ResponseEntity<AiChatResponse> askQuestion(@RequestBody AiChatRequest request, Principal principal) {
        String email = principal != null ? principal.getName() : "anonymous";
        
        // Lấy thông tin ngữ cảnh thực tế từ Database
        String context = aiChatService.buildContext(email, request.getPatientId());
        
        AiChatResponse response = aiChatService.getChatbotResponse(email, request.getQuestion(), context);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/search")
    public ResponseEntity<AiSearchResponse> searchMedicalDocuments(@RequestBody AiSearchRequest request) {
        AiSearchResponse response = aiChatService.getSemanticSearchResults(request.getQuery());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary/{patientId}")
    public ResponseEntity<AiSummaryResponse> getPatientSummary(@PathVariable Integer patientId) {
        AiSummaryResponse response = aiChatService.analyzePatientCondition(patientId);
        return ResponseEntity.ok(response);
    }
}
