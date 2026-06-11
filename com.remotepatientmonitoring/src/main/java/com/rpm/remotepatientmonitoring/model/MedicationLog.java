package com.rpm.remotepatientmonitoring.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "medication_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "patient_medication_id", nullable = false)
    private PatientMedication patientMedication;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(name = "is_taken", nullable = false)
    @Builder.Default
    private Boolean isTaken = false;

    @Column(name = "taken_at")
    private LocalDateTime takenAt;
}