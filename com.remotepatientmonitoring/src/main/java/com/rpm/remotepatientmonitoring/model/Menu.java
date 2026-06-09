package com.rpm.remotepatientmonitoring.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Entity
@Table(name = "menus")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Menu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;
    @ManyToOne
    @JoinColumn(name = "treatment_plan_id", nullable = false)
    private TreatmentPlan treatmentPlan;
    @ManyToOne
    @JoinColumn(name = "nutrition_rule_id", nullable = false)
    private NutritionRule nutritionRule;
    @Column(name = "week_start_date", nullable = false)
    private LocalDate weekStartDate;
    @Column(name = "week_end_date", nullable = false)
    private LocalDate weekEndDate;
    @Column(nullable = false, length = 10)
    private String status = "DRAFT";
    @Column(name = "ai_generated_content", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String aiGeneratedContent;
    @Column(name = "doctor_approved_content", columnDefinition = "NVARCHAR(MAX)")
    private String doctorApprovedContent;
    @ManyToOne
    @JoinColumn(name = "approved_by_doctor_id")
    private Doctor approvedByDoctor;
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;
    @Column(name = "is_emergency_adjustment", nullable = false)
    private Boolean isEmergencyAdjustment = false;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
