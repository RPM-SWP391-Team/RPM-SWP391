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

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AiChatController.class);

    @Autowired
    private AiChatService aiChatService;

    @PostMapping("/ask")
    public ResponseEntity<AiChatResponse> askQuestion(@RequestBody AiChatRequest request, Principal principal) {
        String email = principal != null ? principal.getName() : "anonymous";
        log.info("[CHATBOT] Received ask: email={}, patientId={}, question={}", email, request.getPatientId(), request.getQuestion());
        
        // Lấy thông tin ngữ cảnh thực tế từ Database
        String context = aiChatService.buildContext(email, request.getPatientId());
        log.info("[CHATBOT] Built context (first 200 chars): {}", context != null && context.length() > 200 ? context.substring(0, 200) : context);
        
        AiChatResponse response = aiChatService.getChatbotResponse(email, request.getPatientId(), request.getQuestion(), context);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/search")
    public ResponseEntity<AiSearchResponse> searchMedicalDocuments(@RequestBody AiSearchRequest request) {
        AiSearchResponse response = aiChatService.getSemanticSearchResults(request.getQuery());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary/{patientId}")
    public ResponseEntity<AiSummaryResponse> getPatientSummary(
            @PathVariable Integer patientId,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate) {
        AiSummaryResponse response = aiChatService.analyzePatientCondition(patientId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<java.util.List<com.rpm.remotepatientmonitoring.model.AiChatHistory>> getChatHistory(
            @RequestParam(required = false) Integer patientId, 
            Principal principal) {
        String email = principal != null ? principal.getName() : "anonymous";
        return ResponseEntity.ok(aiChatService.getChatHistory(email, patientId));
    }

    @DeleteMapping("/history")
    public ResponseEntity<java.util.Map<String, Object>> clearChatHistory(
            @RequestParam(required = false) Integer patientId, 
            Principal principal) {
        String email = principal != null ? principal.getName() : "anonymous";
        aiChatService.clearChatHistory(email, patientId);
        java.util.Map<String, Object> res = new java.util.HashMap<>();
        res.put("success", true);
        res.put("message", "Đã xóa lịch sử chat thành công.");
        return ResponseEntity.ok(res);
    }
}
