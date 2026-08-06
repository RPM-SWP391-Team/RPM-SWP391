package com.rpm.remotepatientmonitoring.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.model.Doctor;
import com.rpm.remotepatientmonitoring.model.DailyHealthLog;

@Entity
@Table(name = "alerts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Alert {
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
    @JoinColumn(name = "health_log_id")
    private DailyHealthLog healthLog;
    @Column(name = "alert_level", nullable = false)
    private Integer alertLevel;
    @Column(name = "alert_color", nullable = false, length = 10)
    private String alertColor;
    @Column(name = "metric_type", nullable = false, length = 30)
    private String metricType;
    @Column(name = "metric_value", nullable = false, length = 200)
    private String metricValue;
    @Column(name = "threshold_violated", nullable = false, length = 200)
    private String thresholdViolated;
    @Column(name = "alert_message", nullable = false, length = 500)
    private String alertMessage;
    @Column(name = "is_resolved", nullable = false)
    private Boolean isResolved = false;
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    @ManyToOne
    @JoinColumn(name = "resolved_by_doctor_id")
    private Doctor resolvedByDoctor;
    @Column(name = "resolution_notes", length = 500)
    private String resolutionNotes;
    @Column(name = "triggered_at", nullable = false)
    private LocalDateTime triggeredAt = LocalDateTime.now();
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public DailyHealthLog getHealthLog() {
        return healthLog;
    }

    public void setHealthLog(DailyHealthLog healthLog) {
        this.healthLog = healthLog;
    }

    public Integer getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(Integer alertLevel) {
        this.alertLevel = alertLevel;
    }

    public String getAlertColor() {
        return alertColor;
    }

    public void setAlertColor(String alertColor) {
        this.alertColor = alertColor;
    }

    public String getMetricType() {
        return metricType;
    }

    public void setMetricType(String metricType) {
        this.metricType = metricType;
    }

    public String getMetricValue() { 
        if (metricValue != null && !"N/A".equalsIgnoreCase(metricValue) && !metricValue.trim().isEmpty()) {
            return metricValue;
        }
        if (healthLog != null) {
            if ("BLOOD_PRESSURE".equalsIgnoreCase(metricType) || healthLog.getSystolicBp() != null || healthLog.getDiastolicBp() != null) {
                String sys = healthLog.getSystolicBp() != null ? String.valueOf(healthLog.getSystolicBp()) : "?";
                String dia = healthLog.getDiastolicBp() != null ? String.valueOf(healthLog.getDiastolicBp()) : "?";
                return sys + "/" + dia;
            } else if ("GLUCOSE".equalsIgnoreCase(metricType) || healthLog.getGlucoseLevel() != null) {
                if (healthLog.getGlucoseLevel() != null) {
                    return String.valueOf(healthLog.getGlucoseLevel());
                }
            }
        }
        if (alertMessage != null && alertMessage.contains("(") && alertMessage.contains(")")) {
            try {
                int start = alertMessage.indexOf("(");
                int end = alertMessage.indexOf(")", start);
                if (start < end) {
                    String extracted = alertMessage.substring(start + 1, end).replace("mmol/L", "").trim();
                    if (!extracted.isEmpty()) {
                        return extracted;
                    }
                }
            } catch (Exception ignored) {}
        }
        return metricValue != null ? metricValue : "N/A"; 
    }

    public void setMetricValue(String metricValue) {
        this.metricValue = metricValue;
    }

    public String getThresholdViolated() {
        return thresholdViolated;
    }

    public void setThresholdViolated(String thresholdViolated) {
        this.thresholdViolated = thresholdViolated;
    }

    public String getAlertMessage() {
        return alertMessage;
    }

    public void setAlertMessage(String alertMessage) {
        this.alertMessage = alertMessage;
    }

    public Boolean getIsResolved() {
        return isResolved;
    }

    public void setIsResolved(Boolean isResolved) {
        this.isResolved = isResolved;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public Doctor getResolvedByDoctor() {
        return resolvedByDoctor;
    }

    public void setResolvedByDoctor(Doctor resolvedByDoctor) {
        this.resolvedByDoctor = resolvedByDoctor;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }

    public LocalDateTime getTriggeredAt() {
        return triggeredAt;
    }

    public void setTriggeredAt(LocalDateTime triggeredAt) {
        this.triggeredAt = triggeredAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}