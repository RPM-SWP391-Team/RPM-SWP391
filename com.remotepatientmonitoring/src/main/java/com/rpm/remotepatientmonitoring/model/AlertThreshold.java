package com.rpm.remotepatientmonitoring.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "alert_thresholds")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertThreshold {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String scope = "HOSPITAL";

    @Column(name = "metric_type", nullable = false, length = 50)
    private String metricType;

    // --- Glucose thresholds (Giữ nguyên trường, xóa @NotNull và @DecimalMin) ---
    @Column(name = "glucose_normal_min")
    private BigDecimal glucoseNormalMin;

    @Column(name = "glucose_normal_max")
    private BigDecimal glucoseNormalMax;

    @Column(name = "glucose_warning_min")
    private BigDecimal glucoseWarningMin;

    @Column(name = "glucose_warning_max")
    private BigDecimal glucoseWarningMax;

    @Column(name = "glucose_treating_min")
    private BigDecimal glucoseTreatingMin;

    @Column(name = "glucose_treating_max")
    private BigDecimal glucoseTreatingMax;

    @Column(name = "glucose_danger_threshold")
    private BigDecimal glucoseDangerThreshold;

    // --- Systolic Blood Pressure (Giữ nguyên trường, xóa @NotNull và @Min) ---
    @Column(name = "systolic_normal_max")
    private Integer systolicNormalMax;

    @Column(name = "systolic_prehypertension_min")
    private Integer systolicPrehypertensionMin;

    @Column(name = "systolic_prehypertension_max")
    private Integer systolicPrehypertensionMax;

    @Column(name = "systolic_hypertension_min")
    private Integer systolicHypertensionMin;

    @Column(name = "systolic_hypertension_max")
    private Integer systolicHypertensionMax;

    @Column(name = "systolic_danger_threshold")
    private Integer systolicDangerThreshold;

    @Column(name = "systolic_emergency_threshold")
    private Integer systolicEmergencyThreshold;

    // --- Diastolic Blood Pressure (Giữ nguyên trường, xóa @NotNull và @Min) ---
    @Column(name = "diastolic_normal_max")
    private Integer diastolicNormalMax;

    @Column(name = "diastolic_hypertension_min")
    private Integer diastolicHypertensionMin;

    @Column(name = "diastolic_hypertension_max")
    private Integer diastolicHypertensionMax;

    @Column(name = "diastolic_danger_threshold")
    private Integer diastolicDangerThreshold;

    @Column(name = "diastolic_emergency_threshold")
    private Integer diastolicEmergencyThreshold;

    @Column(name = "created_by_doctor_id")
    private Integer createdByDoctorId;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}