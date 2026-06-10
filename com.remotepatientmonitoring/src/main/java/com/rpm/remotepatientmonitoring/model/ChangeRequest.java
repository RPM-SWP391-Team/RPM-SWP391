package com.rpm.remotepatientmonitoring.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "change_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "request_type", nullable = false, length = 20)
    private String requestType;

    @Column(name = "patient_reason", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String patientReason;

    @Column(nullable = false, length = 15)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "doctor_response", columnDefinition = "NVARCHAR(MAX)")
    private String doctorResponse;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
