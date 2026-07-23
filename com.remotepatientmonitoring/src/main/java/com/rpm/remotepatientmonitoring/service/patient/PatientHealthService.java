package com.rpm.remotepatientmonitoring.service.patient;

import com.rpm.remotepatientmonitoring.dto.patient.HealthLogRequest;
import com.rpm.remotepatientmonitoring.service.doctor.AuditTrailService;
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
    private com.rpm.remotepatientmonitoring.repository.AlertThresholdRepository alertThresholdRepository;

    @Autowired
    private com.rpm.remotepatientmonitoring.repository.NotificationRepository notificationRepository;

    @Autowired
    private com.rpm.remotepatientmonitoring.repository.HealthLogRepository healthLogRepository;

    @Autowired
    private com.rpm.remotepatientmonitoring.repository.EmergencyGuideRepository emergencyGuideRepository;

    @Autowired
    private com.rpm.remotepatientmonitoring.repository.EmergencyProtocolRepository emergencyProtocolRepository;

    @Autowired
    private AuditTrailService auditTrailService;

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

    public void evaluateAndGenerateAlerts(HealthLogRequest req) {
        com.rpm.remotepatientmonitoring.model.Patient patient = patientRepository.findById(req.getPatientId()).orElse(null);
        if (patient == null || patient.getDoctor() == null) return;

        com.rpm.remotepatientmonitoring.model.Doctor doctor = patient.getDoctor();
        com.rpm.remotepatientmonitoring.model.DailyHealthLog latestLog = healthLogRepository.findFirstByPatientIdOrderByLogTimeDesc(req.getPatientId()).orElse(null);

        com.rpm.remotepatientmonitoring.model.AlertThreshold threshold = alertThresholdRepository.findByPatientIdAndScope(patient.getId(), "PATIENT")
                .orElseGet(() -> {
                    if (doctor.getHospital() != null) {
                        return alertThresholdRepository.findByHospitalIdAndScope(doctor.getHospital().getId(), "HOSPITAL").orElse(null);
                    }
                    return null;
                });

        int systolicLevel = 1;
        int diastolicLevel = 1;
        int glucoseLevel = 1;

        String alertMessage = "";
        String metricType = "";
        String metricValue = "";
        String thresholdViolated = "";

        if (req.getSystolicBp() != null) {
            int sys = req.getSystolicBp();
            int warnMin = threshold != null && threshold.getSystolicWarningMin() != null ? threshold.getSystolicWarningMin() : 130;
            int dangMin = threshold != null && threshold.getSystolicDangerMin() != null ? threshold.getSystolicDangerMin() : 140;
            int emerg = threshold != null && threshold.getSystolicEmergencyThreshold() != null ? threshold.getSystolicEmergencyThreshold() : 180;
            
            if (sys >= emerg) { systolicLevel = 4; alertMessage += "Huyết áp tâm thu cấp cứu (" + sys + "). "; metricType = "BLOOD_PRESSURE"; metricValue = sys + "/" + (req.getDiastolicBp() != null ? req.getDiastolicBp() : "?"); thresholdViolated = ">=" + emerg; }
            else if (sys >= dangMin) { systolicLevel = 3; alertMessage += "Huyết áp tâm thu nguy hiểm (" + sys + "). "; metricType = "BLOOD_PRESSURE"; metricValue = sys + "/" + (req.getDiastolicBp() != null ? req.getDiastolicBp() : "?"); thresholdViolated = ">=" + dangMin; }
            else if (sys >= warnMin) { systolicLevel = 2; alertMessage += "Huyết áp tâm thu hơi cao (" + sys + "). "; metricType = "BLOOD_PRESSURE"; metricValue = sys + "/" + (req.getDiastolicBp() != null ? req.getDiastolicBp() : "?"); thresholdViolated = ">=" + warnMin; }
        }

        if (req.getDiastolicBp() != null) {
            int dia = req.getDiastolicBp();
            int warnMin = threshold != null && threshold.getDiastolicWarningMin() != null ? threshold.getDiastolicWarningMin() : 85;
            int dangMin = threshold != null && threshold.getDiastolicDangerMin() != null ? threshold.getDiastolicDangerMin() : 90;
            int emerg = threshold != null && threshold.getDiastolicEmergencyThreshold() != null ? threshold.getDiastolicEmergencyThreshold() : 110;

            if (dia >= emerg) { diastolicLevel = 4; alertMessage += "Huyết áp tâm trương cấp cứu (" + dia + "). "; metricType = "BLOOD_PRESSURE"; thresholdViolated = ">=" + emerg; }
            else if (dia >= dangMin) { diastolicLevel = 3; alertMessage += "Huyết áp tâm trương nguy hiểm (" + dia + "). "; metricType = "BLOOD_PRESSURE"; thresholdViolated = ">=" + dangMin; }
            else if (dia >= warnMin) { diastolicLevel = 2; alertMessage += "Huyết áp tâm trương hơi cao (" + dia + "). "; metricType = "BLOOD_PRESSURE"; thresholdViolated = ">=" + warnMin; }
        }
        
        if (req.getGlucoseLevel() != null) {
            double glu = req.getGlucoseLevel().doubleValue();
            double hypo = threshold != null && threshold.getGlucoseHypoThreshold() != null ? threshold.getGlucoseHypoThreshold().doubleValue() : 4.4;
            double highMax = threshold != null && threshold.getGlucoseHighMax() != null ? threshold.getGlucoseHighMax().doubleValue() : 16.0;
            double normMax = threshold != null && threshold.getGlucoseNormalMax() != null ? threshold.getGlucoseNormalMax().doubleValue() : 10.0;

            if (glu < hypo) { glucoseLevel = 4; alertMessage += "Hạ đường huyết (" + glu + " mmol/L). "; metricType = "GLUCOSE"; metricValue = String.valueOf(glu); thresholdViolated = "<" + hypo; }
            else if (glu > highMax) { glucoseLevel = 4; alertMessage += "Đường huyết khẩn cấp (" + glu + " mmol/L). "; metricType = "GLUCOSE"; metricValue = String.valueOf(glu); thresholdViolated = ">" + highMax; }
            else if (glu > normMax) { glucoseLevel = 2; alertMessage += "Đường huyết cao (" + glu + " mmol/L). "; metricType = "GLUCOSE"; metricValue = String.valueOf(glu); thresholdViolated = ">" + normMax; }
        }
        
        int finalLevel = Math.max(systolicLevel, Math.max(diastolicLevel, glucoseLevel));
        
        if (finalLevel >= 2) {
            com.rpm.remotepatientmonitoring.model.Alert alert = new com.rpm.remotepatientmonitoring.model.Alert();
            alert.setPatient(patient);
            alert.setDoctor(doctor);
            alert.setHealthLog(latestLog);
            alert.setAlertLevel(finalLevel);
            
            String alertColor = "YELLOW";
            if (finalLevel == 4) alertColor = "RED";
            else if (finalLevel == 3) alertColor = "ORANGE";
            
            alert.setAlertColor(alertColor);
            alert.setMetricType(metricType);
            alert.setThresholdViolated(thresholdViolated);
            alert.setMetricValue(metricValue.isEmpty() ? "N/A" : metricValue);
            alert.setAlertMessage(alertMessage.trim());
            alert.setIsResolved(false);
            alert.setTriggeredAt(java.time.LocalDateTime.now());
            alert.setCreatedAt(java.time.LocalDateTime.now());
            
            alertRepository.save(alert);
            
            // Only notify DOCTOR if level >= 3 (Orange/Red)
            if (finalLevel >= 3) {
                com.rpm.remotepatientmonitoring.model.Notification notif = new com.rpm.remotepatientmonitoring.model.Notification();
                notif.setPatient(patient);
                notif.setDoctor(doctor);
                notif.setRecipientType("DOCTOR");
                notif.setRecipientId(doctor.getId());
                notif.setNotificationType("ALERT");
                notif.setChannel("IN_APP");
                notif.setStatus("SENT");
                notif.setTitle("Cảnh báo Y tế từ bệnh nhân " + patient.getFullName());
                notif.setContent("Phát hiện chỉ số bất thường mức " + alertColor + ". Vui lòng kiểm tra ngay!");
                notif.setIsRead(false);
                notif.setCreatedAt(java.time.LocalDateTime.now());
                
                notificationRepository.save(notif);
            }
        }
    }

    @org.springframework.transaction.annotation.Transactional
    public void saveDailyHealthLog(Integer id, com.rpm.remotepatientmonitoring.model.Patient patient, com.rpm.remotepatientmonitoring.dto.patient.DailyHealthLogFormDto formDto) {
        com.rpm.remotepatientmonitoring.model.DailyHealthLog log;
        if (id != null) {
            log = healthLogRepository.findById(id).orElse(null);
            if (log != null) {
                // Clear old alerts and notifications first since they are about to be re-evaluated
                alertRepository.deleteByHealthLogId(id);
                notificationRepository.deleteByDailyHealthLogId(id);
                
                log.setLogType(formDto.getLogType());
                log.setSystolicBp(formDto.getSystolicBp());
                log.setDiastolicBp(formDto.getDiastolicBp());
                log.setHeartRate(formDto.getHeartRate());
                log.setGlucoseLevel(formDto.getGlucoseLevel());
                log.setPatientNotes(formDto.getPatientNotes());
            } else {
                log = createNewDailyHealthLogEntity(patient, formDto);
            }
        } else {
            log = createNewDailyHealthLogEntity(patient, formDto);
        }
        
        healthLogRepository.save(log);
        
        // Evaluate alerts for the log
        HealthLogRequest alertReq = new HealthLogRequest();
        alertReq.setPatientId(patient.getId());
        alertReq.setLogType(log.getLogType());
        alertReq.setInputMethod(log.getInputMethod());
        alertReq.setSystolicBp(log.getSystolicBp());
        alertReq.setDiastolicBp(log.getDiastolicBp());
        alertReq.setHeartRate(log.getHeartRate());
        alertReq.setGlucoseLevel(log.getGlucoseLevel());
        alertReq.setPatientNotes(log.getPatientNotes());
        evaluateAndGenerateAlerts(alertReq);
    }

    private com.rpm.remotepatientmonitoring.model.DailyHealthLog createNewDailyHealthLogEntity(com.rpm.remotepatientmonitoring.model.Patient patient, com.rpm.remotepatientmonitoring.dto.patient.DailyHealthLogFormDto formDto) {
        return com.rpm.remotepatientmonitoring.model.DailyHealthLog.builder()
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
    }

    @org.springframework.transaction.annotation.Transactional
    public void deleteDailyHealthLog(Integer id) {
        alertRepository.deleteByHealthLogId(id);
        notificationRepository.deleteByDailyHealthLogId(id);
        healthLogRepository.deleteById(id);
    }

    public java.util.List<com.rpm.remotepatientmonitoring.model.DailyHealthLog> getRecentDailyHealthLogs(Integer patientId) {
        return healthLogRepository.findByPatientIdOrderByLogTimeDesc(patientId);
    }

    public com.rpm.remotepatientmonitoring.model.DailyHealthLog getDailyHealthLogById(Integer id) {
        return healthLogRepository.findById(id).orElse(null);
    }

    public com.rpm.remotepatientmonitoring.model.Patient getPatientByAccountId(Integer accountId) {
        return patientRepository.findByAccountId(accountId).orElse(null);
    }

    public java.util.List<com.rpm.remotepatientmonitoring.model.Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public void saveDailyHealthLog(com.rpm.remotepatientmonitoring.model.Patient patient, com.rpm.remotepatientmonitoring.dto.patient.DailyHealthLogFormDto formDto) {
        if (patient == null || formDto == null) return;
        HealthLogRequest req = new HealthLogRequest();
        req.setPatientId(patient.getId());
        req.setLogType(formDto.getLogType() != null ? formDto.getLogType() : "RANDOM");
        req.setInputMethod("MANUAL");
        req.setSystolicBp(formDto.getSystolicBp());
        req.setDiastolicBp(formDto.getDiastolicBp());
        req.setGlucoseLevel(formDto.getGlucoseLevel());
        req.setPatientNotes(formDto.getPatientNotes());
        submitDailyHealthLog(req);
    }

    public java.util.Map<String, Object> getEmergencyStatus(com.rpm.remotepatientmonitoring.model.Patient patient) {
        java.util.Map<String, Object> res = new java.util.HashMap<>();
        if (patient == null) {
            res.put("isEmergency", false);
            res.put("level", 1);
            res.put("emergencyContactName", "Chưa thiết lập");
            res.put("emergencyContactPhone", "");
            res.put("guides", java.util.List.of());
            return res;
        }

        com.rpm.remotepatientmonitoring.model.DailyHealthLog latestLog = healthLogRepository.findFirstByPatientIdOrderByLogTimeDesc(patient.getId()).orElse(null);
        boolean isEmergency = false;
        int level = 1;
        String message = "Chỉ số an toàn";

        if (latestLog != null) {
            if ("RED".equals(latestLog.getAlertLevel()) || "ORANGE".equals(latestLog.getAlertLevel())) {
                isEmergency = true;
                level = "RED".equals(latestLog.getAlertLevel()) ? 3 : 2;
                message = "Cảnh báo chỉ số sức khỏe vượt ngưỡng nguy hiểm!";
            }
        }

        res.put("isEmergency", isEmergency);
        res.put("level", level);
        res.put("alertMessage", message);
        res.put("emergencyContactName", patient.getEmergencyContactName() != null ? patient.getEmergencyContactName() : "Người thân");
        res.put("emergencyContactPhone", patient.getEmergencyContactPhone() != null ? patient.getEmergencyContactPhone() : "");

        java.util.List<java.util.Map<String, Object>> guideList = new java.util.ArrayList<>();
        if (patient.getHospital() != null) {
            java.util.List<com.rpm.remotepatientmonitoring.model.EmergencyGuide> guides = 
                emergencyGuideRepository.findByHospitalIdAndIsActive(patient.getHospital().getId(), true);
            for (com.rpm.remotepatientmonitoring.model.EmergencyGuide g : guides) {
                java.util.Map<String, Object> gMap = new java.util.HashMap<>();
                gMap.put("title", g.getTitle());
                gMap.put("content", g.getInstructionContent());
                gMap.put("metricType", g.getMetricType());
                guideList.add(gMap);
            }
        }
        res.put("guides", guideList);

        return res;
    }

    public java.util.List<com.rpm.remotepatientmonitoring.model.EmergencyProtocol> getEmergencyProtocols(Integer hospitalId) {
        if (hospitalId == null) return java.util.List.of();
        return emergencyProtocolRepository.findByHospitalIdAndIsActiveTrue(hospitalId);
    }
}
