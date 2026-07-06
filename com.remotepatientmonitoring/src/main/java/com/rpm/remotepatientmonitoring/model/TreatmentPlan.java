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
}
