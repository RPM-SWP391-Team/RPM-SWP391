package com.rpm.remotepatientmonitoring.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "exercise_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientExercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(name = "exercise_type", nullable = false, length = 100)
    private String exerciseType; // e.g. Đi bộ, Chạy bộ...

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "steps_count")
    private Integer stepsCount; // Số bước chân

    @Column(name = "logged_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // Transient fields to keep compatibility with old controller/UI
    @Transient
    private Integer caloriesBurned;

    @Transient
    private String notes;

    // Custom getters to calculate values dynamically
    public Integer getCaloriesBurned() {
        if (stepsCount != null) {
            double c = stepsCount * 0.04;
            return (int) c;
        }
        return caloriesBurned;
    }

    public String getNotes() {
        if (stepsCount != null) {
            return exerciseType + " (" + stepsCount + " bước)";
        }
        return notes;
    }
}
