package com.rpm.remotepatientmonitoring.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "emergency_guides")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyGuide {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @Column(name = "alert_level", nullable = false, length = 10)
    private String alertLevel; // 'GREEN', 'YELLOW', 'ORANGE', 'RED', 'CRITICAL'

    @Column(name = "metric_type", nullable = false, length = 30)
    private String metricType; // 'GLUCOSE', 'BLOOD_PRESSURE', 'BOTH'

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "instruction_content", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String instructionContent;

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
