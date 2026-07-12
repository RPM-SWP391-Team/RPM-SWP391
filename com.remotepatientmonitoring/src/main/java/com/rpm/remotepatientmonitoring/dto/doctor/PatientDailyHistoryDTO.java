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
}
