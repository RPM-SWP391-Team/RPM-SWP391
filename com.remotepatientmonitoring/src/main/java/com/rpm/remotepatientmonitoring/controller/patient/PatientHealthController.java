package com.rpm.remotepatientmonitoring.controller.patient;

import com.rpm.remotepatientmonitoring.dto.patient.HealthLogRequest;
import com.rpm.remotepatientmonitoring.model.DailyHealthLog;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.repository.HealthLogRepository;
import com.rpm.remotepatientmonitoring.repository.PatientRepository;
import com.rpm.remotepatientmonitoring.service.patient.PatientHealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
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
            Patient patient = patientRepository.findAll().stream().findFirst().orElse(null);
            if (patient == null) {
                return ResponseEntity.ok(Map.of(
                    "isEmergency", false,
                    "level", 1,
                    "emergencyContactName", "Chưa thiết lập",
                    "emergencyContactPhone", ""
                ));
            }

            List<DailyHealthLog> todayLogs = healthLogRepository.findByPatientIdAndLogDate(patient.getId(), LocalDate.now());
            int level = 1; // 1 = Green/Yellow, 2 = Orange, 3 = Red
            Integer latestSystolic = null;
            Integer latestDiastolic = null;
            Double latestGlucose = null;

            for (DailyHealthLog log : todayLogs) {
                if (log.getSystolicBp() != null) {
                    latestSystolic = log.getSystolicBp();
                    if (latestSystolic >= 180) {
                        level = Math.max(level, 3);
                    } else if (latestSystolic >= 140) {
                        level = Math.max(level, 2);
                    }
                }
                if (log.getDiastolicBp() != null) {
                    latestDiastolic = log.getDiastolicBp();
                    if (latestDiastolic >= 120) {
                        level = Math.max(level, 3);
                    } else if (latestDiastolic >= 90) {
                        level = Math.max(level, 2);
                    }
                }
                if (log.getGlucoseLevel() != null) {
                    latestGlucose = log.getGlucoseLevel().doubleValue();
                    if (latestGlucose > 300.0) {
                        level = Math.max(level, 3);
                    } else if (latestGlucose > 130.0) {
                        level = Math.max(level, 2);
                    }
                }
            }

            if (todayLogs.isEmpty()) {
                Optional<DailyHealthLog> latestLogOpt = healthLogRepository.findFirstByPatientIdOrderByLogTimeDesc(patient.getId());
                if (latestLogOpt.isPresent()) {
                    DailyHealthLog log = latestLogOpt.get();
                    if (log.getSystolicBp() != null) {
                        latestSystolic = log.getSystolicBp();
                        if (latestSystolic >= 180) {
                            level = Math.max(level, 3);
                        } else if (latestSystolic >= 140) {
                            level = Math.max(level, 2);
                        }
                    }
                    if (log.getDiastolicBp() != null) {
                        latestDiastolic = log.getDiastolicBp();
                        if (latestDiastolic >= 120) {
                            level = Math.max(level, 3);
                        } else if (latestDiastolic >= 90) {
                            level = Math.max(level, 2);
                        }
                    }
                    if (log.getGlucoseLevel() != null) {
                        latestGlucose = log.getGlucoseLevel().doubleValue();
                        if (latestGlucose > 300.0) {
                            level = Math.max(level, 3);
                        } else if (latestGlucose > 130.0) {
                            level = Math.max(level, 2);
                        }
                    }
                }
            }

            boolean isEmergency = (level >= 2);

            Map<String, Object> response = new HashMap<>();
            response.put("isEmergency", isEmergency);
            response.put("level", level);
            response.put("latestSystolic", latestSystolic);
            response.put("latestDiastolic", latestDiastolic);
            response.put("latestGlucose", latestGlucose);
            response.put("emergencyContactName", patient.getEmergencyContactName() != null ? patient.getEmergencyContactName() : "Chưa thiết lập");
            response.put("emergencyContactPhone", patient.getEmergencyContactPhone() != null ? patient.getEmergencyContactPhone() : "");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}