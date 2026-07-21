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

        return res;
    }

    public java.util.List<com.rpm.remotepatientmonitoring.model.EmergencyProtocol> getEmergencyProtocols(Integer hospitalId) {
        if (hospitalId == null) return java.util.List.of();
        return emergencyProtocolRepository.findByHospitalIdAndIsActiveTrue(hospitalId);
    }
}
