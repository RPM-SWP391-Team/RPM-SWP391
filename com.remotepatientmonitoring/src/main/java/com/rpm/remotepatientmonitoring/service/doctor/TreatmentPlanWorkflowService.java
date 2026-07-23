package com.rpm.remotepatientmonitoring.service.doctor;

import com.rpm.remotepatientmonitoring.model.*;
import com.rpm.remotepatientmonitoring.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TreatmentPlanWorkflowService {

    @Autowired
    private TreatmentPlanRepository treatmentPlanRepository;

    @Autowired
    private NutritionRuleRepository nutritionRuleRepository;

    @Autowired
    private PatientMedicationRepository patientMedicationRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private ClinicalRecordRepository clinicalRecordRepository;

    @Autowired
    private HealthLogRepository healthLogRepository;

    @Autowired
    private com.rpm.remotepatientmonitoring.service.patient.PatientHealthService patientHealthService;

    @Autowired
    private AuditTrailService auditTrailService;

    @Transactional
    public void createNewTreatmentPlan(
            Patient patient,
            Doctor doctor,
            // Baseline Vitals
            Integer baselineSystolicBp,
            Integer baselineDiastolicBp,
            BigDecimal baselineFastingGlucose,
            BigDecimal baselineHba1c,
            BigDecimal baselineWeightKg,
            BigDecimal heightCm,
            BigDecimal bmi,
            // Target Vitals
            Integer targetSystolicBp,
            Integer targetDiastolicBp,
            BigDecimal targetFastingGlucose,
            BigDecimal targetHba1c,
            BigDecimal targetWeightKg,
            // Orders & Goals
            String medicalOrder,
            String exerciseGoal,
            String additionalNotes,
            // Nutrition Rules
            Integer maxCaloriesPerDay,
            BigDecimal maxCarbsG,
            BigDecimal maxSaltG,
            BigDecimal minFiberG,
            BigDecimal maxFatG,
            BigDecimal minProteinG,
            Integer dailyWaterMl,
            String nutritionNotes,
            // Medications
            List<String> medNames,
            List<String> medDosages,
            List<String> medScheduledTimes
    ) {
        LocalDateTime now = LocalDateTime.now();

        // 1. Lấy và lưu phác đồ điều trị cũ trước khi vô hiệu hóa
        TreatmentPlan oldPlan = treatmentPlanRepository.findByPatientIdAndIsCurrent(patient.getId(), true).orElse(null);
        if (oldPlan != null) {
            oldPlan.setIsCurrent(false);
            oldPlan.setUpdatedAt(now);
            treatmentPlanRepository.save(oldPlan);
        }

        // 2. Vô hiệu hóa quy tắc dinh dưỡng cũ
        nutritionRuleRepository.findByPatientIdAndIsCurrent(patient.getId(), true)
                .ifPresent(oldRule -> {
                    oldRule.setIsCurrent(false);
                    oldRule.setUpdatedAt(now);
                    nutritionRuleRepository.save(oldRule);
                });

        // 3. Vô hiệu hóa các thuốc cũ của bệnh nhân
        List<PatientMedication> oldMeds = patientMedicationRepository.findByPatientIdAndIsActiveTrue(patient.getId());
        for (PatientMedication oldMed : oldMeds) {
            oldMed.setIsActive(false);
            oldMed.setUpdatedAt(now);
        }
        patientMedicationRepository.saveAll(oldMeds);

        // 4. Tạo và lưu Quy tắc dinh dưỡng mới
        NutritionRule newRule = new NutritionRule();
        newRule.setPatient(patient);
        newRule.setDoctor(doctor);
        newRule.setMaxCaloriesPerDay(maxCaloriesPerDay);
        newRule.setMaxCarbsG(maxCarbsG);
        newRule.setMaxSaltG(maxSaltG);
        newRule.setMinFiberG(minFiberG);
        newRule.setMaxFatG(maxFatG);
        newRule.setMinProteinG(minProteinG);
        newRule.setDailyWaterMl(dailyWaterMl);
        newRule.setAdditionalNotes(nutritionNotes);
        newRule.setIsCurrent(true);
        newRule.setCreatedAt(now);
        newRule.setUpdatedAt(now);
        NutritionRule savedRule = nutritionRuleRepository.save(newRule);

        // 5. Tạo và lưu Phác đồ điều trị mới
        TreatmentPlan newPlan = new TreatmentPlan();
        newPlan.setPatient(patient);
        newPlan.setDoctor(doctor);
        newPlan.setNutritionRule(savedRule);

        // Lưu ClinicalRecord (Hồ sơ khám bệnh) để ánh xạ với bảng clinical_records
        boolean isInitial = !clinicalRecordRepository.existsByPatientId(patient.getId());
        ClinicalRecord record = ClinicalRecord.builder()
                .patient(patient)
                .doctor(doctor)
                .examinationDate(now)
                .weightKg(baselineWeightKg)
                .heightCm(heightCm)
                .bmi(bmi)
                .systolicBp(baselineSystolicBp)
                .diastolicBp(baselineDiastolicBp)
                .fastingGlucose(baselineFastingGlucose)
                .hba1c(baselineHba1c)
                .isInitialExam(isInitial)
                .createdAt(now)
                .updatedAt(now)
                .build();
        clinicalRecordRepository.save(record);
        
        // Log baseline metrics to DailyHealthLog for charts and history
        if (baselineSystolicBp != null || baselineDiastolicBp != null || baselineFastingGlucose != null) {
            DailyHealthLog healthLog = DailyHealthLog.builder()
                    .patient(patient)
                    .logDate(now.toLocalDate())
                    .logTime(now)
                    .logType("RANDOM")
                    .systolicBp(baselineSystolicBp)
                    .diastolicBp(baselineDiastolicBp)
                    .glucoseLevel(baselineFastingGlucose)
                    .inputMethod("MANUAL")
                    .patientNotes("Chỉ số nền từ phác đồ điều trị mới")
                    .isAlertProcessed(false)
                    .isOcrValidated(false)
                    .build();
            healthLogRepository.save(healthLog);
            
            com.rpm.remotepatientmonitoring.dto.patient.HealthLogRequest alertReq = new com.rpm.remotepatientmonitoring.dto.patient.HealthLogRequest();
            alertReq.setPatientId(patient.getId());
            alertReq.setLogType("RANDOM");
            alertReq.setInputMethod("MANUAL");
            alertReq.setSystolicBp(baselineSystolicBp);
            alertReq.setDiastolicBp(baselineDiastolicBp);
            alertReq.setGlucoseLevel(baselineFastingGlucose);
            patientHealthService.evaluateAndGenerateAlerts(alertReq);
        }

        newPlan.setBaselineSystolicBp(baselineSystolicBp);
        newPlan.setBaselineDiastolicBp(baselineDiastolicBp);
        newPlan.setBaselineFastingGlucose(baselineFastingGlucose);
        newPlan.setBaselineHba1c(baselineHba1c);
        newPlan.setBaselineWeightKg(baselineWeightKg);

        newPlan.setTargetSystolicBp(targetSystolicBp);
        newPlan.setTargetDiastolicBp(targetDiastolicBp);
        newPlan.setTargetFastingGlucose(targetFastingGlucose);
        newPlan.setTargetHba1c(targetHba1c);
        newPlan.setTargetWeightKg(targetWeightKg);

        newPlan.setMedicalOrder(medicalOrder);
        newPlan.setExerciseGoal(exerciseGoal);
        newPlan.setAdditionalNotes(additionalNotes);

        newPlan.setIsCurrent(true);
        newPlan.setEffectiveFrom(LocalDate.now());
        newPlan.setCreatedAt(now);
        newPlan.setUpdatedAt(now);

        TreatmentPlan savedPlan = treatmentPlanRepository.save(newPlan);

        // 6. Tạo và lưu danh sách Thuốc mới
        if (medNames != null && !medNames.isEmpty()) {
            for (int i = 0; i < medNames.size(); i++) {
                String name = medNames.get(i);
                if (name != null && !name.trim().isEmpty()) {
                    String dosage = (medDosages != null && i < medDosages.size()) ? medDosages.get(i) : "";
                    String time = (medScheduledTimes != null && i < medScheduledTimes.size()) ? medScheduledTimes.get(i) : "";

                    PatientMedication med = new PatientMedication();
                    med.setPatient(patient);
                    med.setMedicineName(name);
                    med.setDosage(dosage);
                    med.setScheduledTime(time);
                    med.setIsActive(true);
                    med.setCreatedAt(now);
                    med.setUpdatedAt(now);
                    patientMedicationRepository.save(med);
                }
            }
        }

        // 7. Tạo thông báo mới cho bệnh nhân
        Notification notification = new Notification();
        notification.setPatient(patient);
        notification.setRecipientType("PATIENT");
        notification.setRecipientId(patient.getId());
        notification.setNotificationType("SYSTEM");
        notification.setChannel("IN_APP");
        notification.setStatus("SENT");
        notification.setTitle(oldPlan != null ? "Cập nhật phác đồ điều trị mới" : "Phác đồ điều trị mới");
        notification.setContent("Bác sĩ " + doctor.getFullName() + " vừa " + (oldPlan != null ? "cập nhật" : "tạo mới") + " phác đồ điều trị & danh mục thuốc của bạn. Vui lòng kiểm tra.");
        notification.setIsRead(false);
        notification.setCreatedAt(now);
        notificationRepository.save(notification);

        // Cập nhật trạng thái bệnh nhân thành TREATING nếu đang là NEW
        if ("NEW".equals(patient.getStatus())) {
            patient.setStatus("TREATING");
            patient.setUpdatedAt(now);
            patientRepository.save(patient);
        }
    }
}
