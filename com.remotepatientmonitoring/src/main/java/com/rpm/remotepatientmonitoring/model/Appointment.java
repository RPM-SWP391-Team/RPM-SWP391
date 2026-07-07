package com.rpm.remotepatientmonitoring.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
@Entity
@Table(name = "appointments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    @Builder.Default
    private String status = "PENDING";
    @Column(name = "appointment_type", nullable = false, length = 20)
    @Builder.Default
    private String appointmentType = "CHECKUP";
    @Column(name = "created_by", nullable = false, length = 10)
    @Builder.Default
    private String createdBy = "PATIENT";
    @Column(name = "patient_requested_time")
    private LocalDateTime patientRequestedTime;
    @Column(name = "patient_request_reason", columnDefinition = "NVARCHAR(MAX)")
    private String patientRequestReason;
    @Column(name = "doctor_note", columnDefinition = "NVARCHAR(MAX)")
    private String doctorNote;
    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;
    
    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "reminder_sent_2days", nullable = false)
    @Builder.Default
    private Boolean reminderSent2days = false;
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
