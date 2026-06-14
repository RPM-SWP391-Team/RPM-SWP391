package com.rpm.remotepatientmonitoring.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rpm.remotepatientmonitoring.model.*;
import com.rpm.remotepatientmonitoring.repository.HealthLogRepository;
import com.rpm.remotepatientmonitoring.repository.MedicationLogRepository;
import com.rpm.remotepatientmonitoring.repository.PatientMedicationRepository;
import com.rpm.remotepatientmonitoring.repository.PatientRepository;
import com.rpm.remotepatientmonitoring.repository.WaterLogRepository;
import com.rpm.remotepatientmonitoring.repository.PatientMealRepository;
import com.rpm.remotepatientmonitoring.repository.PatientExerciseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/patient")
public class PatientController {

    @Autowired
    private HealthLogRepository healthLogRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PatientMedicationRepository patientMedicationRepository;

    @Autowired
    private MedicationLogRepository medicationLogRepository;

    @Autowired
    private WaterLogRepository waterLogRepository;

    @Autowired
    private PatientMealRepository patientMealRepository;

    @Autowired
    private PatientExerciseRepository patientExerciseRepository;

    @GetMapping("/dashboard")
    public String getDashboard(Model model) {
        // Mock Patient data
        DiseaseProfile profile = DiseaseProfile.builder()
                .profileName("Đồng mắc (Tiểu đường & Tăng huyết áp)")
                .build();

        Patient patient = Patient.builder()
                .fullName("Nguyễn Văn A")
                .diseaseProfile(profile)
                .build();

        // Mock TreatmentPlan data
        TreatmentPlan plan = new TreatmentPlan();
        plan.setMedicalOrder("1. Metformin 500mg: Uống 1 viên sau ăn sáng (8:00) và 1 viên sau ăn tối (20:00)\n" +
                             "2. Amlodipine 5mg: Uống 1 viên vào buổi sáng (8:00)");
        plan.setExerciseGoal("Đi bộ nhẹ nhàng 30 phút mỗi ngày sau bữa ăn tối");

        // Mock Menu data (Today's meal list)
        List<Map<String, Object>> menuList = List.of(
                Map.of(
                        "mealName", "Bữa Sáng",
                        "food", "Cháo yến mạch củ quả nấu ức gà (1 bát), 1 cốc sữa đậu nành không đường (200ml)",
                        "calories", 380,
                        "salt", 0.5,
                        "fiber", 6.5
                ),
                Map.of(
                        "mealName", "Bữa Trưa",
                        "food", "Cơm gạo lứt (1 chén), Cá hồi áp chảo (150g), Súp lơ xanh luộc chấm nước tương nhạt",
                        "calories", 550,
                        "salt", 1.2,
                        "fiber", 8.0
                ),
                Map.of(
                        "mealName", "Bữa Tối",
                        "food", "Canh bí đỏ thịt bằm (ít muối), Đậu hũ nhồi thịt hấp, Salad rau xà lách cà chua bi",
                        "calories", 470,
                        "salt", 0.8,
                        "fiber", 5.5
                )
        );

        // === Medication Tracker: Lấy danh sách thuốc + tính tiến độ hôm nay ===
        int totalMeds = 0;
        int takenMeds = 0;
        int currentWater = 0;
        int targetWater = 2000; // Default target
        try {
            Patient dbPatient = patientRepository.findAll().stream().findFirst().orElse(null);
            if (dbPatient != null) {
                List<PatientMedication> activeMeds = patientMedicationRepository
                        .findByPatientIdAndIsActiveTrue(dbPatient.getId());
                LocalDate today = LocalDate.now();
                totalMeds = activeMeds.size();
                for (PatientMedication med : activeMeds) {
                    boolean taken = medicationLogRepository
                            .findByPatientMedicationIdAndLogDate(med.getId(), today)
                            .map(log -> Boolean.TRUE.equals(log.getIsTaken()))
                            .orElse(false);
                    if (taken) {
                        takenMeds++;
                    }
                }
                currentWater = waterLogRepository.findByPatientIdAndLogDate(dbPatient.getId(), today)
                        .map(WaterLog::getAmountMl)
                        .orElse(0);
            }
        } catch (Exception ignored) {
        }
        int medProgress = totalMeds > 0 ? (takenMeds * 100 / totalMeds) : 0;
        int waterProgress = targetWater > 0 ? (currentWater * 100 / targetWater) : 0;
        if (waterProgress > 100) {
            waterProgress = 100;
        }

        int totalCalories = 0;
        double totalSalt = 0.0;
        double totalFiber = 0.0;
        try {
            Patient dbPatient = patientRepository.findAll().stream().findFirst().orElse(null);
            if (dbPatient != null) {
                LocalDate today = LocalDate.now();
                List<PatientMeal> mealsToday = patientMealRepository.findByPatientIdAndLogDate(dbPatient.getId(), today);
                for (PatientMeal m : mealsToday) {
                    totalCalories += m.getCalories();
                    totalSalt += m.getSaltG();
                    totalFiber += m.getFiberG();
                }
            }
        } catch (Exception ignored) {
        }

        int caloriesPercent = 1400 > 0 ? (totalCalories * 100 / 1400) : 0;
        int saltPercent = 2.5 > 0 ? (int)(totalSalt * 100 / 2.5) : 0;
        int fiberPercent = 20.0 > 0 ? (int)(totalFiber * 100 / 20.0) : 0;

        if (caloriesPercent > 100) caloriesPercent = 100;
        if (saltPercent > 100) saltPercent = 100;
        if (fiberPercent > 100) fiberPercent = 100;

        int totalExerciseMinutes = 0;
        int targetExerciseMinutes = 30; // Default goal
        try {
            Patient dbPatient = patientRepository.findAll().stream().findFirst().orElse(null);
            if (dbPatient != null) {
                LocalDate today = LocalDate.now();
                List<PatientExercise> exercisesToday = patientExerciseRepository.findByPatientIdAndLogDate(dbPatient.getId(), today);
                for (PatientExercise e : exercisesToday) {
                    totalExerciseMinutes += e.getDurationMinutes();
                }
            }
        } catch (Exception ignored) {
        }
        int exerciseProgress = targetExerciseMinutes > 0 ? (totalExerciseMinutes * 100 / targetExerciseMinutes) : 0;
        if (exerciseProgress > 100) {
            exerciseProgress = 100;
        }

        // Daily nutritional targets
        model.addAttribute("patient", patient);
        model.addAttribute("treatmentPlan", plan);
        model.addAttribute("menu", menuList);
        model.addAttribute("targetCalories", 1400);
        model.addAttribute("targetSalt", 2.5);
        model.addAttribute("targetFiber", 20.0);
        model.addAttribute("totalMeds", totalMeds);
        model.addAttribute("takenMeds", takenMeds);
        model.addAttribute("medProgress", medProgress);
        model.addAttribute("currentWater", currentWater);
        model.addAttribute("targetWater", targetWater);
        model.addAttribute("waterProgress", waterProgress);
        model.addAttribute("totalCalories", totalCalories);
        model.addAttribute("totalSalt", Math.round(totalSalt * 10.0) / 10.0);
        model.addAttribute("totalFiber", Math.round(totalFiber * 10.0) / 10.0);
        model.addAttribute("caloriesPercent", caloriesPercent);
        model.addAttribute("saltPercent", saltPercent);
        model.addAttribute("fiberPercent", fiberPercent);
        model.addAttribute("totalExerciseMinutes", totalExerciseMinutes);
        model.addAttribute("targetExerciseMinutes", targetExerciseMinutes);
        model.addAttribute("exerciseProgress", exerciseProgress);

        return "patient/dashboard";
    }

