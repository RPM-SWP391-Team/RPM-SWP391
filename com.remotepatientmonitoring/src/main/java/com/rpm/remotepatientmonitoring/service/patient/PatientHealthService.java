package com.rpm.remotepatientmonitoring.service.patient;

import com.rpm.remotepatientmonitoring.dto.patient.DailyHealthLogFormDto;
import com.rpm.remotepatientmonitoring.model.Alert;
import com.rpm.remotepatientmonitoring.model.AlertThreshold;
import com.rpm.remotepatientmonitoring.model.DailyHealthLog;
import com.rpm.remotepatientmonitoring.model.Doctor;
import com.rpm.remotepatientmonitoring.model.EmergencyGuide;
import com.rpm.remotepatientmonitoring.model.EmergencyProtocol;
import com.rpm.remotepatientmonitoring.model.Notification;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.repository.AlertRepository;
import com.rpm.remotepatientmonitoring.repository.AlertThresholdRepository;
import com.rpm.remotepatientmonitoring.repository.EmergencyGuideRepository;
import com.rpm.remotepatientmonitoring.repository.EmergencyProtocolRepository;
import com.rpm.remotepatientmonitoring.repository.HealthLogRepository;
import com.rpm.remotepatientmonitoring.repository.NotificationRepository;
import com.rpm.remotepatientmonitoring.repository.PatientRepository;
import java.util.*;

