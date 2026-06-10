package com.rpm.remotepatientmonitoring.model.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "ai_health_summaries")
public class AiHealthSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "summary_date", nullable = false)
    private LocalDate summaryDate;

    @Column(name = "summary_type", nullable = false, length = 10)
    private String summaryType = "WEEKLY";

    @Column(name = "summary_content", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String summaryContent;

    @Column(name = "compliance_rate", precision = 5, scale = 2)
    private BigDecimal complianceRate;

    @Column(name = "medication_compliance_rate", precision = 5, scale = 2)
    private BigDecimal medicationComplianceRate;

    @Column(name = "trend_glucose", length = 15)
    private String trendGlucose;

    @Column(name = "trend_blood_pressure", length = 15)
    private String trendBloodPressure;

    @Column(name = "doctor_feedback", columnDefinition = "NVARCHAR(MAX)")
    private String doctorFeedback;

    @Column(name = "is_accurate_feedback")
    private Boolean isAccurateFeedback;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        generatedAt = LocalDateTime.now();
    }
}