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
    private PatientRepository patientRepository;

    @Autowired
    private NutritionRuleRepository nutritionRuleRepository;

    @Autowired
    private PatientMedicationRepository patientMedicationRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AuditTrailService auditTrailService;

    @Transactional
    public void createNewTreatmentPlan(
            Patient patient,
            Doctor doctor,
            // Baseline metrics
            Integer baselineSystolicBp,
            Integer baselineDiastolicBp,
            BigDecimal baselineFastingGlucose,
            BigDecimal baselineHba1c,
            BigDecimal baselineWeightKg,
            // Target metrics
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

        // 1. Vô hiệu hóa phác đồ điều trị hiện hành cũ
        treatmentPlanRepository.findByPatientIdAndIsCurrent(patient.getId(), true)
                .ifPresent(oldPlan -> {
                    oldPlan.setIsCurrent(false);
                    oldPlan.setUpdatedAt(now);
                    treatmentPlanRepository.save(oldPlan);
                });

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
        newRule.setEffectiveFrom(LocalDate.now());
        newRule.setCreatedAt(now);
        newRule.setUpdatedAt(now);
        nutritionRuleRepository.save(newRule);

        // 5. Tạo và lưu Phác đồ điều trị mới
        TreatmentPlan newPlan = new TreatmentPlan();
        newPlan.setPatient(patient);
        newPlan.setDoctor(doctor);
        newPlan.setNutritionRule(newRule);
        // baseline (đo tại viện)
        newPlan.setBaselineSystolicBp(baselineSystolicBp);
        newPlan.setBaselineDiastolicBp(baselineDiastolicBp);
        newPlan.setBaselineFastingGlucose(baselineFastingGlucose);
        newPlan.setBaselineHba1c(baselineHba1c);
        newPlan.setBaselineWeightKg(baselineWeightKg);
        // target
        newPlan.setTargetSystolicBp(targetSystolicBp);
        newPlan.setTargetDiastolicBp(targetDiastolicBp);
        newPlan.setTargetFastingGlucose(targetFastingGlucose);
        newPlan.setTargetHba1c(targetHba1c);
        newPlan.setTargetWeightKg(targetWeightKg);
        // goals
        newPlan.setMedicalOrder(medicalOrder);
        newPlan.setExerciseGoal(exerciseGoal);
        newPlan.setAdditionalNotes(additionalNotes);
        newPlan.setIsCurrent(true);
        newPlan.setEffectiveFrom(LocalDate.now());
        newPlan.setCreatedAt(now);
        newPlan.setUpdatedAt(now);
        treatmentPlanRepository.save(newPlan);

        // 6. Tạo và lưu danh sách thuốc mới liên kết với phác đồ
        if (medNames != null) {
            for (int i = 0; i < medNames.size(); i++) {
                String name = medNames.get(i);
                if (name != null && !name.trim().isEmpty()) {
                    PatientMedication med = new PatientMedication();
                    med.setPatient(patient);
                    med.setTreatmentPlan(newPlan);
                    med.setMedicineName(name.trim());
                    med.setDosage(medDosages.get(i));
                    med.setScheduledTime(medScheduledTimes.get(i));
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
        notification.setTitle("Cập nhật phác đồ điều trị mới");
        notification.setContent("Bác sĩ " + doctor.getFullName() + " vừa cập nhật phác đồ điều trị & danh mục thuốc của bạn. Vui lòng kiểm tra.");
        notification.setIsRead(false);
        notification.setCreatedAt(now);
        notification.setPatient(patient);
        notificationRepository.save(notification);

        // Cập nhật trạng thái bệnh nhân thành TREATING nếu đang là NEW
        if ("NEW".equals(patient.getStatus())) {
            patient.setStatus("TREATING");
            patient.setUpdatedAt(now);
            patientRepository.save(patient);
        }

        // 8. Ghi Audit Trail
        auditTrailService.logAction(
                "DOCTOR",
                doctor.getId(),
                "CREATE_TREATMENT_PLAN",
                "treatment_plans",
                newPlan.getId(),
                null,
                newPlan,
                "Bác sĩ " + doctor.getFullName() + " tạo/cập nhật phác đồ mới cho bệnh nhân " + patient.getFullName()
        );
    }
}
