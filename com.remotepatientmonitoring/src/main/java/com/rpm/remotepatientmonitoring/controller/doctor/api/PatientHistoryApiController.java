package com.rpm.remotepatientmonitoring.controller.doctor.api;

import com.rpm.remotepatientmonitoring.dto.doctor.*;
import com.rpm.remotepatientmonitoring.model.*;
import com.rpm.remotepatientmonitoring.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/doctor/patients/{patientId}/history")
public class PatientHistoryApiController {

    @Autowired
    private HealthLogRepository healthLogRepository;

    @Autowired
    private PatientMealRepository patientMealRepository;

    @Autowired
    private PatientExerciseRepository patientExerciseRepository;

    @Autowired
    private WaterLogRepository waterLogRepository;

    @Autowired
    private MedicationLogRepository medicationLogRepository;

    @Autowired
    private NutritionRuleRepository nutritionRuleRepository;

    @GetMapping("/active-days")
    public ResponseEntity<List<String>> getActiveDays(
            @PathVariable Integer patientId,
            @RequestParam int year,
            @RequestParam int month) {
        
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        Set<LocalDate> activeDates = new HashSet<>();

        // Add all dates that have at least one record
        // We fetch greater than or equal to start date, we will filter by end date in memory for simplicity or just loop days.
        // Actually it's better to fetch logs and extract dates.
        List<DailyHealthLog> healthLogs = healthLogRepository.findByPatientIdAndLogDateGreaterThanEqualOrderByLogDateAsc(patientId, startDate);
        healthLogs.stream().filter(l -> !l.getLogDate().isAfter(endDate)).forEach(l -> activeDates.add(l.getLogDate()));

        List<PatientMeal> meals = patientMealRepository.findAll(); // Optimization needed? We will just get all or filter in memory
        // Better: Use a query or stream filtering if we don't have the exact method.
        // Wait, patientMealRepository doesn't have a date range method.
        // For simplicity, let's just get the methods we have or loop through the month days and check.
        // Since looping 31 days and doing 5 queries per day is 150 queries, it's not ideal but okay for a simple dashboard.
        // Let's optimize: we can just check if any record exists for that date.
        
        for (int i = 1; i <= yearMonth.lengthOfMonth(); i++) {
            LocalDate date = yearMonth.atDay(i);
            if (activeDates.contains(date)) continue;
            
            if (!healthLogRepository.findByPatientIdAndLogDate(patientId, date).isEmpty()) { activeDates.add(date); continue; }
            if (!patientMealRepository.findByPatientIdAndLogDate(patientId, date).isEmpty()) { activeDates.add(date); continue; }
            if (!patientExerciseRepository.findByPatientIdAndLogDate(patientId, date).isEmpty()) { activeDates.add(date); continue; }
            if (waterLogRepository.findByPatientIdAndLogDate(patientId, date).isPresent()) { activeDates.add(date); continue; }
            if (!medicationLogRepository.findByPatientMedicationPatientIdAndLogDate(patientId, date).isEmpty()) { activeDates.add(date); }
        }

        List<String> result = activeDates.stream()
                .map(LocalDate::toString)
                .sorted()
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/day")
    public ResponseEntity<PatientDailyHistoryDTO> getDayDetails(
            @PathVariable Integer patientId,
            @RequestParam String date) {
        
        LocalDate logDate = LocalDate.parse(date);

        PatientDailyHistoryDTO dto = new PatientDailyHistoryDTO();
        
        // 1. Health Logs
        List<DailyHealthLog> healthLogs = healthLogRepository.findByPatientIdAndLogDate(patientId, logDate);
        if (!healthLogs.isEmpty()) {
            // Find max or latest values
            Integer maxSys = healthLogs.stream().map(DailyHealthLog::getSystolicBp).filter(Objects::nonNull).max(Integer::compareTo).orElse(null);
            Integer maxDia = healthLogs.stream().map(DailyHealthLog::getDiastolicBp).filter(Objects::nonNull).max(Integer::compareTo).orElse(null);
            BigDecimal maxGlu = healthLogs.stream().map(DailyHealthLog::getGlucoseLevel).filter(Objects::nonNull).max(BigDecimal::compareTo).orElse(null);
            Integer maxHr = healthLogs.stream().map(DailyHealthLog::getHeartRate).filter(Objects::nonNull).max(Integer::compareTo).orElse(null);
            
            dto.setSystolicBp(maxSys);
            dto.setDiastolicBp(maxDia);
            dto.setGlucoseLevel(maxGlu);
            dto.setHeartRate(maxHr);
        }

        // 2. Meals
        List<PatientMeal> meals = patientMealRepository.findByPatientIdAndLogDate(patientId, logDate);
        dto.setTotalMeals(meals.size());
        dto.setTotalMealKcal(meals.stream().mapToInt(m -> m.getCalories() != null ? m.getCalories() : 0).sum());
        List<MealDetailDTO> mealDTOs = meals.stream().map(m -> MealDetailDTO.builder()
                .mealType(m.getMealType())
                .foodName(m.getFoodName())
                .quantity(m.getQuantityG() != null ? m.getQuantityG().intValue() : 0)
                .totalCalories(m.getCalories() != null ? m.getCalories() : 0)
                .logTime(m.getCreatedAt())
                .build()).collect(Collectors.toList());
        dto.setMeals(mealDTOs);

        // 3. Exercises
        List<PatientExercise> exercises = patientExerciseRepository.findByPatientIdAndLogDate(patientId, logDate);
        dto.setTotalExercises(exercises.size());
        dto.setTotalExerciseDuration(exercises.stream().mapToInt(PatientExercise::getDurationMinutes).sum());
        dto.setTotalExerciseKcal(exercises.stream().mapToInt(PatientExercise::getCaloriesBurned).sum());
        List<ExerciseDetailDTO> exDTOs = exercises.stream().map(e -> ExerciseDetailDTO.builder()
                .exerciseType(e.getExerciseType())
                .durationMinutes(e.getDurationMinutes())
                .caloriesBurned(e.getCaloriesBurned())
                .logTime(e.getCreatedAt())
                .build()).collect(Collectors.toList());
        dto.setExercises(exDTOs);

        // 4. Water
        waterLogRepository.findByPatientIdAndLogDate(patientId, logDate).ifPresent(w -> {
            dto.setTotalWaterMl(w.getAmountMl());
        });
        nutritionRuleRepository.findByPatientIdAndIsCurrent(patientId, true).ifPresent(r -> {
            dto.setTargetWaterMl(r.getDailyWaterMl());
        });

        // 5. Medications
        List<MedicationLog> meds = medicationLogRepository.findByPatientMedicationPatientIdAndLogDate(patientId, logDate);
        dto.setTotalMedicationsCount(meds.size());
        dto.setTakenMedicationsCount((int) meds.stream().filter(MedicationLog::getIsTaken).count());
        List<MedicationDetailDTO> medDTOs = meds.stream().map(m -> MedicationDetailDTO.builder()
                .medicineName(m.getPatientMedication().getMedicineName())
                .dosage(m.getPatientMedication().getDosage())
                .scheduledTime(m.getPatientMedication().getScheduledTime())
                .isTaken(m.getIsTaken())
                .takenAt(m.getTakenAt())
                .build()).collect(Collectors.toList());
        dto.setMedications(medDTOs);

        return ResponseEntity.ok(dto);
    }
}
