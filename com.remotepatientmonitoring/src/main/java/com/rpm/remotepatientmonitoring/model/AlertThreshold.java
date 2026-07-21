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

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Hospital getHospital() { return hospital; }
    public void setHospital(Hospital hospital) { this.hospital = hospital; }
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }
    public String getMetricType() { return metricType; }
    public void setMetricType(String metricType) { this.metricType = metricType; }
    public BigDecimal getGlucoseHypoThreshold() { return glucoseHypoThreshold; }
    public void setGlucoseHypoThreshold(BigDecimal glucoseHypoThreshold) { this.glucoseHypoThreshold = glucoseHypoThreshold; }
    public BigDecimal getGlucoseNormalMax() { return glucoseNormalMax; }
    public void setGlucoseNormalMax(BigDecimal glucoseNormalMax) { this.glucoseNormalMax = glucoseNormalMax; }
    public BigDecimal getGlucoseHighMax() { return glucoseHighMax; }
    public void setGlucoseHighMax(BigDecimal glucoseHighMax) { this.glucoseHighMax = glucoseHighMax; }
    public Integer getSystolicNormalMax() { return systolicNormalMax; }
    public void setSystolicNormalMax(Integer systolicNormalMax) { this.systolicNormalMax = systolicNormalMax; }
    public Integer getSystolicWarningMin() { return systolicWarningMin; }
    public void setSystolicWarningMin(Integer systolicWarningMin) { this.systolicWarningMin = systolicWarningMin; }
    public Integer getSystolicWarningMax() { return systolicWarningMax; }
    public void setSystolicWarningMax(Integer systolicWarningMax) { this.systolicWarningMax = systolicWarningMax; }
    public Integer getSystolicDangerMin() { return systolicDangerMin; }
    public void setSystolicDangerMin(Integer systolicDangerMin) { this.systolicDangerMin = systolicDangerMin; }
    public Integer getSystolicDangerMax() { return systolicDangerMax; }
    public void setSystolicDangerMax(Integer systolicDangerMax) { this.systolicDangerMax = systolicDangerMax; }
    public Integer getSystolicEmergencyThreshold() { return systolicEmergencyThreshold; }
    public void setSystolicEmergencyThreshold(Integer systolicEmergencyThreshold) { this.systolicEmergencyThreshold = systolicEmergencyThreshold; }
    public Integer getDiastolicNormalMax() { return diastolicNormalMax; }
    public void setDiastolicNormalMax(Integer diastolicNormalMax) { this.diastolicNormalMax = diastolicNormalMax; }
    public Integer getDiastolicWarningMin() { return diastolicWarningMin; }
    public void setDiastolicWarningMin(Integer diastolicWarningMin) { this.diastolicWarningMin = diastolicWarningMin; }
    public Integer getDiastolicWarningMax() { return diastolicWarningMax; }
    public void setDiastolicWarningMax(Integer diastolicWarningMax) { this.diastolicWarningMax = diastolicWarningMax; }
    public Integer getDiastolicDangerMin() { return diastolicDangerMin; }
    public void setDiastolicDangerMin(Integer diastolicDangerMin) { this.diastolicDangerMin = diastolicDangerMin; }
    public Integer getDiastolicDangerMax() { return diastolicDangerMax; }
    public void setDiastolicDangerMax(Integer diastolicDangerMax) { this.diastolicDangerMax = diastolicDangerMax; }
    public Integer getDiastolicEmergencyThreshold() { return diastolicEmergencyThreshold; }
    public void setDiastolicEmergencyThreshold(Integer diastolicEmergencyThreshold) { this.diastolicEmergencyThreshold = diastolicEmergencyThreshold; }
    public Integer getCreatedByDoctorId() { return createdByDoctorId; }
    public void setCreatedByDoctorId(Integer createdByDoctorId) { this.createdByDoctorId = createdByDoctorId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}