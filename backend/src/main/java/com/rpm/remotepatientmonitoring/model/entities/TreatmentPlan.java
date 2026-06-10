package com.rpm.remotepatientmonitoring.model.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "treatment_plans")
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

    @Column(name = "medical_order", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String medicalOrder;

    @Column(name = "exercise_goal", length = 500)
    private String exerciseGoal;

    @Column(name = "additional_notes", columnDefinition = "NVARCHAR(MAX)")
    private String additionalNotes;

    @Column(name = "is_current", nullable = false)
    private Boolean isCurrent = true;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (effectiveFrom == null) effectiveFrom = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}