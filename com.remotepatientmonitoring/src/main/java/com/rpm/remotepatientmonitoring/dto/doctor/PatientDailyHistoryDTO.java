package com.rpm.remotepatientmonitoring.dto.doctor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientDailyHistoryDTO {
    // Health metrics
    private Integer systolicBp;
    private Integer diastolicBp;
    private BigDecimal glucoseLevel;
    private Integer heartRate;

    // Water
    private Integer totalWaterMl;
    private Integer targetWaterMl;

    // Meals
    private Integer totalMeals;
    private Integer totalMealKcal;
    private List<MealDetailDTO> meals;

    // Exercises
    private Integer totalExercises;
    private Integer totalExerciseDuration;
    private Integer totalExerciseKcal;
    private List<ExerciseDetailDTO> exercises;

    // Medications
    private Integer takenMedicationsCount;
    private Integer totalMedicationsCount;
    private List<MedicationDetailDTO> medications;

    public Integer getSystolicBp() { return systolicBp; }
    public void setSystolicBp(Integer systolicBp) { this.systolicBp = systolicBp; }
    public Integer getDiastolicBp() { return diastolicBp; }
    public void setDiastolicBp(Integer diastolicBp) { this.diastolicBp = diastolicBp; }
    public BigDecimal getGlucoseLevel() { return glucoseLevel; }
    public void setGlucoseLevel(BigDecimal glucoseLevel) { this.glucoseLevel = glucoseLevel; }
    public Integer getHeartRate() { return heartRate; }
    public void setHeartRate(Integer heartRate) { this.heartRate = heartRate; }
    public Integer getTotalWaterMl() { return totalWaterMl; }
    public void setTotalWaterMl(Integer totalWaterMl) { this.totalWaterMl = totalWaterMl; }
    public Integer getTargetWaterMl() { return targetWaterMl; }
    public void setTargetWaterMl(Integer targetWaterMl) { this.targetWaterMl = targetWaterMl; }
    public Integer getTotalMeals() { return totalMeals; }
    public void setTotalMeals(Integer totalMeals) { this.totalMeals = totalMeals; }
    public Integer getTotalMealKcal() { return totalMealKcal; }
    public void setTotalMealKcal(Integer totalMealKcal) { this.totalMealKcal = totalMealKcal; }
    public List<MealDetailDTO> getMeals() { return meals; }
    public void setMeals(List<MealDetailDTO> meals) { this.meals = meals; }
    public Integer getTotalExercises() { return totalExercises; }
    public void setTotalExercises(Integer totalExercises) { this.totalExercises = totalExercises; }
    public Integer getTotalExerciseDuration() { return totalExerciseDuration; }
    public void setTotalExerciseDuration(Integer totalExerciseDuration) { this.totalExerciseDuration = totalExerciseDuration; }
    public Integer getTotalExerciseKcal() { return totalExerciseKcal; }
    public void setTotalExerciseKcal(Integer totalExerciseKcal) { this.totalExerciseKcal = totalExerciseKcal; }
    public List<ExerciseDetailDTO> getExercises() { return exercises; }
    public void setExercises(List<ExerciseDetailDTO> exercises) { this.exercises = exercises; }
    public Integer getTakenMedicationsCount() { return takenMedicationsCount; }
    public void setTakenMedicationsCount(Integer takenMedicationsCount) { this.takenMedicationsCount = takenMedicationsCount; }
    public Integer getTotalMedicationsCount() { return totalMedicationsCount; }
    public void setTotalMedicationsCount(Integer totalMedicationsCount) { this.totalMedicationsCount = totalMedicationsCount; }
    public List<MedicationDetailDTO> getMedications() { return medications; }
    public void setMedications(List<MedicationDetailDTO> medications) { this.medications = medications; }
}
