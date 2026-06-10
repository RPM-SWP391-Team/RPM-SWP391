package com.rpm.remotepatientmonitoring.model.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "alert_thresholds")
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
    private String scope = "HOSPITAL";

    @Column(name = "metric_type", nullable = false, length = 50)
    private String metricType;

    @Column(name = "glucose_normal_min", precision = 6, scale = 2)
    private BigDecimal glucoseNormalMin;

    @Column(name = "glucose_normal_max", precision = 6, scale = 2)
    private BigDecimal glucoseNormalMax;

    @Column(name = "glucose_warning_min", precision = 6, scale = 2)
    private BigDecimal glucoseWarningMin;

    @Column(name = "glucose_warning_max", precision = 6, scale = 2)
    private BigDecimal glucoseWarningMax;

    @Column(name = "glucose_treating_min", precision = 6, scale = 2)
    private BigDecimal glucoseTreatingMin;

    @Column(name = "glucose_treating_max", precision = 6, scale = 2)
    private BigDecimal glucoseTreatingMax;

    @Column(name = "glucose_danger_threshold", precision = 6, scale = 2)
    private BigDecimal glucoseDangerThreshold;

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

    @ManyToOne
    @JoinColumn(name = "created_by_doctor_id")
    private Doctor createdByDoctor;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}