    @GetMapping("/adherence")
    public String getAdherencePage(Model model) {
        Patient patient = patientRepository.findAll().stream().findFirst().orElse(null);
        if (patient == null) {
            DiseaseProfile profile = DiseaseProfile.builder()
                    .profileName("Đồng mắc (Tiểu đường & Tăng huyết áp)")
                    .build();
            patient = Patient.builder()
                    .fullName("Nguyễn Văn A")
                    .diseaseProfile(profile)
                    .build();
        }

        List<Map<String, Object>> medicationList = new ArrayList<>();
        int currentWater = 0;
        int targetWater = 2000;
        try {
            if (patient.getId() != null) {
                List<PatientMedication> activeMeds = patientMedicationRepository
                        .findByPatientIdAndIsActiveTrue(patient.getId());
                LocalDate today = LocalDate.now();
                for (PatientMedication med : activeMeds) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", med.getId());
                    item.put("medicineName", med.getMedicineName());
                    item.put("dosage", med.getDosage());
                    item.put("scheduledTime", med.getScheduledTime());
                    boolean taken = medicationLogRepository
                            .findByPatientMedicationIdAndLogDate(med.getId(), today)
                            .map(log -> Boolean.TRUE.equals(log.getIsTaken()))
                            .orElse(false);
                    item.put("isTaken", taken);
                    medicationList.add(item);
                }

                currentWater = waterLogRepository.findByPatientIdAndLogDate(patient.getId(), today)
                        .map(WaterLog::getAmountMl)
                        .orElse(0);
            }
        } catch (Exception ignored) {
        }
        int waterProgress = targetWater > 0 ? (currentWater * 100 / targetWater) : 0;
        if (waterProgress > 100) {
            waterProgress = 100;
        }

