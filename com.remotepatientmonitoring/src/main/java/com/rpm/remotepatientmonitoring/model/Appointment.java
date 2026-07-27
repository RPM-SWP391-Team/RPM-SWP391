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

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }
    public LocalDateTime getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(LocalDateTime appointmentTime) { this.appointmentTime = appointmentTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAppointmentType() { return appointmentType; }
    public void setAppointmentType(String appointmentType) { this.appointmentType = appointmentType; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getPatientRequestedTime() { return patientRequestedTime; }
    public void setPatientRequestedTime(LocalDateTime patientRequestedTime) { this.patientRequestedTime = patientRequestedTime; }
    public String getPatientRequestReason() { return patientRequestReason; }
    public void setPatientRequestReason(String patientRequestReason) { this.patientRequestReason = patientRequestReason; }
    public String getDoctorNote() { return doctorNote; }
    public void setDoctorNote(String doctorNote) { this.doctorNote = doctorNote; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Boolean getReminderSent2days() { return reminderSent2days; }
    public void setReminderSent2days(Boolean reminderSent2days) { this.reminderSent2days = reminderSent2days; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
