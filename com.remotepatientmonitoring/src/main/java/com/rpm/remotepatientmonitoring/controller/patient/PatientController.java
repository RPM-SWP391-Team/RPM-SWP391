package com.rpm.remotepatientmonitoring.controller.patient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rpm.remotepatientmonitoring.model.*;
import com.rpm.remotepatientmonitoring.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;

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
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

    @Autowired
    private TreatmentPlanRepository treatmentPlanRepository;

    @Autowired
    private NutritionRuleRepository nutritionRuleRepository;

    @Autowired
    private FoodDictionaryRepository foodDictionaryRepository;

    private Patient getCurrentPatient() {
        org.springframework.security.core.Authentication auth = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            Object principal = auth.getPrincipal();
            if (principal instanceof com.rpm.remotepatientmonitoring.config.CustomUserDetails) {
                com.rpm.remotepatientmonitoring.config.CustomUserDetails userDetails = 
                    (com.rpm.remotepatientmonitoring.config.CustomUserDetails) principal;
                java.util.Optional<Patient> opt = patientRepository.findByAccountId(userDetails.getAccount().getId());
                if (opt.isPresent()) {
                    return opt.get();
                }
            }
        }
        List<Patient> all = patientRepository.findAll();
        if (all.size() > 0) {
            return all.get(0);
        }
        return null;
    }

    @GetMapping("/dashboard")
    public String getDashboard(Model model) {
        Patient patient = getCurrentPatient();
        if (patient == null) {
            return "redirect:/auth/login";
        }
        if ("NEW".equals(patient.getStatus())) {
            return "redirect:/patient/appointments";
        }

        // Lấy phác đồ điều trị hiện hành của bệnh nhân từ DB
        TreatmentPlan plan = null;
        if (patient.getId() != null) {
            plan = treatmentPlanRepository.findByPatientIdAndIsCurrent(patient.getId(), true).orElse(null);
        }
        if (plan == null) {
            plan = new TreatmentPlan();
            plan.setMedicalOrder("Chưa có chỉ định thuốc chính thức từ bác sĩ.");
            plan.setExerciseGoal("Chưa thiết lập mục tiêu vận động.");
        }

        // Lấy giới hạn dinh dưỡng mục tiêu từ NutritionRule hiện hành
        int targetCalories = 2000;
        double targetSalt = 5.0;
        double targetFiber = 25.0;
        int targetWater = 2000;

        if (patient.getId() != null) {
            NutritionRule currentRule = nutritionRuleRepository.findByPatientIdAndIsCurrent(patient.getId(), true).orElse(null);
            if (currentRule != null) {
                targetCalories = currentRule.getMaxCaloriesPerDay() != null ? currentRule.getMaxCaloriesPerDay() : 2000;
                targetSalt = currentRule.getMaxSaltG() != null ? currentRule.getMaxSaltG().doubleValue() : 5.0;
                targetFiber = currentRule.getMinFiberG() != null ? currentRule.getMinFiberG().doubleValue() : 25.0;
                targetWater = currentRule.getDailyWaterMl() != null ? currentRule.getDailyWaterMl() : 2000;
            }
        }

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
        try {
            if (patient.getId() != null) {
                List<PatientMedication> activeMeds = patientMedicationRepository
                        .findByPatientIdAndIsActiveTrue(patient.getId());
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
                currentWater = waterLogRepository.findByPatientIdAndLogDate(patient.getId(), today)
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
            if (patient.getId() != null) {
                LocalDate today = LocalDate.now();
                List<PatientMeal> mealsToday = patientMealRepository.findByPatientIdAndLogDate(patient.getId(), today);
                for (PatientMeal m : mealsToday) {
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

        int totalExerciseMinutes = 0;
        int targetExerciseMinutes = 30; // Default goal
        try {
            if (patient.getId() != null) {
                LocalDate today = LocalDate.now();
                List<PatientExercise> exercisesToday = patientExerciseRepository.findByPatientIdAndLogDate(patient.getId(), today);
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
        model.addAttribute("targetCalories", targetCalories);
        model.addAttribute("targetSalt", targetSalt);
        model.addAttribute("targetFiber", targetFiber);
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
        Patient patient = getCurrentPatient();
        if (patient == null) {
            return "redirect:/auth/login";
        }
        if ("NEW".equals(patient.getStatus())) {
            return "redirect:/patient/appointments";
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
        Patient patient = getCurrentPatient();
        if (patient == null) {
            return "redirect:/auth/login";
        }
        if ("NEW".equals(patient.getStatus())) {
            return "redirect:/patient/appointments";
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
        
        List<FoodDictionary> foods = foodDictionaryRepository.findByIsActiveTrue();
        model.addAttribute("foods", foods);

        return "patient/nutrition";
    }

    @GetMapping("/exercise")
    public String getExercisePage(Model model) {
        Patient patient = getCurrentPatient();
        if (patient == null) {
            return "redirect:/auth/login";
        }
        if ("NEW".equals(patient.getStatus())) {
            return "redirect:/patient/appointments";
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

    // ==================== Progress Report (Patient Profile) ====================

    @GetMapping("/progress")
    public String getProgressReport(Model model) {
        Patient patient = getCurrentPatient();
        if (patient == null) {
            return "redirect:/auth/login";
        }
        if ("NEW".equals(patient.getStatus())) {
            return "redirect:/patient/appointments";
        }
        
        // Eagerly initialize proxies to avoid LazyInitializationException in Thymeleaf
        if (patient.getAccount() != null) {
            patient.getAccount().getEmail();
        }
        if (patient.getDoctor() != null) {
            patient.getDoctor().getFullName();
        }
        if (patient.getHospital() != null) {
            patient.getHospital().getFullName();
        }
        if (patient.getDiseaseProfile() != null) {
            patient.getDiseaseProfile().getProfileName();
        }

        model.addAttribute("patient", patient);
        return "patient/progress";
    }

    @PostMapping("/progress/update")
    public String updateProfile(
            @RequestParam("phone") String phone,
            @RequestParam("address") String address,
            @RequestParam("email") String email,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam("emergencyContactName") String emergencyContactName,
            @RequestParam("emergencyContactPhone") String emergencyContactPhone,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes
    ) {
        if (!phone.matches("0[35789][0-9]{8}")) {
            redirectAttributes.addFlashAttribute("error", "Số điện thoại cá nhân không hợp lệ!");
            return "redirect:/patient/progress";
        }
        if (!emergencyContactPhone.matches("0[35789][0-9]{8}")) {
            redirectAttributes.addFlashAttribute("error", "Số điện thoại người thân không hợp lệ!");
            return "redirect:/patient/progress";
        }
        if (!email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")) {
            redirectAttributes.addFlashAttribute("error", "Email không hợp lệ!");
            return "redirect:/patient/progress";
        }
        if (password != null && !password.trim().isEmpty() && (password.length() < 6 || password.length() > 50)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu phải từ 6 đến 50 ký tự!");
            return "redirect:/patient/progress";
        }
        Patient patient = patientRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No patient found in the database."));

        patient.setPhone(phone);
        patient.setAddress(address);
        patient.setEmergencyContactName(emergencyContactName);
        patient.setEmergencyContactPhone(emergencyContactPhone);
        patient.setUpdatedAt(LocalDateTime.now());
        patientRepository.save(patient);

        Account account = patient.getAccount();
        if (account != null) {
            account.setEmail(email);
            if (password != null && !password.trim().isEmpty()) {
                account.setPasswordHash(passwordEncoder.encode(password));
            }
            account.setUpdatedAt(LocalDateTime.now());
            accountRepository.save(account);
        }

        return "redirect:/patient/progress?updateSuccess=true";
    }
}