import com.rpm.remotepatientmonitoring.dto.patient.HealthLogRequest;
import com.rpm.remotepatientmonitoring.service.doctor.AuditTrailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class PatientHealthService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private AlertThresholdRepository alertThresholdRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private HealthLogRepository healthLogRepository;

    @Autowired
    private EmergencyGuideRepository emergencyGuideRepository;

    @Autowired
    private EmergencyProtocolRepository emergencyProtocolRepository;

    @Autowired
    private AuditTrailService auditTrailService;

    public Map<String, Object> submitDailyHealthLog(HealthLogRequest req) {
        if (req.getSystolicBp() != null && req.getDiastolicBp() != null) {
            if (req.getSystolicBp() <= req.getDiastolicBp()) {
                throw new IllegalArgumentException("Huyết áp tâm thu phải lớn hơn tâm trương!");
            }
        }

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
        Patient patient = patientRepository.findById(req.getPatientId()).orElse(null);
        if (patient == null || patient.getDoctor() == null)
            return;

        Doctor doctor = patient.getDoctor();
        DailyHealthLog latestLog = healthLogRepository.findFirstByPatientIdOrderByLogTimeDesc(req.getPatientId())
                .orElse(null);

        Optional<AlertThreshold> patientThresholdOpt = alertThresholdRepository.findByPatientIdAndScope(patient.getId(),
                "PATIENT");
        AlertThreshold threshold = null;
        if (patientThresholdOpt.isPresent()) {
            threshold = patientThresholdOpt.get();
        } else if (doctor.getHospital() != null) {
            Optional<AlertThreshold> hospitalThresholdOpt = alertThresholdRepository
                    .findByHospitalIdAndScope(doctor.getHospital().getId(), "HOSPITAL");
            if (hospitalThresholdOpt.isPresent()) {
                threshold = hospitalThresholdOpt.get();
            }
        }

        int systolicLevel = 1;
        int diastolicLevel = 1;
        int glucoseLevel = 1;

        String alertMessage = "";
        String metricType = "";
        String metricValue = "";
        String thresholdViolated = "";

        if (req.getSystolicBp() != null) {
            int sys = req.getSystolicBp();
            int warnMin = threshold != null && threshold.getSystolicWarningMin() != null
                    ? threshold.getSystolicWarningMin()
                    : 120;
            int dangMin = threshold != null && threshold.getSystolicDangerMin() != null
                    ? threshold.getSystolicDangerMin()
                    : 140;
            int emerg = threshold != null && threshold.getSystolicEmergencyThreshold() != null
                    ? threshold.getSystolicEmergencyThreshold()
                    : 160;

            if (sys >= emerg) {
                systolicLevel = 4;
                alertMessage += "Huyết áp tâm thu nguy kịch (" + sys + "). ";
                metricType = "BLOOD_PRESSURE";
                metricValue = sys + "/" + (req.getDiastolicBp() != null ? req.getDiastolicBp() : "?");
                thresholdViolated = ">=" + emerg;
            } else if (sys >= dangMin) {
                systolicLevel = 3;
                alertMessage += "Huyết áp tâm thu nguy cơ cao (" + sys + "). ";
                metricType = "BLOOD_PRESSURE";
                metricValue = sys + "/" + (req.getDiastolicBp() != null ? req.getDiastolicBp() : "?");
                thresholdViolated = ">=" + dangMin;
            } else if (sys >= warnMin) {
                systolicLevel = 2;
                alertMessage += "Huyết áp tâm thu chú ý (" + sys + "). ";
                metricType = "BLOOD_PRESSURE";
                metricValue = sys + "/" + (req.getDiastolicBp() != null ? req.getDiastolicBp() : "?");
                thresholdViolated = ">=" + warnMin;
            }
        }

        if (req.getDiastolicBp() != null) {
            int dia = req.getDiastolicBp();
            int warnMin = threshold != null && threshold.getDiastolicWarningMin() != null
                    ? threshold.getDiastolicWarningMin()
                    : 80;
            int dangMin = threshold != null && threshold.getDiastolicDangerMin() != null
                    ? threshold.getDiastolicDangerMin()
                    : 90;
            int emerg = threshold != null && threshold.getDiastolicEmergencyThreshold() != null
                    ? threshold.getDiastolicEmergencyThreshold()
                    : 100;

            if (dia >= emerg) {
                diastolicLevel = 4;
                alertMessage += "Huyết áp tâm trương nguy kịch (" + dia + "). ";
                metricType = "BLOOD_PRESSURE";
                thresholdViolated = ">=" + emerg;
            } else if (dia >= dangMin) {
                diastolicLevel = 3;
                alertMessage += "Huyết áp tâm trương nguy cơ cao (" + dia + "). ";
                metricType = "BLOOD_PRESSURE";
                thresholdViolated = ">=" + dangMin;
            } else if (dia >= warnMin) {
                diastolicLevel = 2;
                alertMessage += "Huyết áp tâm trương chú ý (" + dia + "). ";
                metricType = "BLOOD_PRESSURE";
                thresholdViolated = ">=" + warnMin;
            }
        }

        if (req.getGlucoseLevel() != null) {
            double glu = req.getGlucoseLevel().doubleValue();
            double hypo = threshold != null && threshold.getGlucoseHypoThreshold() != null
                    ? threshold.getGlucoseHypoThreshold().doubleValue()
                    : 4.4;
            double highMax = threshold != null && threshold.getGlucoseHighMax() != null
                    ? threshold.getGlucoseHighMax().doubleValue()
                    : 16.0;
            double normMax = threshold != null && threshold.getGlucoseNormalMax() != null
                    ? threshold.getGlucoseNormalMax().doubleValue()
                    : 10.0;

            if (glu < hypo) {
                glucoseLevel = 4;
                alertMessage += "Hạ đường huyết (" + glu + " mmol/L). ";
                metricType = "GLUCOSE";
                metricValue = String.valueOf(glu);
                thresholdViolated = "<" + hypo;
            } else if (glu >= highMax) {
                glucoseLevel = 4;
                alertMessage += "Đường huyết khẩn cấp (" + glu + " mmol/L). ";
                metricType = "GLUCOSE";
                metricValue = String.valueOf(glu);
                thresholdViolated = ">=" + highMax;
            } else if (glu > normMax) {
                glucoseLevel = 2;
                alertMessage += "Đường huyết cao (" + glu + " mmol/L). ";
                metricType = "GLUCOSE";
                metricValue = String.valueOf(glu);
                thresholdViolated = ">" + normMax;
            }
        }

        int finalLevel = Math.max(systolicLevel, Math.max(diastolicLevel, glucoseLevel));

        String alertColor = "GREEN";
        if (finalLevel == 4)
            alertColor = "RED";
        else if (finalLevel == 3)
            alertColor = "ORANGE";
        else if (finalLevel == 2)
            alertColor = "YELLOW";

        if (latestLog != null) {
            latestLog.setAlertLevel(alertColor);
            healthLogRepository.save(latestLog);
        }

        if (finalLevel >= 2) {
            Alert alert = new Alert();
            alert.setPatient(patient);
            alert.setDoctor(doctor);
            alert.setHealthLog(latestLog);
            alert.setAlertLevel(finalLevel);

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
                Notification notif = new Notification();
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
    public void saveDailyHealthLog(Integer id, Patient patient, DailyHealthLogFormDto formDto) {
        DailyHealthLog log;
        if (id != null) {
            log = healthLogRepository.findById(id).orElse(null);
            if (log != null) {
                // Clear old alerts and notifications first since they are about to be
                // re-evaluated
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

    private DailyHealthLog createNewDailyHealthLogEntity(Patient patient, DailyHealthLogFormDto formDto) {
        return DailyHealthLog.builder()
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

    public List<DailyHealthLog> getRecentDailyHealthLogs(Integer patientId) {
        return healthLogRepository.findByPatientIdOrderByLogTimeDesc(patientId);
    }

    public DailyHealthLog getDailyHealthLogById(Integer id) {
        return healthLogRepository.findById(id).orElse(null);
    }

    public Patient getPatientByAccountId(Integer accountId) {
        return patientRepository.findByAccountId(accountId).orElse(null);
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public void saveDailyHealthLog(Patient patient, DailyHealthLogFormDto formDto) {
        if (patient == null || formDto == null)
            return;
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

    public Map<String, Object> getEmergencyStatus(Patient patient) {
        Map<String, Object> res = new java.util.HashMap<>();
        if (patient == null) {
            res.put("isEmergency", false);
            res.put("level", 1);
            res.put("emergencyContactName", "Chưa thiết lập");
            res.put("emergencyContactPhone", "");
            res.put("guides", List.of());
            return res;
        }

        // Fetch threshold for accurate level computation
        AlertThreshold threshold = getAlertThresholdByPatientOrHospital(
                patient.getId(),
                patient.getHospital() != null ? patient.getHospital().getId() : null);

        // Fetch latest BP and Glucose logs
        DailyHealthLog latestBpLog = healthLogRepository
                .findFirstByPatientIdAndSystolicBpIsNotNullOrderByLogTimeDesc(patient.getId()).orElse(null);
        DailyHealthLog latestGlLog = healthLogRepository
                .findFirstByPatientIdAndGlucoseLevelIsNotNullOrderByLogTimeDesc(patient.getId()).orElse(null);

        boolean isEmergency = false;
        int level = 1;
        String message = "Chỉ số an toàn";

        // Compute level from actual numeric values (same logic as
        // evaluateAndGenerateAlerts)
        int systolicLevel = 1;
        int diastolicLevel = 1;
        int glucoseLevel = 1;

        if (latestBpLog != null && latestBpLog.getSystolicBp() != null) {
            int sys = latestBpLog.getSystolicBp();
            int warnMin = (threshold != null && threshold.getSystolicWarningMin() != null)
                    ? threshold.getSystolicWarningMin()
                    : 120;
            int dangMin = (threshold != null && threshold.getSystolicDangerMin() != null)
                    ? threshold.getSystolicDangerMin()
                    : 140;
            int emerg = (threshold != null && threshold.getSystolicEmergencyThreshold() != null)
                    ? threshold.getSystolicEmergencyThreshold()
                    : 160;

            if (sys >= emerg) {
                systolicLevel = 4;
            } else if (sys >= dangMin) {
                systolicLevel = 3;
            } else if (sys >= warnMin) {
                systolicLevel = 2;
            }
        }

        if (latestBpLog != null && latestBpLog.getDiastolicBp() != null) {
            int dia = latestBpLog.getDiastolicBp();
            int warnMin = (threshold != null && threshold.getDiastolicWarningMin() != null)
                    ? threshold.getDiastolicWarningMin()
                    : 80;
            int dangMin = (threshold != null && threshold.getDiastolicDangerMin() != null)
                    ? threshold.getDiastolicDangerMin()
                    : 90;
            int emerg = (threshold != null && threshold.getDiastolicEmergencyThreshold() != null)
                    ? threshold.getDiastolicEmergencyThreshold()
                    : 100;

            if (dia >= emerg) {
                diastolicLevel = 4;
            } else if (dia >= dangMin) {
                diastolicLevel = 3;
            } else if (dia >= warnMin) {
                diastolicLevel = 2;
            }
        }

        if (latestGlLog != null && latestGlLog.getGlucoseLevel() != null) {
            double glu = latestGlLog.getGlucoseLevel().doubleValue();
            double hypo = (threshold != null && threshold.getGlucoseHypoThreshold() != null)
                    ? threshold.getGlucoseHypoThreshold().doubleValue()
                    : 4.4;
            double highMax = (threshold != null && threshold.getGlucoseHighMax() != null)
                    ? threshold.getGlucoseHighMax().doubleValue()
                    : 16.0;
            double normMax = (threshold != null && threshold.getGlucoseNormalMax() != null)
                    ? threshold.getGlucoseNormalMax().doubleValue()
                    : 10.0;

            if (glu < hypo) {
                glucoseLevel = 4;
            } else if (glu >= highMax) {
                glucoseLevel = 4;
            } else if (glu > normMax) {
                glucoseLevel = 2;
            }
        }

        level = Math.max(systolicLevel, Math.max(diastolicLevel, glucoseLevel));

        if (level >= 3) {
            isEmergency = true;
            message = "Cảnh báo chỉ số sức khỏe vượt ngưỡng nguy hiểm!";
        } else if (level == 2) {
            message = "Chỉ số sức khỏe cần chú ý theo dõi.";
        }

        res.put("isEmergency", isEmergency);
        res.put("level", level);
        res.put("alertMessage", message);
        res.put("emergencyContactName",
                patient.getEmergencyContactName() != null ? patient.getEmergencyContactName() : "Người thân");
        res.put("emergencyContactPhone",
                patient.getEmergencyContactPhone() != null ? patient.getEmergencyContactPhone() : "");

        // Populate latest BP and Glucose levels for Today's Health Assessment UI
        // (already fetched above)

        if (latestBpLog != null) {
            res.put("latestSystolic", latestBpLog.getSystolicBp());
            res.put("latestDiastolic", latestBpLog.getDiastolicBp());
        } else {
            res.put("latestSystolic", null);
            res.put("latestDiastolic", null);
        }

        if (latestGlLog != null) {
            res.put("latestGlucose", latestGlLog.getGlucoseLevel());
        } else {
            res.put("latestGlucose", null);
        }

        List<Map<String, Object>> guideList = new java.util.ArrayList<>();
        if (patient.getHospital() != null) {
            List<EmergencyGuide> guides = emergencyGuideRepository
                    .findByHospitalIdAndIsActive(patient.getHospital().getId(), true);
            for (EmergencyGuide g : guides) {
                Map<String, Object> gMap = new java.util.HashMap<>();
                gMap.put("title", g.getTitle());
                gMap.put("content", g.getInstructionContent());
                gMap.put("metricType", g.getMetricType());
                guideList.add(gMap);
            }
        }
        res.put("guides", guideList);

        // Fetch latest doctor resolution note for alert handling instructions
        Alert latestDoctorNoteAlert = alertRepository
                .findFirstByPatientIdAndIsResolvedTrueAndResolutionNotesIsNotNullOrderByResolvedAtDesc(patient.getId())
                .orElse(null);

        if (latestDoctorNoteAlert != null && latestDoctorNoteAlert.getResolutionNotes() != null
                && !latestDoctorNoteAlert.getResolutionNotes().trim().isEmpty()) {
            res.put("doctorNote", latestDoctorNoteAlert.getResolutionNotes());
            res.put("doctorName",
                    latestDoctorNoteAlert.getResolvedByDoctor() != null
                            ? latestDoctorNoteAlert.getResolvedByDoctor().getFullName()
                            : "Bác sĩ phụ trách");
            res.put("doctorNoteTime",
                    latestDoctorNoteAlert.getResolvedAt() != null ? latestDoctorNoteAlert.getResolvedAt().toString()
                            : "");
        } else {
            res.put("doctorNote", null);
        }

        return res;
    }

    public List<EmergencyProtocol> getEmergencyProtocols(Integer hospitalId) {
        if (hospitalId == null)
            return List.of();
        return emergencyProtocolRepository.findByHospitalIdAndIsActiveTrue(hospitalId);
    }

    public List<DailyHealthLog> getHealthLogsByPatientIdAndDate(Integer patientId, java.time.LocalDate date) {
        return healthLogRepository.findByPatientIdAndLogDate(patientId, date);
    }

    public AlertThreshold getAlertThresholdByPatientOrHospital(Integer patientId, Integer hospitalId) {
        Optional<AlertThreshold> opt = alertThresholdRepository.findByPatientIdAndScope(patientId, "PATIENT");
        if (opt.isPresent()) {
            return opt.get();
        }
        if (hospitalId != null) {
            Optional<AlertThreshold> hospOpt = alertThresholdRepository.findByHospitalIdAndScope(hospitalId,
                    "HOSPITAL");
            if (hospOpt.isPresent()) {
                return hospOpt.get();
            }
        }
        return null;
    }
}
