package com.rpm.remotepatientmonitoring.controller.patient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rpm.remotepatientmonitoring.model.*;
import com.rpm.remotepatientmonitoring.config.CustomUserDetails;
import com.rpm.remotepatientmonitoring.service.patient.ExerciseLogService;
import com.rpm.remotepatientmonitoring.service.patient.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/patient")
public class PatientController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PatientService patientService;

    @Autowired
    private ExerciseLogService exerciseLogService;

    @Autowired
    private com.rpm.remotepatientmonitoring.service.patient.PatientHealthService patientHealthService;

    private Patient getCurrentPatient() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            Object principal = auth.getPrincipal();
            if (principal instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) principal;
                Optional<Patient> opt = patientService.findByAccountId(userDetails.getAccount().getId());
                if (opt.isPresent()) {
                    return opt.get();
                }
            }
        }
        return null;
    }

    @GetMapping("/dashboard")
    public String getDashboard(
            @RequestParam(value = "date", required = false) String dateStr,
            Model model) {
        Patient patient = getCurrentPatient();
        if (patient == null) {
            return "redirect:/auth/login";
        }
        if ("NEW".equals(patient.getStatus())) {
            return "redirect:/patient/appointments";
        }

        System.out.println("DEBUG PatientController /dashboard - dateStr: " + dateStr);
        LocalDate selectedDate = LocalDate.now();
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            try {
                selectedDate = LocalDate.parse(dateStr.trim());
                System.out.println("DEBUG PatientController /dashboard - parsed selectedDate: " + selectedDate);
            } catch (Exception e) {
                System.out.println("DEBUG PatientController /dashboard - parsing failed: " + e.getMessage());
            }
        }
        boolean isToday = selectedDate.equals(LocalDate.now());

        // Lấy phác đồ điều trị hiện hành của bệnh nhân từ DB
        TreatmentPlan plan = null;
        if (patient.getId() != null) {
            plan = patientService.findCurrentTreatmentPlan(patient.getId()).orElse(null);
        }
        if (plan == null) {
            plan = new TreatmentPlan();
            plan.setMedicalOrder("Chưa có chỉ định thuốc chính thức từ bác sĩ.");
            plan.setExerciseGoal("Chưa thiết lập mục tiêu vận động.");
        }

        // Lấy giới hạn dinh dưỡng mục tiêu từ NutritionRule hiện hành
        Integer targetCalories = null;
        Double targetCarbs = null;
        Double targetSalt = null;
        Double targetFiber = null;
        Double targetFat = null;
        Double targetProtein = null;
        Integer targetWater = null;

        if (patient.getId() != null) {
            NutritionRule currentRule = patientService.findCurrentNutritionRule(patient.getId()).orElse(null);
            if (currentRule != null) {
                targetCalories = currentRule.getMaxCaloriesPerDay();
                targetCarbs = currentRule.getMaxCarbsG() != null ? currentRule.getMaxCarbsG().doubleValue() : null;
                targetSalt = currentRule.getMaxSaltG() != null ? currentRule.getMaxSaltG().doubleValue() : null;
                targetFiber = currentRule.getMinFiberG() != null ? currentRule.getMinFiberG().doubleValue() : null;
                targetFat = currentRule.getMaxFatG() != null ? currentRule.getMaxFatG().doubleValue() : null;
                targetProtein = currentRule.getMinProteinG() != null ? currentRule.getMinProteinG().doubleValue() : null;
                targetWater = currentRule.getDailyWaterMl();
            }
        }

        // Calculate meal-specific targets (Breakfast 30%, Lunch 40%, Dinner 30%) of daily nutrition rules
        List<Map<String, Object>> mealTargets = new ArrayList<>();
        
        String[] mealNames = {"Bữa Sáng", "Bữa Trưa", "Bữa Tối"};
        double[] mealRatios = {0.3, 0.4, 0.3};
        
        for (int i = 0; i < 3; i++) {
            Map<String, Object> meal = new HashMap<>();
            meal.put("mealName", mealNames[i]);
            
            double ratio = mealRatios[i];
            
            meal.put("calories", targetCalories != null ? (int) Math.round(targetCalories * ratio) : null);
            meal.put("protein", targetProtein != null ? Math.round(targetProtein * ratio * 10.0) / 10.0 : null);
            meal.put("lipid", targetFat != null ? Math.round(targetFat * ratio * 10.0) / 10.0 : null);
            meal.put("glucid", targetCarbs != null ? Math.round(targetCarbs * ratio * 10.0) / 10.0 : null);
            meal.put("salt", targetSalt != null ? Math.round(targetSalt * ratio * 10.0) / 10.0 : null);
            meal.put("fiber", targetFiber != null ? Math.round(targetFiber * ratio * 10.0) / 10.0 : null);
            meal.put("water", targetWater != null ? (int) Math.round(targetWater * ratio) : null);
            
            mealTargets.add(meal);
        }

        // === Medication Tracker: Lấy danh sách thuốc + tính tiến độ theo selectedDate ===
        int totalMeds = 0;
        int takenMeds = 0;
        Integer currentWater = 0;
        try {
            if (patient.getId() != null) {
                List<PatientMedication> activeMeds = patientService.findActiveMedications(patient.getId());
                totalMeds = activeMeds.size();
                for (PatientMedication med : activeMeds) {
                    boolean taken = false;
                    Optional<MedicationLog> medLogOpt = patientService.findMedicationLog(med.getId(), selectedDate);
                    if (medLogOpt.isPresent()) {
                        MedicationLog logVal = medLogOpt.get();
                        if (Boolean.TRUE.equals(logVal.getIsTaken())) {
                            taken = true;
                        }
                    }
                    if (taken) {
                        takenMeds++;
                    }
                }
                
                Optional<WaterLog> waterLogOpt = patientService.findWaterLog(patient.getId(), selectedDate);
                if (waterLogOpt.isPresent()) {
                    currentWater = waterLogOpt.get().getAmountMl();
                }
            }
        } catch (Exception ignored) {
        }
        int medProgress = totalMeds > 0 ? (takenMeds * 100 / totalMeds) : 0;
        int waterProgress = (targetWater != null && targetWater > 0 && currentWater != null) ? (currentWater * 100 / targetWater) : 0;
        if (waterProgress > 100) {
            waterProgress = 100;
        }

        Integer totalCalories = 0;
        Double totalCarbs = 0.0;
        Double totalSalt = 0.0;
        Double totalFiber = 0.0;
        Double totalFat = 0.0;
        Double totalProtein = 0.0;
        try {
            if (patient.getId() != null) {
                List<PatientMeal> mealsToday = patientService.findMealsByDate(patient.getId(), selectedDate);
                if (mealsToday != null) {
                    for (PatientMeal m : mealsToday) {
                        if (m.getCalories() != null) totalCalories += m.getCalories();
                        if (m.getGlucidG() != null) totalCarbs += m.getGlucidG();
                        if (m.getSaltG() != null) totalSalt += m.getSaltG();
                        if (m.getFiberG() != null) totalFiber += m.getFiberG();
                        if (m.getLipidG() != null) totalFat += m.getLipidG();
                        if (m.getProteinG() != null) totalProtein += m.getProteinG();
                    }
                }
            }
        } catch (Exception ignored) {
        }

        int caloriesPercent = (targetCalories != null && targetCalories > 0 && totalCalories != null) ? (totalCalories * 100 / targetCalories) : 0;
        int carbsPercent = (targetCarbs != null && targetCarbs > 0 && totalCarbs != null) ? (int)(totalCarbs * 100 / targetCarbs) : 0;
        int saltPercent = (targetSalt != null && targetSalt > 0 && totalSalt != null) ? (int)(totalSalt * 100 / targetSalt) : 0;
        int fiberPercent = (targetFiber != null && targetFiber > 0 && totalFiber != null) ? (int)(totalFiber * 100 / targetFiber) : 0;
        int fatPercent = (targetFat != null && targetFat > 0 && totalFat != null) ? (int)(totalFat * 100 / targetFat) : 0;
        int proteinPercent = (targetProtein != null && targetProtein > 0 && totalProtein != null) ? (int)(totalProtein * 100 / targetProtein) : 0;

        if (caloriesPercent > 100) caloriesPercent = 100;
        if (carbsPercent > 100) carbsPercent = 100;
        if (saltPercent > 100) saltPercent = 100;
        if (fiberPercent > 100) fiberPercent = 100;
        if (fatPercent > 100) fatPercent = 100;
        if (proteinPercent > 100) proteinPercent = 100;

        int totalExerciseMinutes = 0;
        int targetExerciseMinutes = 30; // Default goal
        try {
            if (patient.getId() != null) {
                List<PatientExercise> exercisesToday = patientService.findExercisesByDate(patient.getId(), selectedDate);
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

        // --- Calculate today's health logs completion status ---
        Map<String, Object> logStatus = new HashMap<>();
        boolean reqBp = false;
        boolean reqGl = false;
        if (patient.getDiseaseProfile() != null) {
            reqBp = Boolean.TRUE.equals(patient.getDiseaseProfile().getRequiresBpInput());
            reqGl = Boolean.TRUE.equals(patient.getDiseaseProfile().getRequiresGlucoseInput());
        } else {
            reqBp = true;
            reqGl = true;
        }

        List<DailyHealthLog> logsList = new ArrayList<>();
        if (patient.getId() != null) {
            logsList = patientHealthService.getHealthLogsByPatientIdAndDate(patient.getId(), selectedDate);
        }
        final List<DailyHealthLog> finalLogs = logsList;
        
        com.rpm.remotepatientmonitoring.model.AlertThreshold threshold = patientHealthService.getAlertThresholdByPatientOrHospital(patient.getId(), patient.getHospital() != null ? patient.getHospital().getId() : null);

        // Find logs for MORNING
        List<DailyHealthLog> morningLogs = finalLogs.stream()
                .filter(l -> "MORNING".equalsIgnoreCase(l.getLogType()))
                .toList();
        DailyHealthLog morningBpLog = morningLogs.stream()
                .filter(l -> l.getSystolicBp() != null)
                .max(Comparator.comparing(DailyHealthLog::getLogTime))
                .orElse(null);
        DailyHealthLog morningGlLog = morningLogs.stream()
                .filter(l -> l.getGlucoseLevel() != null)
                .max(Comparator.comparing(DailyHealthLog::getLogTime))
                .orElse(null);

        // Find logs for EVENING
        List<DailyHealthLog> eveningLogs = finalLogs.stream()
                .filter(l -> "EVENING".equalsIgnoreCase(l.getLogType()))
                .toList();
        DailyHealthLog eveningBpLog = eveningLogs.stream()
                .filter(l -> l.getSystolicBp() != null)
                .max(Comparator.comparing(DailyHealthLog::getLogTime))
                .orElse(null);
        DailyHealthLog eveningGlLog = eveningLogs.stream()
                .filter(l -> l.getGlucoseLevel() != null)
                .max(Comparator.comparing(DailyHealthLog::getLogTime))
                .orElse(null);

        // Find logs for RANDOM
        List<DailyHealthLog> randomLogs = finalLogs.stream()
                .filter(l -> "RANDOM".equalsIgnoreCase(l.getLogType()))
                .toList();
        DailyHealthLog randomBpLog = randomLogs.stream()
                .filter(l -> l.getSystolicBp() != null)
                .max(Comparator.comparing(DailyHealthLog::getLogTime))
                .orElse(null);
        DailyHealthLog randomGlLog = randomLogs.stream()
                .filter(l -> l.getGlucoseLevel() != null)
                .max(Comparator.comparing(DailyHealthLog::getLogTime))
                .orElse(null);

        // Morning BP
        if (reqBp) {
            if (morningBpLog != null) {
                String statusText = getBpStatusText(morningBpLog.getSystolicBp(), morningBpLog.getDiastolicBp(), threshold);
                logStatus.put("morningBp", "Đã nhập: " + morningBpLog.getSystolicBp() + "/" + morningBpLog.getDiastolicBp() + " mmHg (" + statusText + ")");
                logStatus.put("morningBpClass", getBpClass(morningBpLog.getSystolicBp(), morningBpLog.getDiastolicBp(), threshold));
                logStatus.put("morningBpDone", true);
            } else {
                logStatus.put("morningBp", "Chưa nhập");
                logStatus.put("morningBpClass", "text-danger fw-bold");
                logStatus.put("morningBpDone", false);
            }
        } else {
            logStatus.put("morningBp", "Không yêu cầu");
            logStatus.put("morningBpClass", "text-muted");
            logStatus.put("morningBpDone", true);
        }

        // Morning Glucose
        if (reqGl) {
            if (morningGlLog != null) {
                String statusText = getGlucoseStatusText(morningGlLog.getGlucoseLevel(), threshold);
                logStatus.put("morningGl", "Đã nhập: " + morningGlLog.getGlucoseLevel() + " mmol/L (" + statusText + ")");
                logStatus.put("morningGlClass", getGlucoseClass(morningGlLog.getGlucoseLevel(), threshold));
                logStatus.put("morningGlDone", true);
            } else {
                logStatus.put("morningGl", "Chưa nhập");
                logStatus.put("morningGlClass", "text-danger fw-bold");
                logStatus.put("morningGlDone", false);
            }
        } else {
            logStatus.put("morningGl", "Không yêu cầu");
            logStatus.put("morningGlClass", "text-muted");
            logStatus.put("morningGlDone", true);
        }

        // Evening BP
        if (reqBp) {
            if (eveningBpLog != null) {
                String statusText = getBpStatusText(eveningBpLog.getSystolicBp(), eveningBpLog.getDiastolicBp(), threshold);
                logStatus.put("eveningBp", "Đã nhập: " + eveningBpLog.getSystolicBp() + "/" + eveningBpLog.getDiastolicBp() + " mmHg (" + statusText + ")");
                logStatus.put("eveningBpClass", getBpClass(eveningBpLog.getSystolicBp(), eveningBpLog.getDiastolicBp(), threshold));
                logStatus.put("eveningBpDone", true);
            } else {
                logStatus.put("eveningBp", "Chưa nhập");
                logStatus.put("eveningBpClass", "text-danger fw-bold");
                logStatus.put("eveningBpDone", false);
            }
        } else {
            logStatus.put("eveningBp", "Không yêu cầu");
            logStatus.put("eveningBpClass", "text-muted");
            logStatus.put("eveningBpDone", true);
        }

        // Evening Glucose
        if (reqGl) {
            if (eveningGlLog != null) {
                String statusText = getGlucoseStatusText(eveningGlLog.getGlucoseLevel(), threshold);
                logStatus.put("eveningGl", "Đã nhập: " + eveningGlLog.getGlucoseLevel() + " mmol/L (" + statusText + ")");
                logStatus.put("eveningGlClass", getGlucoseClass(eveningGlLog.getGlucoseLevel(), threshold));
                logStatus.put("eveningGlDone", true);
            } else {
                logStatus.put("eveningGl", "Chưa nhập");
                logStatus.put("eveningGlClass", "text-danger fw-bold");
                logStatus.put("eveningGlDone", false);
            }
        } else {
            logStatus.put("eveningGl", "Không yêu cầu");
            logStatus.put("eveningGlClass", "text-muted");
            logStatus.put("eveningGlDone", true);
        }

        // Random BP & Glucose (Optional)
        if (randomBpLog != null) {
            String statusText = getBpStatusText(randomBpLog.getSystolicBp(), randomBpLog.getDiastolicBp(), threshold);
            logStatus.put("randomBp", "Đã nhập: " + randomBpLog.getSystolicBp() + "/" + randomBpLog.getDiastolicBp() + " mmHg (" + statusText + ")");
            logStatus.put("randomBpClass", getBpClass(randomBpLog.getSystolicBp(), randomBpLog.getDiastolicBp(), threshold));
        } else {
            logStatus.put("randomBp", "Chưa nhập (Tùy chọn)");
            logStatus.put("randomBpClass", "text-muted");
        }
        if (randomGlLog != null) {
            String statusText = getGlucoseStatusText(randomGlLog.getGlucoseLevel(), threshold);
            logStatus.put("randomGl", "Đã nhập: " + randomGlLog.getGlucoseLevel() + " mmol/L (" + statusText + ")");
            logStatus.put("randomGlClass", getGlucoseClass(randomGlLog.getGlucoseLevel(), threshold));
        } else {
            logStatus.put("randomGl", "Chưa nhập (Tùy chọn)");
            logStatus.put("randomGlClass", "text-muted");
        }

        // Check if all required logs are completed
        boolean allRequiredDone = true;
        if (reqBp && (!Boolean.TRUE.equals(logStatus.get("morningBpDone")) || !Boolean.TRUE.equals(logStatus.get("eveningBpDone")))) {
            allRequiredDone = false;
        }
        if (reqGl && (!Boolean.TRUE.equals(logStatus.get("morningGlDone")) || !Boolean.TRUE.equals(logStatus.get("eveningGlDone")))) {
            allRequiredDone = false;
        }
        logStatus.put("allRequiredDone", allRequiredDone);
        model.addAttribute("logStatus", logStatus);

        // Daily nutritional targets
        model.addAttribute("patient", patient);
        model.addAttribute("treatmentPlan", plan);
        model.addAttribute("menu", mealTargets);
        model.addAttribute("selectedDate", selectedDate.toString());
        model.addAttribute("isToday", isToday);
        
        model.addAttribute("targetCalories", targetCalories);
        model.addAttribute("targetSalt", targetSalt);
        model.addAttribute("targetFiber", targetFiber);
        model.addAttribute("targetCarbs", targetCarbs);
        model.addAttribute("targetFat", targetFat);
        model.addAttribute("targetProtein", targetProtein);
        
        model.addAttribute("totalCalories", totalCalories);
        model.addAttribute("totalSalt", totalSalt != null ? Math.round(totalSalt * 10.0) / 10.0 : null);
        model.addAttribute("totalFiber", totalFiber != null ? Math.round(totalFiber * 10.0) / 10.0 : null);
        model.addAttribute("totalCarbs", totalCarbs != null ? Math.round(totalCarbs * 10.0) / 10.0 : null);
        model.addAttribute("totalFat", totalFat != null ? Math.round(totalFat * 10.0) / 10.0 : null);
        model.addAttribute("totalProtein", totalProtein != null ? Math.round(totalProtein * 10.0) / 10.0 : null);
        
        model.addAttribute("caloriesPercent", caloriesPercent);
        model.addAttribute("saltPercent", saltPercent);
        model.addAttribute("fiberPercent", fiberPercent);
        model.addAttribute("carbsPercent", carbsPercent);
        model.addAttribute("fatPercent", fatPercent);
        model.addAttribute("proteinPercent", proteinPercent);

        model.addAttribute("totalMeds", totalMeds);
        model.addAttribute("takenMeds", takenMeds);
        model.addAttribute("medProgress", medProgress);
        model.addAttribute("currentWater", currentWater);
        model.addAttribute("targetWater", targetWater);
        model.addAttribute("waterProgress", waterProgress);
        
        model.addAttribute("totalExerciseMinutes", totalExerciseMinutes);
        model.addAttribute("targetExerciseMinutes", targetExerciseMinutes);
        model.addAttribute("exerciseProgress", exerciseProgress);

        return "patient/dashboard";
    }
    
    @GetMapping("/adherence")
    public String getAdherencePage(
            @RequestParam(value = "range", defaultValue = "week") String range,
            @RequestParam(value = "searchDate", required = false) String searchDateStr,
            Model model) {
        Patient patient = getCurrentPatient();
        if (patient == null) {
            return "redirect:/auth/login";
        }
        if ("NEW".equals(patient.getStatus())) {
            return "redirect:/patient/appointments";
        }

        List<Map<String, Object>> medicationList = new ArrayList<>();
        List<PatientMedication> activeMeds = new ArrayList<>();
        int currentWater = 0;
        int targetWater = 2000;
        try {
            if (patient.getId() != null) {
                NutritionRule currentRule = patientService.findCurrentNutritionRule(patient.getId()).orElse(null);
                if (currentRule != null && currentRule.getDailyWaterMl() != null) {
                    targetWater = currentRule.getDailyWaterMl();
                }

                activeMeds = patientService.findActiveMedications(patient.getId());
                LocalDate today = LocalDate.now();
                for (PatientMedication med : activeMeds) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", med.getId());
                    item.put("medicineName", med.getMedicineName());
                    item.put("dosage", med.getDosage());
                    item.put("scheduledTime", med.getScheduledTime());
                    boolean taken = false;
                    Optional<MedicationLog> medLogOpt = patientService.findMedicationLog(med.getId(), today);
                    if (medLogOpt.isPresent()) {
                        MedicationLog logVal = medLogOpt.get();
                        if (Boolean.TRUE.equals(logVal.getIsTaken())) {
                            taken = true;
                        }
                    }
                    item.put("isTaken", taken);

                    boolean isOverdue = false;
                    if (!taken) {
                        try {
                            String scheduledStr = med.getScheduledTime();
                            if (scheduledStr != null && scheduledStr.matches("^\\d{2}:\\d{2}$")) {
                                java.time.LocalTime scheduledTime = java.time.LocalTime.parse(scheduledStr);
                                java.time.LocalTime currentTime = java.time.LocalTime.now();
                                if (currentTime.isAfter(scheduledTime)) {
                                    isOverdue = true;
                                }
                            }
                        } catch (Exception ignored) {
                        }
                    }
                    item.put("isOverdue", isOverdue);

                    medicationList.add(item);
                }

                currentWater = patientService.findWaterLog(patient.getId(), today)
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

        LocalDate sDate = null;
        if (searchDateStr != null && !searchDateStr.trim().isEmpty()) {
            try {
                sDate = LocalDate.parse(searchDateStr);
                range = "custom";
            } catch (Exception ignored) {
            }
        }

        List<MedicationLog> historyLogs = new ArrayList<>();
        try {
            if (patient.getId() != null) {
                if (sDate != null) {
                    List<MedicationLog> rawLogs = patientService.findMedicationHistory(patient.getId(), sDate);
                    for (MedicationLog logVal : rawLogs) {
                        if (logVal.getLogDate().equals(sDate)) {
                            historyLogs.add(logVal);
                        }
                    }
                } else if ("today".equalsIgnoreCase(range)) {
                    historyLogs = patientService.findMedicationHistory(patient.getId(), LocalDate.now());
                } else if ("week".equalsIgnoreCase(range)) {
                    historyLogs = patientService.findMedicationHistory(patient.getId(), LocalDate.now().minusDays(7));
                } else if ("month".equalsIgnoreCase(range)) {
                    historyLogs = patientService.findMedicationHistory(patient.getId(), LocalDate.now().minusDays(30));
                } else {
                    historyLogs = patientService.findMedicationHistory(patient.getId(), null);
                }
            }
        } catch (Exception ignored) {
        }

        // Group history by date (reverse order, i.e., recent date first)
        Map<LocalDate, Map<String, Object>> historyGrouped = new TreeMap<>(Collections.reverseOrder());
        
        // 1. Initialize keys for all dates from medication logs
        for (MedicationLog logVal : historyLogs) {
            LocalDate date = logVal.getLogDate();
            historyGrouped.putIfAbsent(date, new HashMap<>());
            Map<String, Object> dateData = historyGrouped.get(date);
            dateData.putIfAbsent("meds", new ArrayList<Map<String, Object>>());
        }
        
        // 2. Populate medication logs
        for (MedicationLog logVal : historyLogs) {
            LocalDate date = logVal.getLogDate();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> medsList = (List<Map<String, Object>>) historyGrouped.get(date).get("meds");
            
            Map<String, Object> logInfo = new HashMap<>();
            logInfo.put("id", logVal.getId());
            logInfo.put("medicationId", logVal.getPatientMedication().getId());
            logInfo.put("medicineName", logVal.getPatientMedication().getMedicineName());
            logInfo.put("dosage", logVal.getPatientMedication().getDosage());
            logInfo.put("scheduledTime", logVal.getPatientMedication().getScheduledTime());
            logInfo.put("isTaken", logVal.getIsTaken());
            logInfo.put("takenAt", logVal.getTakenAt());
            
            medsList.add(logInfo);
        }

        // 3. Query and populate water logs for the history range
        List<WaterLog> historyWaterLogs = new ArrayList<>();
        try {
            if (patient.getId() != null) {
                if (sDate != null) {
                    patientService.findWaterLog(patient.getId(), sDate).ifPresent(historyWaterLogs::add);
                } else {
                    LocalDate oldestDate = LocalDate.now().minusDays(30); // default
                    if (!historyLogs.isEmpty()) {
                        oldestDate = historyLogs.get(0).getLogDate();
                    } else {
                        if ("today".equalsIgnoreCase(range)) oldestDate = LocalDate.now();
                        else if ("week".equalsIgnoreCase(range)) oldestDate = LocalDate.now().minusDays(7);
                        else if ("month".equalsIgnoreCase(range)) oldestDate = LocalDate.now().minusDays(30);
                        else oldestDate = LocalDate.of(2000, 1, 1);
                    }
                    historyWaterLogs = patientService.findWaterHistory(patient.getId(), oldestDate);
                }
            }
        } catch (Exception ignored) {
        }

        for (WaterLog wl : historyWaterLogs) {
            LocalDate date = wl.getLogDate();
            historyGrouped.putIfAbsent(date, new HashMap<>());
            Map<String, Object> dateData = historyGrouped.get(date);
            dateData.put("waterId", wl.getId());
            dateData.put("waterAmount", wl.getAmountMl());
            dateData.put("waterTarget", targetWater);
            int waterPercent = targetWater > 0 ? (wl.getAmountMl() * 100 / targetWater) : 0;
            dateData.put("waterPercent", Math.min(waterPercent, 100));
        }

        // Ensure all grouped dates have an initialized "meds" list (even if they only had water logs)
        for (Map<String, Object> dateData : historyGrouped.values()) {
            dateData.putIfAbsent("meds", new ArrayList<Map<String, Object>>());
        }

        // Calculate daily adherence chart data (Medication & Water)
        List<Map<String, Object>> chartData = new ArrayList<>();
        try {
            if (patient.getId() != null) {
                List<LocalDate> chartDates = new ArrayList<>();
                LocalDate today = LocalDate.now();
                int daysToFetch = 7; // default for week
                if ("today".equalsIgnoreCase(range)) {
                    daysToFetch = 1;
                } else if ("week".equalsIgnoreCase(range)) {
                    daysToFetch = 7;
                } else if ("month".equalsIgnoreCase(range)) {
                    daysToFetch = 30;
                } else {
                    daysToFetch = 15; // last 15 days for "all"
                }

                for (int i = daysToFetch - 1; i >= 0; i--) {
                    chartDates.add(today.minusDays(i));
                }

                // Batch fetch water logs for the chart period to optimize DB queries
                List<WaterLog> chartWaterLogs = patientService.findWaterHistory(patient.getId(), chartDates.get(0));
                Map<LocalDate, Integer> waterLogMap = new HashMap<>();
                for (WaterLog wl : chartWaterLogs) {
                    waterLogMap.put(wl.getLogDate(), wl.getAmountMl());
                }

                List<PatientMedication> activeMedsForChart = patientService.findActiveMedications(patient.getId());

                for (LocalDate date : chartDates) {
                    Map<String, Object> dayInfo = new HashMap<>();
                    String dateLabel = date.getDayOfMonth() + "/" + date.getMonthValue();
                    dayInfo.put("dateLabel", dateLabel);

                    // Filter medications that were active on this 'date' (check if log exists OR if created on/before 'date')
                    List<PatientMedication> activeOnDate = new ArrayList<>();
                    Map<Integer, Boolean> takenMap = new HashMap<>();

                    for (PatientMedication med : activeMedsForChart) {
                        Optional<MedicationLog> logOpt = patientService.findMedicationLog(med.getId(), date);
                        
                        boolean isActive = false;
                        if (logOpt.isPresent()) {
                            isActive = true;
                            takenMap.put(med.getId(), Boolean.TRUE.equals(logOpt.get().getIsTaken()));
                        } else if (med.getCreatedAt() == null) {
                            isActive = true;
                        } else {
                            LocalDate medCreatedDate = med.getCreatedAt().toLocalDate();
                            if (medCreatedDate.isBefore(date) || medCreatedDate.isEqual(date)) {
                                isActive = true;
                            }
                        }

                        if (isActive) {
                            activeOnDate.add(med);
                        }
                    }

                    int totalMedsCountOnDate = activeOnDate.size();

                    // Medication Adherence
                    int takenCount = 0;
                    for (PatientMedication med : activeOnDate) {
                        if (Boolean.TRUE.equals(takenMap.get(med.getId()))) {
                            takenCount++;
                        }
                    }
                    int medPercent = totalMedsCountOnDate > 0 ? (takenCount * 100 / totalMedsCountOnDate) : 100;
                    dayInfo.put("medPercent", medPercent);
                    dayInfo.put("medTaken", takenCount);
                    dayInfo.put("medTotal", totalMedsCountOnDate);

                    // Water Adherence
                    int waterAmount = waterLogMap.getOrDefault(date, 0);
                    int waterPercent = targetWater > 0 ? (waterAmount * 100 / targetWater) : 0;
                    dayInfo.put("waterPercent", Math.min(waterPercent, 100));
                    dayInfo.put("waterAmount", waterAmount);
                    dayInfo.put("waterTarget", targetWater);

                    chartData.add(dayInfo);
                }
            }
        } catch (Exception ignored) {
        }

        // Build water log for the target date for display under "Theo Dõi Uống Nước"
        List<WaterLog> recentWaterLogs = new ArrayList<>();
        try {
            if (patient.getId() != null) {
                LocalDate targetDate = (sDate != null) ? sDate : LocalDate.now();
                recentWaterLogs = patientService.findWaterLogs(patient.getId(), targetDate);
            }
        } catch (Exception ignored) {}

        model.addAttribute("patient", patient);
        model.addAttribute("medications", medicationList);
        model.addAttribute("currentWater", currentWater);
        model.addAttribute("targetWater", targetWater);
        model.addAttribute("waterProgress", waterProgress);
        model.addAttribute("totalMeds", totalMeds);
        model.addAttribute("takenMeds", takenMeds);
        model.addAttribute("range", range);
        model.addAttribute("activeMedsList", activeMeds);
        model.addAttribute("waterLogs", recentWaterLogs);
        model.addAttribute("history", historyGrouped);
        model.addAttribute("chartData", chartData);
        model.addAttribute("searchDate", searchDateStr);
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
    public String getNutritionPage(
            @RequestParam(value = "date", required = false) String dateStr,
            Model model) {
        Patient patient = getCurrentPatient();
        if (patient == null) {
            return "redirect:/auth/login";
        }
        if ("NEW".equals(patient.getStatus())) {
            return "redirect:/patient/appointments";
        }

        LocalDate targetDate = LocalDate.now();
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            try {
                targetDate = LocalDate.parse(dateStr);
            } catch (Exception ignored) {}
        }

        List<PatientMeal> meals = new ArrayList<>();
        int totalCalories = 0;
        double totalSalt = 0.0;
        double totalFiber = 0.0;
        double totalCarbs = 0.0;
        double totalFat = 0.0;
        double totalProtein = 0.0;

        int targetCalories = 1400;
        double targetSalt = 2.5;
        double targetFiber = 20.0;
        double targetCarbs = 150.0;
        double targetFat = 50.0;
        double targetProtein = 60.0;

        if (patient.getId() != null) {
            NutritionRule currentRule = patientService.findCurrentNutritionRule(patient.getId()).orElse(null);
            if (currentRule != null) {
                if (currentRule.getMaxCaloriesPerDay() != null) targetCalories = currentRule.getMaxCaloriesPerDay();
                if (currentRule.getMaxSaltG() != null) targetSalt = currentRule.getMaxSaltG().doubleValue();
                if (currentRule.getMinFiberG() != null) targetFiber = currentRule.getMinFiberG().doubleValue();
                if (currentRule.getMaxCarbsG() != null) targetCarbs = currentRule.getMaxCarbsG().doubleValue();
                if (currentRule.getMaxFatG() != null) targetFat = currentRule.getMaxFatG().doubleValue();
                if (currentRule.getMinProteinG() != null) targetProtein = currentRule.getMinProteinG().doubleValue();
            }
        }

        try {
            if (patient.getId() != null) {
                meals = patientService.findMealsByDate(patient.getId(), targetDate);
                for (PatientMeal m : meals) {
                    if (m.getCalories() != null) totalCalories += m.getCalories();
                    if (m.getSaltG() != null) totalSalt += m.getSaltG();
                    if (m.getFiberG() != null) totalFiber += m.getFiberG();
                    if (m.getGlucidG() != null) totalCarbs += m.getGlucidG();
                    if (m.getLipidG() != null) totalFat += m.getLipidG();
                    if (m.getProteinG() != null) totalProtein += m.getProteinG();
                }
            }
        } catch (Exception ignored) {
        }

        int caloriesPercent = targetCalories > 0 ? (totalCalories * 100 / targetCalories) : 0;
        int saltPercent = targetSalt > 0 ? (int)(totalSalt * 100 / targetSalt) : 0;
        int fiberPercent = targetFiber > 0 ? (int)(totalFiber * 100 / targetFiber) : 0;
        int carbsPercent = targetCarbs > 0 ? (int)(totalCarbs * 100 / targetCarbs) : 0;
        int fatPercent = targetFat > 0 ? (int)(totalFat * 100 / targetFat) : 0;
        int proteinPercent = targetProtein > 0 ? (int)(totalProtein * 100 / targetProtein) : 0;

        if (caloriesPercent > 100) caloriesPercent = 100;
        if (saltPercent > 100) saltPercent = 100;
        if (fiberPercent > 100) fiberPercent = 100;
        if (carbsPercent > 100) carbsPercent = 100;
        if (fatPercent > 100) fatPercent = 100;
        if (proteinPercent > 100) proteinPercent = 100;

        model.addAttribute("patient", patient);
        model.addAttribute("meals", meals);
        model.addAttribute("currentDate", targetDate.toString());
        model.addAttribute("totalCalories", totalCalories);
        model.addAttribute("totalSalt", Math.round(totalSalt * 10.0) / 10.0);
        model.addAttribute("totalFiber", Math.round(totalFiber * 10.0) / 10.0);
        model.addAttribute("totalCarbs", Math.round(totalCarbs * 10.0) / 10.0);
        model.addAttribute("totalFat", Math.round(totalFat * 10.0) / 10.0);
        model.addAttribute("totalProtein", Math.round(totalProtein * 10.0) / 10.0);

        model.addAttribute("targetCalories", targetCalories);
        model.addAttribute("targetSalt", targetSalt);
        model.addAttribute("targetFiber", targetFiber);
        model.addAttribute("targetCarbs", targetCarbs);
        model.addAttribute("targetFat", targetFat);
        model.addAttribute("targetProtein", targetProtein);

        model.addAttribute("caloriesPercent", caloriesPercent);
        model.addAttribute("saltPercent", saltPercent);
        model.addAttribute("fiberPercent", fiberPercent);
        model.addAttribute("carbsPercent", carbsPercent);
        model.addAttribute("fatPercent", fatPercent);
        model.addAttribute("proteinPercent", proteinPercent);
        
        List<FoodDictionary> foods = patientService.findActiveFoods();
        model.addAttribute("foods", foods);

        return "patient/nutrition";
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
        model.addAttribute("latestBmi", exerciseLogService.getLatestBmi(patient.getId()));
        return "patient/progress";
    }

    @PostMapping("/progress/update")
    public String updateProfile(
            @RequestParam("phone") String phone,
            @RequestParam("address") String address,
            @RequestParam(value = "currentPassword", required = false) String currentPassword,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "emergencyContactName", required = false) String emergencyContactName,
            @RequestParam(value = "emergencyContactPhone", required = false) String emergencyContactPhone,
            RedirectAttributes redirectAttributes
    ) {
        Patient patient = getCurrentPatient();
        if (patient == null) {
            return "redirect:/auth/login";
        }

        // Validate personal phone format
        if (phone == null || !phone.matches("0[35789][0-9]{8}")) {
            redirectAttributes.addFlashAttribute("error", "Số điện thoại cá nhân không hợp lệ!");
            return "redirect:/patient/progress";
        }

        // Validate emergency phone format if provided
        if (emergencyContactPhone != null && !emergencyContactPhone.trim().isEmpty()) {
            if (!emergencyContactPhone.matches("0[35789][0-9]{8}")) {
                redirectAttributes.addFlashAttribute("error", "Số điện thoại người thân không hợp lệ!");
                return "redirect:/patient/progress";
            }
        }

        // Check password change request and validate current password
        boolean isChangingPassword = password != null && !password.trim().isEmpty();
        if (isChangingPassword) {
            if (password.length() < 6 || password.length() > 50) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu mới phải từ 6 đến 50 ký tự!");
                return "redirect:/patient/progress";
            }
            if (currentPassword == null || currentPassword.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng nhập mật khẩu hiện tại để xác nhận đổi mật khẩu mới!");
                return "redirect:/patient/progress";
            }
            Account account = patient.getAccount();
            if (account == null || !passwordEncoder.matches(currentPassword, account.getPasswordHash())) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu hiện tại không đúng!");
                return "redirect:/patient/progress";
            }
        }

        patientService.updateProfile(patient, phone, address, emergencyContactName, emergencyContactPhone, isChangingPassword ? password : null);

        return "redirect:/patient/progress?updateSuccess=true";
    }

    private String getBpStatusText(Integer sys, Integer dia, com.rpm.remotepatientmonitoring.model.AlertThreshold threshold) {
        if (sys == null || dia == null) return "Chưa nhập";
        int sysEmerg = threshold != null && threshold.getSystolicEmergencyThreshold() != null ? threshold.getSystolicEmergencyThreshold() : 180;
        int diaEmerg = threshold != null && threshold.getDiastolicEmergencyThreshold() != null ? threshold.getDiastolicEmergencyThreshold() : 110;
        int sysDangMin = threshold != null && threshold.getSystolicDangerMin() != null ? threshold.getSystolicDangerMin() : 140;
        int diaDangMin = threshold != null && threshold.getDiastolicDangerMin() != null ? threshold.getDiastolicDangerMin() : 90;
        int sysWarnMin = threshold != null && threshold.getSystolicWarningMin() != null ? threshold.getSystolicWarningMin() : 130;
        int diaWarnMin = threshold != null && threshold.getDiastolicWarningMin() != null ? threshold.getDiastolicWarningMin() : 85;

        if (sys >= sysEmerg || dia >= diaEmerg) return "Nguy hiểm";
        if (sys >= sysDangMin || dia >= diaDangMin) return "Vượt ngưỡng";
        if (sys >= sysWarnMin || dia >= diaWarnMin) return "Cần chú ý";
        return "Đạt mục tiêu";
    }

    private String getBpClass(Integer sys, Integer dia, com.rpm.remotepatientmonitoring.model.AlertThreshold threshold) {
        if (sys == null || dia == null) return "text-danger fw-bold";
        int sysEmerg = threshold != null && threshold.getSystolicEmergencyThreshold() != null ? threshold.getSystolicEmergencyThreshold() : 180;
        int diaEmerg = threshold != null && threshold.getDiastolicEmergencyThreshold() != null ? threshold.getDiastolicEmergencyThreshold() : 110;
        int sysDangMin = threshold != null && threshold.getSystolicDangerMin() != null ? threshold.getSystolicDangerMin() : 140;
        int diaDangMin = threshold != null && threshold.getDiastolicDangerMin() != null ? threshold.getDiastolicDangerMin() : 90;
        int sysWarnMin = threshold != null && threshold.getSystolicWarningMin() != null ? threshold.getSystolicWarningMin() : 130;
        int diaWarnMin = threshold != null && threshold.getDiastolicWarningMin() != null ? threshold.getDiastolicWarningMin() : 85;

        if (sys >= sysEmerg || dia >= diaEmerg) return "text-danger fw-bold";
        if (sys >= sysDangMin || dia >= diaDangMin) return "text-warning fw-bold"; // Optional: Use text-warning or a custom class if text-orange exists.
        if (sys >= sysWarnMin || dia >= diaWarnMin) return "text-warning fw-bold";
        return "text-success fw-bold";
    }

    private String getGlucoseStatusText(java.math.BigDecimal val, com.rpm.remotepatientmonitoring.model.AlertThreshold threshold) {
        if (val == null) return "Chưa nhập";
        double glu = val.doubleValue();
        double hypo = threshold != null && threshold.getGlucoseHypoThreshold() != null ? threshold.getGlucoseHypoThreshold().doubleValue() : 4.4;
        double highMax = threshold != null && threshold.getGlucoseHighMax() != null ? threshold.getGlucoseHighMax().doubleValue() : 16.0;
        double normMax = threshold != null && threshold.getGlucoseNormalMax() != null ? threshold.getGlucoseNormalMax().doubleValue() : 10.0;

        if (glu < hypo) return "Nguy hiểm (Hạ)";
        if (glu > highMax) return "Nguy hiểm (Cao)";
        if (glu > normMax) return "Vượt ngưỡng";
        return "Đạt mục tiêu";
    }

    private String getGlucoseClass(java.math.BigDecimal val, com.rpm.remotepatientmonitoring.model.AlertThreshold threshold) {
        if (val == null) return "text-danger fw-bold";
        double glu = val.doubleValue();
        double hypo = threshold != null && threshold.getGlucoseHypoThreshold() != null ? threshold.getGlucoseHypoThreshold().doubleValue() : 4.4;
        double highMax = threshold != null && threshold.getGlucoseHighMax() != null ? threshold.getGlucoseHighMax().doubleValue() : 16.0;
        double normMax = threshold != null && threshold.getGlucoseNormalMax() != null ? threshold.getGlucoseNormalMax().doubleValue() : 10.0;

        if (glu < hypo || glu > highMax) return "text-danger fw-bold";
        if (glu > normMax) return "text-warning fw-bold";
        return "text-success fw-bold";
    }
}
