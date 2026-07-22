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
@Table(name = "daily_health_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyHealthLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;
    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;
    @Column(name = "log_time", nullable = false)
    private LocalDateTime logTime;
    @Column(name = "log_type", nullable = false, length = 10)
    private String logType;
    @Column(name = "systolic_bp")
    private Integer systolicBp;
    @Column(name = "diastolic_bp")
    private Integer diastolicBp;
    @Column(name = "heart_rate")
    private Integer heartRate;
    @Column(name = "glucose_level", precision = 6, scale = 2)
    private BigDecimal glucoseLevel;
    @Column(name = "input_method", nullable = false, length = 10)
    @Builder.Default
    private String inputMethod = "MANUAL";
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    @Column(name = "is_ocr_validated", nullable = false)
    @Builder.Default
    private Boolean isOcrValidated = false;
    @Column(name = "alert_level", length = 10)
    private String alertLevel;
    @Column(name = "patient_notes", length = 500)
    private String patientNotes;
    @Column(name = "is_alert_processed", nullable = false)
    @Builder.Default
    private Boolean isAlertProcessed = false;
    @Column(name = "alert_processed_at")
    private LocalDateTime alertProcessedAt;
    @ManyToOne
    @JoinColumn(name = "alert_processed_by")
    private Doctor alertProcessedBy;
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public LocalDate getLogDate() { return logDate; }
    public void setLogDate(LocalDate logDate) { this.logDate = logDate; }
    public LocalDateTime getLogTime() { return logTime; }
    public void setLogTime(LocalDateTime logTime) { this.logTime = logTime; }
    public Integer getSystolicBp() { return systolicBp; }
    public void setSystolicBp(Integer systolicBp) { this.systolicBp = systolicBp; }
    public Integer getDiastolicBp() { return diastolicBp; }
    public void setDiastolicBp(Integer diastolicBp) { this.diastolicBp = diastolicBp; }
    public Integer getHeartRate() { return heartRate; }
    public void setHeartRate(Integer heartRate) { this.heartRate = heartRate; }
    public BigDecimal getGlucoseLevel() { return glucoseLevel; }
    public void setGlucoseLevel(BigDecimal glucoseLevel) { this.glucoseLevel = glucoseLevel; }
    public String getLogType() { return logType; }
    public void setLogType(String logType) { this.logType = logType; }
    public String getInputMethod() { return inputMethod; }
    public void setInputMethod(String inputMethod) { this.inputMethod = inputMethod; }
    public String getPatientNotes() { return patientNotes; }
    public void setPatientNotes(String patientNotes) { this.patientNotes = patientNotes; }
}
