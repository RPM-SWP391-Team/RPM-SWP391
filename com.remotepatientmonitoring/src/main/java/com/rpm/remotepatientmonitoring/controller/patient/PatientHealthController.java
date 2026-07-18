package com.rpm.remotepatientmonitoring.controller.patient;

import com.rpm.remotepatientmonitoring.dto.patient.HealthLogRequest;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.model.EmergencyProtocol;
import com.rpm.remotepatientmonitoring.config.CustomUserDetails;
import com.rpm.remotepatientmonitoring.service.patient.PatientHealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patient/health")
public class PatientHealthController {

    @Autowired
    private PatientHealthService healthService;

    private Patient getCurrentPatient() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            Object principal = auth.getPrincipal();
            if (principal instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) principal;
                Patient patient = healthService.getPatientByAccountId(userDetails.getAccount().getId());
                if (patient != null) {
                    return patient;
                }
            }
        }
        return null;
    }

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

    @GetMapping("/emergency-status")
    public ResponseEntity<?> getEmergencyStatus() {
        try {
            Patient patient = getCurrentPatient();
            if (patient == null) {
                // Fallback to first patient if no session (e.g. testing)
                List<Patient> allPatients = healthService.getAllPatients();
                if (allPatients.isEmpty() == false) {
                    patient = allPatients.get(0);
                }
            }
            if (patient == null) {
                return ResponseEntity.ok(Map.of(
                    "isEmergency", false,
                    "level", 1,
                    "emergencyContactName", "Chưa thiết lập",
                    "emergencyContactPhone", ""
                ));
            }

            Map<String, Object> response = healthService.getEmergencyStatus(patient);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/protocols")
    public ResponseEntity<?> getEmergencyProtocols() {
        try {
            Patient patient = getCurrentPatient();
            if (patient == null) {
                // Fallback to first patient if no session (e.g. testing)
                List<Patient> allPatients = healthService.getAllPatients();
                if (allPatients.isEmpty() == false) {
                    patient = allPatients.get(0);
                }
            }
            if (patient == null || patient.getHospital() == null) {
                return ResponseEntity.ok(List.of());
            }

            List<EmergencyProtocol> protocols = healthService.getEmergencyProtocols(patient.getHospital().getId());
            return ResponseEntity.ok(protocols);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}