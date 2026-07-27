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

    // Glucose thresholds
    @Column(name = "glucose_hypo_threshold", precision = 4, scale = 2)
    private BigDecimal glucoseHypoThreshold;

    @Column(name = "glucose_normal_max", precision = 4, scale = 2)
    private BigDecimal glucoseNormalMax;

    @Column(name = "glucose_high_max", precision = 4, scale = 2)
    private BigDecimal glucoseHighMax;

    // Systolic Blood Pressure
    @Column(name = "systolic_normal_max")
    private Integer systolicNormalMax;

    @Column(name = "systolic_warning_min")
    private Integer systolicWarningMin;

    @Column(name = "systolic_warning_max")
    private Integer systolicWarningMax;

    @Column(name = "systolic_danger_min")
    private Integer systolicDangerMin;

    @Column(name = "systolic_danger_max")
    private Integer systolicDangerMax;

    @Column(name = "systolic_emergency_threshold")
    private Integer systolicEmergencyThreshold;

    // Diastolic Blood Pressure
    @Column(name = "diastolic_normal_max")
    private Integer diastolicNormalMax;

    @Column(name = "diastolic_warning_min")
    private Integer diastolicWarningMin;

    @Column(name = "diastolic_warning_max")
    private Integer diastolicWarningMax;

    @Column(name = "diastolic_danger_min")
    private Integer diastolicDangerMin;

    @Column(name = "diastolic_danger_max")
    private Integer diastolicDangerMax;

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