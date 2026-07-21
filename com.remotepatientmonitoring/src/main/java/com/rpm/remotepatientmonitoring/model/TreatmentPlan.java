package com.rpm.remotepatientmonitoring.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "treatment_plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TreatmentPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne
    @JoinColumn(name = "nutrition_rule_id")
    private NutritionRule nutritionRule;

    @Column(name = "target_systolic_bp")
    private Integer targetSystolicBp;

    @Column(name = "target_diastolic_bp")
    private Integer targetDiastolicBp;

    @Column(name = "target_fasting_glucose", precision = 6, scale = 2)
    private BigDecimal targetFastingGlucose;

    @Column(name = "target_hba1c", precision = 5, scale = 2)
    private BigDecimal targetHba1c;

    @Column(name = "target_weight_kg", precision = 5, scale = 2)
    private BigDecimal targetWeightKg;

    @Column(name = "target_steps")
    private Integer targetSteps;

    @Column(name = "target_sleep_hours", precision = 4, scale = 2)
    private BigDecimal targetSleepHours;

    @Column(name = "baseline_systolic_bp")
    private Integer baselineSystolicBp;

    @Column(name = "baseline_diastolic_bp")
    private Integer baselineDiastolicBp;

    @Column(name = "baseline_fasting_glucose", precision = 6, scale = 2)
    private BigDecimal baselineFastingGlucose;

    @Column(name = "baseline_hba1c", precision = 5, scale = 2)
    private BigDecimal baselineHba1c;

    @Column(name = "baseline_weight_kg", precision = 5, scale = 2)
    private BigDecimal baselineWeightKg;

    @Column(name = "medical_order", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String medicalOrder;

    @Column(name = "exercise_goal", length = 500)
    private String exerciseGoal;

    @Column(name = "additional_notes", columnDefinition = "NVARCHAR(MAX)")
    private String additionalNotes;

    @Column(name = "is_current", nullable = false)
    @Builder.Default
    private Boolean isCurrent = true;

    @Column(name = "effective_from", nullable = false)
    @Builder.Default
    private LocalDate effectiveFrom = LocalDate.now();

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }
    public NutritionRule getNutritionRule() { return nutritionRule; }
    public void setNutritionRule(NutritionRule nutritionRule) { this.nutritionRule = nutritionRule; }
    public Integer getTargetSystolicBp() { return targetSystolicBp; }
    public void setTargetSystolicBp(Integer targetSystolicBp) { this.targetSystolicBp = targetSystolicBp; }
    public Integer getTargetDiastolicBp() { return targetDiastolicBp; }
    public void setTargetDiastolicBp(Integer targetDiastolicBp) { this.targetDiastolicBp = targetDiastolicBp; }
    public BigDecimal getTargetFastingGlucose() { return targetFastingGlucose; }
    public void setTargetFastingGlucose(BigDecimal targetFastingGlucose) { this.targetFastingGlucose = targetFastingGlucose; }
    public BigDecimal getTargetHba1c() { return targetHba1c; }
    public void setTargetHba1c(BigDecimal targetHba1c) { this.targetHba1c = targetHba1c; }
    public BigDecimal getTargetWeightKg() { return targetWeightKg; }
    public void setTargetWeightKg(BigDecimal targetWeightKg) { this.targetWeightKg = targetWeightKg; }
    public Integer getTargetSteps() { return targetSteps; }
    public void setTargetSteps(Integer targetSteps) { this.targetSteps = targetSteps; }
    public BigDecimal getTargetSleepHours() { return targetSleepHours; }
    public void setTargetSleepHours(BigDecimal targetSleepHours) { this.targetSleepHours = targetSleepHours; }
    public Integer getBaselineSystolicBp() { return baselineSystolicBp; }
    public void setBaselineSystolicBp(Integer baselineSystolicBp) { this.baselineSystolicBp = baselineSystolicBp; }
    public Integer getBaselineDiastolicBp() { return baselineDiastolicBp; }
    public void setBaselineDiastolicBp(Integer baselineDiastolicBp) { this.baselineDiastolicBp = baselineDiastolicBp; }
    public BigDecimal getBaselineFastingGlucose() { return baselineFastingGlucose; }
    public void setBaselineFastingGlucose(BigDecimal baselineFastingGlucose) { this.baselineFastingGlucose = baselineFastingGlucose; }
    public BigDecimal getBaselineHba1c() { return baselineHba1c; }
    public void setBaselineHba1c(BigDecimal baselineHba1c) { this.baselineHba1c = baselineHba1c; }
    public BigDecimal getBaselineWeightKg() { return baselineWeightKg; }
    public void setBaselineWeightKg(BigDecimal baselineWeightKg) { this.baselineWeightKg = baselineWeightKg; }
    public String getMedicalOrder() { return medicalOrder; }
    public void setMedicalOrder(String medicalOrder) { this.medicalOrder = medicalOrder; }
    public String getExerciseGoal() { return exerciseGoal; }
    public void setExerciseGoal(String exerciseGoal) { this.exerciseGoal = exerciseGoal; }
    public String getAdditionalNotes() { return additionalNotes; }
    public void setAdditionalNotes(String additionalNotes) { this.additionalNotes = additionalNotes; }
    public Boolean getIsCurrent() { return isCurrent; }
    public void setIsCurrent(Boolean isCurrent) { this.isCurrent = isCurrent; }
    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(LocalDate effectiveFrom) { this.effectiveFrom = effectiveFrom; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
