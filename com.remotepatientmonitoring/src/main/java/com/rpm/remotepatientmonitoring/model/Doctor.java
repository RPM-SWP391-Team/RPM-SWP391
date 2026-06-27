package com.rpm.remotepatientmonitoring.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "doctors")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    private Account account;

    @ManyToOne
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @Column(name = "doctor_code", nullable = false, unique = true, length = 50)
    private String doctorCode;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(nullable = false, unique = true, length = 20)
    private String phone;

    @Column(length = 255)
    private String specialty;

    @Column(length = 10)
    private String gender;

    @Column(name = "capacity_limit", nullable = false)
    @Builder.Default
    private Integer capacityLimit = 50;

    @Column(name = "current_patient_count", nullable = false)
    @Builder.Default
    private Integer currentPatientCount = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