        int totalMeds = medicationList.size();
        int takenMeds = 0;
        for (Map<String, Object> item : medicationList) {
            if (Boolean.TRUE.equals(item.get("isTaken"))) {
                takenMeds++;
            }
        }

        model.addAttribute("patient", patient);
        model.addAttribute("medications", medicationList);
        model.addAttribute("currentWater", currentWater);
        model.addAttribute("targetWater", targetWater);
        model.addAttribute("waterProgress", waterProgress);
        model.addAttribute("totalMeds", totalMeds);
        model.addAttribute("takenMeds", takenMeds);
        return "patient/adherence";
    }

    @GetMapping("/medications")
    public String getMedicationsPage() {
        return "redirect:/patient/adherence";
    }

    @GetMapping("/water")
    public String getWaterPage() {
        return "redirect:/patient/adherence";
    }

    @GetMapping("/nutrition")
    public String getNutritionPage(Model model) {
        Patient patient = patientRepository.findAll().stream().findFirst().orElse(null);
        if (patient == null) {
            DiseaseProfile profile = DiseaseProfile.builder()
                    .profileName("Đồng mắc (Tiểu đường & Tăng huyết áp)")
                    .build();
            patient = Patient.builder()
                    .fullName("Nguyễn Văn A")
                    .diseaseProfile(profile)
                    .build();
        }

        List<PatientMeal> meals = new ArrayList<>();
        int totalCalories = 0;
        double totalSalt = 0.0;
        double totalFiber = 0.0;
        int targetCalories = 1400;
        double targetSalt = 2.5;
        double targetFiber = 20.0;

        try {
            if (patient.getId() != null) {
                LocalDate today = LocalDate.now();
                meals = patientMealRepository.findByPatientIdAndLogDate(patient.getId(), today);
                for (PatientMeal m : meals) {
                    totalCalories += m.getCalories();
                    totalSalt += m.getSaltG();
                    totalFiber += m.getFiberG();
                }
            }
        } catch (Exception ignored) {
        }

        int caloriesPercent = targetCalories > 0 ? (totalCalories * 100 / targetCalories) : 0;
        int saltPercent = targetSalt > 0 ? (int)(totalSalt * 100 / targetSalt) : 0;
        int fiberPercent = targetFiber > 0 ? (int)(totalFiber * 100 / targetFiber) : 0;

        if (caloriesPercent > 100) caloriesPercent = 100;
        if (saltPercent > 100) saltPercent = 100;
        if (fiberPercent > 100) fiberPercent = 100;

        model.addAttribute("patient", patient);
        model.addAttribute("meals", meals);
        model.addAttribute("totalCalories", totalCalories);
        model.addAttribute("totalSalt", Math.round(totalSalt * 10.0) / 10.0);
        model.addAttribute("totalFiber", Math.round(totalFiber * 10.0) / 10.0);
        model.addAttribute("targetCalories", targetCalories);
        model.addAttribute("targetSalt", targetSalt);
        model.addAttribute("targetFiber", targetFiber);
        model.addAttribute("caloriesPercent", caloriesPercent);
        model.addAttribute("saltPercent", saltPercent);
        model.addAttribute("fiberPercent", fiberPercent);

        return "patient/nutrition";
    }

    @GetMapping("/exercise")
    public String getExercisePage(Model model) {
        Patient patient = patientRepository.findAll().stream().findFirst().orElse(null);
        if (patient == null) {
            DiseaseProfile profile = DiseaseProfile.builder()
                    .profileName("Đồng mắc (Tiểu đường & Tăng huyết áp)")
                    .build();
            patient = Patient.builder()
                    .fullName("Nguyễn Văn A")
                    .diseaseProfile(profile)
                    .build();
        }

        List<PatientExercise> exercises = new ArrayList<>();
        int totalMinutes = 0;
        int totalCaloriesBurned = 0;
        int targetMinutes = 30; // Default target

        try {
            if (patient.getId() != null) {
                LocalDate today = LocalDate.now();
                exercises = patientExerciseRepository.findByPatientIdAndLogDate(patient.getId(), today);
                for (PatientExercise e : exercises) {
                    totalMinutes += e.getDurationMinutes();
                    totalCaloriesBurned += e.getCaloriesBurned();
                }
            }
        } catch (Exception ignored) {
        }

        int exerciseProgress = targetMinutes > 0 ? (totalMinutes * 100 / targetMinutes) : 0;
        if (exerciseProgress > 100) {
            exerciseProgress = 100;
        }

        model.addAttribute("patient", patient);
        model.addAttribute("exercises", exercises);
        model.addAttribute("totalMinutes", totalMinutes);
        model.addAttribute("totalCaloriesBurned", totalCaloriesBurned);
        model.addAttribute("targetMinutes", targetMinutes);
        model.addAttribute("exerciseProgress", exerciseProgress);

        return "patient/exercise";
    }

    // ==================== Progress Report ====================

    @GetMapping("/progress")
    public String getProgressReport(Model model) throws JsonProcessingException {
        Patient patient = patientRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No patient found in the database. Please initialize data first."));

        LocalDate startDate = LocalDate.now().minusDays(7);
        List<DailyHealthLog> logs = healthLogRepository.findByPatientIdAndLogDateGreaterThanEqualOrderByLogDateAsc(patient.getId(), startDate);

        List<String> dates = new ArrayList<>();
        List<Integer> systolicList = new ArrayList<>();
        List<Integer> diastolicList = new ArrayList<>();
        List<Double> glucoseList = new ArrayList<>();

        for (DailyHealthLog log : logs) {
            dates.add(log.getLogDate().toString() + " (" + log.getLogType() + ")");
            systolicList.add(log.getSystolicBp() != null ? log.getSystolicBp() : 0);
            diastolicList.add(log.getDiastolicBp() != null ? log.getDiastolicBp() : 0);
            glucoseList.add(log.getGlucoseLevel() != null ? log.getGlucoseLevel().doubleValue() : 0.0);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        model.addAttribute("patient", patient);
        model.addAttribute("datesJson", objectMapper.writeValueAsString(dates));
        model.addAttribute("systolicJson", objectMapper.writeValueAsString(systolicList));
        model.addAttribute("diastolicJson", objectMapper.writeValueAsString(diastolicList));
        model.addAttribute("glucoseJson", objectMapper.writeValueAsString(glucoseList));

        return "patient/progress";
    }
}
