package com.rpm.remotepatientmonitoring.model.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "system_logs")
public class SystemLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "log_level", nullable = false, length = 10)
    private String logLevel;

    @Column(name = "module_name", nullable = false, length = 100)
    private String moduleName;

    @Column(name = "event_code", length = 100)
    private String eventCode;

    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String message;

    @Column(name = "related_patient_id")
    private Integer relatedPatientId;

    @Column(name = "related_doctor_id")
    private Integer relatedDoctorId;

    @Column(name = "related_entity_type", length = 50)
    private String relatedEntityType;

    @Column(name = "related_entity_id")
    private Integer relatedEntityId;

    @Column(name = "additional_data", columnDefinition = "NVARCHAR(MAX)")
    private String additionalData;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}