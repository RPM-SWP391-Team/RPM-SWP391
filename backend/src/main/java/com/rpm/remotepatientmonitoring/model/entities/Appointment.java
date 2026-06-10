package com.rpm.remotepatientmonitoring.model.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "appointment_time", nullable = false)
    private LocalDateTime appointmentTime;

    @Column(nullable = false, length = 15)
    private String status = "PENDING";

    @Column(name = "appointment_type", nullable = false, length = 20)
    private String appointmentType = "CHECKUP";

    @Column(name = "created_by", nullable = false, length = 10)
    private String createdBy = "PATIENT";

    @Column(name = "patient_requested_time")
    private LocalDateTime patientRequestedTime;

    @Column(name = "patient_request_reason", columnDefinition = "NVARCHAR(MAX)")
    private String patientRequestReason;

    @Column(name = "doctor_note", columnDefinition = "NVARCHAR(MAX)")
    private String doctorNote;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "reminder_sent_2days", nullable = false)
    private Boolean reminderSent2days = false;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

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