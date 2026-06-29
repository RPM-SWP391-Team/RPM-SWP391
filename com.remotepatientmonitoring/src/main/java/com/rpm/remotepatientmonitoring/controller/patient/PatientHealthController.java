package com.rpm.remotepatientmonitoring.controller.patient;

import com.rpm.remotepatientmonitoring.dto.patient.HealthLogRequest;
import com.rpm.remotepatientmonitoring.model.DailyHealthLog;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.model.EmergencyGuide;
import com.rpm.remotepatientmonitoring.config.CustomUserDetails;
import com.rpm.remotepatientmonitoring.repository.HealthLogRepository;
import com.rpm.remotepatientmonitoring.repository.PatientRepository;
import com.rpm.remotepatientmonitoring.repository.EmergencyGuideRepository;
import com.rpm.remotepatientmonitoring.service.patient.PatientHealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/patient/health")
public class PatientHealthController {

    @Autowired
    private PatientHealthService healthService;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private HealthLogRepository healthLogRepository;

    @Autowired
    private EmergencyGuideRepository emergencyGuideRepository;

    private Patient getCurrentPatient() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            Object principal = auth.getPrincipal();
            if (principal instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) principal;
                Optional<Patient> opt = patientRepository.findByAccountId(userDetails.getAccount().getId());
                if (opt.isPresent()) {
                    return opt.get();
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
                List<Patient> allPatients = patientRepository.findAll();
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

            int level = 1; // 1 = Green, 2 = Yellow, 3 = Orange, 4 = Red
            Integer latestSystolic = null;
            Integer latestDiastolic = null;
            Double latestGlucose = null;

            Optional<DailyHealthLog> latestLogOpt = healthLogRepository.findFirstByPatientIdOrderByLogTimeDesc(patient.getId());
            if (latestLogOpt.isPresent()) {
                DailyHealthLog log = latestLogOpt.get();
                if (log.getSystolicBp() != null) {
                    latestSystolic = log.getSystolicBp();
                    if (latestSystolic >= 180) {
                        level = Math.max(level, 4);
                    } else if (latestSystolic >= 140) {
                        level = Math.max(level, 3);
                    } else if (latestSystolic >= 130) {
                        level = Math.max(level, 2);
                    }
                }
                if (log.getDiastolicBp() != null) {
                    latestDiastolic = log.getDiastolicBp();
                    if (latestDiastolic >= 110) {
                        level = Math.max(level, 4);
                    } else if (latestDiastolic >= 90) {
                        level = Math.max(level, 3);
                    } else if (latestDiastolic >= 85) {
                        level = Math.max(level, 2);
                    }
                }
                if (log.getGlucoseLevel() != null) {
                    latestGlucose = log.getGlucoseLevel().doubleValue();
                    if (latestGlucose < 4.4 || latestGlucose > 16.0) {
                        level = Math.max(level, 4);
                    } else if (latestGlucose > 10.0) {
                        level = Math.max(level, 3);
                    }
                }
            }

            boolean isEmergency = (level >= 3);

            List<Map<String, Object>> guidesList = new ArrayList<>();
            if (patient.getHospital() != null) {
                List<EmergencyGuide> dbGuides = 
                    emergencyGuideRepository.findByHospitalIdAndIsActive(patient.getHospital().getId(), true);
                for (EmergencyGuide g : dbGuides) {
                    Map<String, Object> gMap = new HashMap<>();
                    gMap.put("title", g.getTitle());
                    gMap.put("content", g.getInstructionContent());
                    gMap.put("alertLevel", g.getAlertLevel());
                    gMap.put("metricType", g.getMetricType());
                    guidesList.add(gMap);
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("isEmergency", isEmergency);
            response.put("level", level);
            response.put("latestSystolic", latestSystolic);
            response.put("latestDiastolic", latestDiastolic);
            response.put("latestGlucose", latestGlucose);
            response.put("emergencyContactName", patient.getEmergencyContactName() != null ? patient.getEmergencyContactName() : "Chưa thiết lập");
            response.put("emergencyContactPhone", patient.getEmergencyContactPhone() != null ? patient.getEmergencyContactPhone() : "");
            response.put("guides", guidesList);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}