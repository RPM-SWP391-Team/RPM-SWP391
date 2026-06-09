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
    @Column(name = "is_medication_taken")
    private Boolean isMedicationTaken;
    @Column(name = "is_water_intake_done")
    private Boolean isWaterIntakeDone;
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
}
