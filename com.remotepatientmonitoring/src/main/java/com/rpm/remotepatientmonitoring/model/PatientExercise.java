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

    // Helper getters to compute values dynamically for UI templates
    public Integer getCaloriesBurned() {
        if (stepsCount != null) {
            double c = stepsCount * 0.04;
            return (int) c;
        }
        return 0;
    }

    public String getNotes() {
        if (stepsCount != null) {
            return exerciseType + " (" + stepsCount + " bước)";
        }
        return "";
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public LocalDate getLogDate() { return logDate; }
    public void setLogDate(LocalDate logDate) { this.logDate = logDate; }
    public String getExerciseType() { return exerciseType; }
    public void setExerciseType(String exerciseType) { this.exerciseType = exerciseType; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public Integer getStepsCount() { return stepsCount; }
    public void setStepsCount(Integer stepsCount) { this.stepsCount = stepsCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
