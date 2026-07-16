package com.rpm.remotepatientmonitoring.service.patient;

import com.rpm.remotepatientmonitoring.dto.patient.HealthLogRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;
import java.util.Map;
@Service
public class PatientHealthService {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private com.rpm.remotepatientmonitoring.repository.PatientRepository patientRepository;

    @Autowired
    private com.rpm.remotepatientmonitoring.repository.AlertRepository alertRepository;

    @Autowired
    private com.rpm.remotepatientmonitoring.repository.NotificationRepository notificationRepository;

    @Autowired
    private com.rpm.remotepatientmonitoring.repository.HealthLogRepository healthLogRepository;

    @Autowired
    private com.rpm.remotepatientmonitoring.repository.EmergencyGuideRepository emergencyGuideRepository;

    @Autowired
    private com.rpm.remotepatientmonitoring.repository.EmergencyProtocolRepository emergencyProtocolRepository;

    public Map<String, Object> submitDailyHealthLog(HealthLogRequest req) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate).withProcedureName("sp_record_daily_health_log");
        MapSqlParameterSource inParams = new MapSqlParameterSource();
        inParams.addValue("patient_id", req.getPatientId());
        inParams.addValue("log_type", req.getLogType());
        inParams.addValue("input_method", req.getInputMethod());
        inParams.addValue("systolic_bp", req.getSystolicBp());
        inParams.addValue("diastolic_bp", req.getDiastolicBp());
        inParams.addValue("heart_rate", req.getHeartRate());
        inParams.addValue("glucose_level", req.getGlucoseLevel());
        inParams.addValue("image_url", req.getImageUrl());
        inParams.addValue("patient_notes", req.getPatientNotes());
        
        Map<String, Object> result = jdbcCall.execute(inParams);
        
        String message = (String) result.get("result_message");
        if (message != null && !message.startsWith("Lỗi")) {
            evaluateAndGenerateAlerts(req);
        }
        
        return result;
    }

    private void evaluateAndGenerateAlerts(HealthLogRequest req) {
        com.rpm.remotepatientmonitoring.model.Patient patient = patientRepository.findById(req.getPatientId()).orElse(null);
        if (patient == null || patient.getDoctor() == null) return;
        
        com.rpm.remotepatientmonitoring.model.Doctor doctor = patient.getDoctor();
        com.rpm.remotepatientmonitoring.model.DailyHealthLog latestLog = healthLogRepository.findFirstByPatientIdOrderByLogTimeDesc(req.getPatientId()).orElse(null);
        
        int systolicLevel = 1;
        int diastolicLevel = 1;
        int glucoseLevel = 1;
        
        String alertMessage = "";
        String metricType = "";
        String metricValue = "";
        String thresholdViolated = "";
        
        if (req.getSystolicBp() != null) {
            int sys = req.getSystolicBp();
            if (sys >= 180) { systolicLevel = 3; alertMessage += "Huyết áp tâm thu quá cao (" + sys + "). "; metricType = "BLOOD_PRESSURE"; metricValue = sys + "/" + (req.getDiastolicBp() != null ? req.getDiastolicBp() : "?"); thresholdViolated = ">=180"; }
            else if (sys >= 140) { systolicLevel = 2; alertMessage += "Huyết áp tâm thu cao (" + sys + "). "; metricType = "BLOOD_PRESSURE"; metricValue = sys + "/" + (req.getDiastolicBp() != null ? req.getDiastolicBp() : "?"); thresholdViolated = ">=140"; }
        }
        
        if (req.getDiastolicBp() != null) {
            int dia = req.getDiastolicBp();
            if (dia >= 120) { diastolicLevel = 3; alertMessage += "Huyết áp tâm trương quá cao (" + dia + "). "; metricType = "BLOOD_PRESSURE"; thresholdViolated = ">=120"; }
            else if (dia >= 90) { diastolicLevel = 2; alertMessage += "Huyết áp tâm trương cao (" + dia + "). "; metricType = "BLOOD_PRESSURE"; thresholdViolated = ">=90"; }
        }
        
        if (req.getGlucoseLevel() != null) {
            double glu = req.getGlucoseLevel().doubleValue();
            if (glu < 4.4) { glucoseLevel = 3; alertMessage += "Hạ đường huyết (" + glu + "). "; metricType = "GLUCOSE"; metricValue = String.valueOf(glu); thresholdViolated = "<4.4"; }
            else if (glu > 16.0) { glucoseLevel = 3; alertMessage += "Đường huyết quá cao (" + glu + "). "; metricType = "GLUCOSE"; metricValue = String.valueOf(glu); thresholdViolated = ">16.0"; }
            else if (glu > 10.0) { glucoseLevel = 2; alertMessage += "Đường huyết cao (" + glu + "). "; metricType = "GLUCOSE"; metricValue = String.valueOf(glu); thresholdViolated = ">10.0"; }
        }
        
        int finalLevel = Math.max(systolicLevel, Math.max(diastolicLevel, glucoseLevel));
        
        if (finalLevel >= 2) {
            com.rpm.remotepatientmonitoring.model.Alert alert = new com.rpm.remotepatientmonitoring.model.Alert();
            alert.setPatient(patient);
            alert.setDoctor(doctor);
            alert.setHealthLog(latestLog);
            alert.setAlertLevel(finalLevel);
            alert.setAlertColor(finalLevel == 3 ? "RED" : "ORANGE");
            alert.setMetricType(metricType.isEmpty() ? "UNKNOWN" : metricType);
            alert.setMetricValue(metricValue.isEmpty() ? "N/A" : metricValue);
            alert.setThresholdViolated(thresholdViolated.isEmpty() ? "N/A" : thresholdViolated);
            alert.setAlertMessage(alertMessage.trim());
            alert.setIsResolved(false);
            alert.setTriggeredAt(java.time.LocalDateTime.now());
            alert.setCreatedAt(java.time.LocalDateTime.now());
            
            alertRepository.save(alert);
            
            com.rpm.remotepatientmonitoring.model.Notification notif = new com.rpm.remotepatientmonitoring.model.Notification();
            notif.setPatient(patient);
            notif.setDoctor(doctor);
            notif.setRecipientType("DOCTOR");
            notif.setRecipientId(doctor.getId());
            notif.setNotificationType("ALERT");
            notif.setChannel("IN_APP");
            notif.setStatus("SENT");
            notif.setTitle("Cảnh báo Y tế từ bệnh nhân " + patient.getFullName());
            notif.setContent("Phát hiện chỉ số bất thường mức " + alert.getAlertColor() + ". Vui lòng kiểm tra ngay!");
            notif.setIsRead(false);
            notif.setCreatedAt(java.time.LocalDateTime.now());
            
            notificationRepository.save(notif);
        }
    }

    public void saveDailyHealthLog(com.rpm.remotepatientmonitoring.model.Patient patient, com.rpm.remotepatientmonitoring.dto.patient.DailyHealthLogFormDto formDto) {
        com.rpm.remotepatientmonitoring.model.DailyHealthLog log = com.rpm.remotepatientmonitoring.model.DailyHealthLog.builder()
                .patient(patient)
                .logDate(java.time.LocalDate.now())
                .logTime(java.time.LocalDateTime.now())
                .logType(formDto.getLogType())
                .systolicBp(formDto.getSystolicBp())
                .diastolicBp(formDto.getDiastolicBp())
                .heartRate(formDto.getHeartRate())
                .glucoseLevel(formDto.getGlucoseLevel())
                .patientNotes(formDto.getPatientNotes())
                .inputMethod("MANUAL")
                .isOcrValidated(false)
                .isAlertProcessed(false)
                .createdAt(java.time.LocalDateTime.now())
                .build();
        
        healthLogRepository.save(log);
    }

    public com.rpm.remotepatientmonitoring.model.Patient getPatientByAccountId(Integer accountId) {
        return patientRepository.findByAccountId(accountId).orElse(null);
    }

    public java.util.List<com.rpm.remotepatientmonitoring.model.Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public Map<String, Object> getEmergencyStatus(com.rpm.remotepatientmonitoring.model.Patient patient) {
        int level = 1; // 1 = Green, 2 = Yellow, 3 = Orange, 4 = Red
        Integer latestSystolic = null;
        Integer latestDiastolic = null;
        Double latestGlucose = null;

        java.util.Optional<com.rpm.remotepatientmonitoring.model.DailyHealthLog> latestBpLogOpt = 
            healthLogRepository.findFirstByPatientIdAndSystolicBpIsNotNullOrderByLogTimeDesc(patient.getId());
        if (latestBpLogOpt.isPresent()) {
            com.rpm.remotepatientmonitoring.model.DailyHealthLog bpLog = latestBpLogOpt.get();
            if (bpLog.getSystolicBp() != null) {
                latestSystolic = bpLog.getSystolicBp();
                if (latestSystolic >= 180 || latestSystolic < 90) {
                    level = Math.max(level, 4);
                } else if (latestSystolic >= 140) {
                    level = Math.max(level, 3);
                } else if (latestSystolic >= 130) {
                    level = Math.max(level, 2);
                }
            }
            if (bpLog.getDiastolicBp() != null) {
                latestDiastolic = bpLog.getDiastolicBp();
                if (latestDiastolic >= 110 || latestDiastolic < 60) {
                    level = Math.max(level, 4);
                } else if (latestDiastolic >= 90) {
                    level = Math.max(level, 3);
                } else if (latestDiastolic >= 85) {
                    level = Math.max(level, 2);
                }
            }
        }

        java.util.Optional<com.rpm.remotepatientmonitoring.model.DailyHealthLog> latestGlucoseLogOpt = 
            healthLogRepository.findFirstByPatientIdAndGlucoseLevelIsNotNullOrderByLogTimeDesc(patient.getId());
        if (latestGlucoseLogOpt.isPresent()) {
            com.rpm.remotepatientmonitoring.model.DailyHealthLog glucoseLog = latestGlucoseLogOpt.get();
            if (glucoseLog.getGlucoseLevel() != null) {
                latestGlucose = glucoseLog.getGlucoseLevel().doubleValue();
                if (latestGlucose < 4.4 || latestGlucose > 16.0) {
                    level = Math.max(level, 4);
                } else if (latestGlucose > 10.0) {
                    level = Math.max(level, 3);
                }
            }
        }

        boolean isEmergency = (level >= 3);

        java.util.List<Map<String, Object>> guidesList = new java.util.ArrayList<>();
        if (patient.getHospital() != null) {
            java.util.List<com.rpm.remotepatientmonitoring.model.EmergencyGuide> dbGuides = 
                emergencyGuideRepository.findByHospitalIdAndIsActive(patient.getHospital().getId(), true);
            for (com.rpm.remotepatientmonitoring.model.EmergencyGuide g : dbGuides) {
                Map<String, Object> gMap = new java.util.HashMap<>();
                gMap.put("title", g.getTitle());
                gMap.put("content", g.getInstructionContent());
                gMap.put("alertLevel", g.getAlertLevel());
                gMap.put("metricType", g.getMetricType());
                guidesList.add(gMap);
            }
        }

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("isEmergency", isEmergency);
        response.put("level", level);
        response.put("latestSystolic", latestSystolic);
        response.put("latestDiastolic", latestDiastolic);
        response.put("latestGlucose", latestGlucose);
        response.put("emergencyContactName", patient.getEmergencyContactName() != null ? patient.getEmergencyContactName() : "Chưa thiết lập");
        response.put("emergencyContactPhone", patient.getEmergencyContactPhone() != null ? patient.getEmergencyContactPhone() : "");
        response.put("guides", guidesList);

        return response;
    }

    public java.util.List<com.rpm.remotepatientmonitoring.model.EmergencyProtocol> getEmergencyProtocols(Integer hospitalId) {
        return emergencyProtocolRepository.findByHospitalIdAndIsActiveTrue(hospitalId);
    }
}
