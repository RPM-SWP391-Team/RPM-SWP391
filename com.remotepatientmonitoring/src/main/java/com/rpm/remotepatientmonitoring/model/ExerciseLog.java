package com.rpm.remotepatientmonitoring.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "exercise_logs")
public class ExerciseLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(name = "exercise_type", nullable = false, length = 100)
    private String exerciseType;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "steps_count")
    private Integer stepsCount;

    @Column(name = "calories_burned")
    private Double caloriesBurned;

    @Column(name = "logged_at", nullable = false)
    private LocalDateTime loggedAt;

    // Constructors
    public ExerciseLog() {
        this.loggedAt = LocalDateTime.now();
    }

    public ExerciseLog(Patient patient, LocalDate logDate, String exerciseType, Integer durationMinutes, Integer stepsCount, Double caloriesBurned) {
        this.patient = patient;
        this.logDate = logDate;
        this.exerciseType = exerciseType;
        this.durationMinutes = durationMinutes;
        this.stepsCount = stepsCount;
        this.caloriesBurned = caloriesBurned;
        this.loggedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public LocalDate getLogDate() {
        return logDate;
    }

    public void setLogDate(LocalDate logDate) {
        this.logDate = logDate;
    }

    public String getExerciseType() {
        return exerciseType;
    }

    public void setExerciseType(String exerciseType) {
        this.exerciseType = exerciseType;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Integer getStepsCount() {
        return stepsCount;
    }

    public void setStepsCount(Integer stepsCount) {
        this.stepsCount = stepsCount;
    }

    public Double getCaloriesBurned() {
        return caloriesBurned;
    }

    public void setCaloriesBurned(Double caloriesBurned) {
        this.caloriesBurned = caloriesBurned;
    }

    public LocalDateTime getLoggedAt() {
        return loggedAt;
    }

    public void setLoggedAt(LocalDateTime loggedAt) {
        this.loggedAt = loggedAt;
    }
}
