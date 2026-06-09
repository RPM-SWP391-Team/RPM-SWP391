package com.rpm.remotepatientmonitoring.controller;

import com.rpm.remotepatientmonitoring.dto.HealthLogRequest;
import com.rpm.remotepatientmonitoring.service.PatientHealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController
@RequestMapping("/api/patient/health")
public class PatientHealthController {
    @Autowired
    private PatientHealthService healthService;
    @PostMapping("/log")
    public ResponseEntity<?> recordHealthLog(@RequestBody HealthLogRequest request) {
        try {
            if (request.getPatientId() == null) {
                request.setPatientId(4);
            }
            Map<String, Object> result = healthService.submitDailyHealthLog(request);
            String message = (String) result.get("result_message");
            if (message != null && message.startsWith("Lỗi")) {
                return ResponseEntity.badRequest().body(result);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }
}