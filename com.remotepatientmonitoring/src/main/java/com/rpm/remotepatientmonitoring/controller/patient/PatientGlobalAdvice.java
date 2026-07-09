package com.rpm.remotepatientmonitoring.controller.patient;

import com.rpm.remotepatientmonitoring.model.Account;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.model.PatientMedication;
import com.rpm.remotepatientmonitoring.model.MedicationLog;
import com.rpm.remotepatientmonitoring.model.DailyHealthLog;
import com.rpm.remotepatientmonitoring.repository.NotificationRepository;
import com.rpm.remotepatientmonitoring.repository.PatientRepository;
import com.rpm.remotepatientmonitoring.repository.PatientMedicationRepository;
import com.rpm.remotepatientmonitoring.repository.MedicationLogRepository;
import com.rpm.remotepatientmonitoring.repository.HealthLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;
import com.rpm.remotepatientmonitoring.model.Notification;
import com.rpm.remotepatientmonitoring.model.ExerciseLog;
import com.rpm.remotepatientmonitoring.model.PatientMeal;
import com.rpm.remotepatientmonitoring.model.NutritionRule;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@ControllerAdvice(basePackages = "com.rpm.remotepatientmonitoring.controller.patient")
public class PatientGlobalAdvice {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private com.rpm.remotepatientmonitoring.repository.AccountRepository accountRepository;

    @Autowired
    private PatientMedicationRepository patientMedicationRepository;

    @Autowired
    private MedicationLogRepository medicationLogRepository;

    @Autowired
    private HealthLogRepository healthLogRepository;

    @Autowired
    private com.rpm.remotepatientmonitoring.repository.ExerciseLogRepository exerciseLogRepository;

    @Autowired
    private com.rpm.remotepatientmonitoring.service.patient.ExerciseLogService exerciseLogService;

    @Autowired
    private com.rpm.remotepatientmonitoring.repository.PatientMealRepository patientMealRepository;

    @Autowired
    private com.rpm.remotepatientmonitoring.repository.NutritionRuleRepository nutritionRuleRepository;

    private Patient getCurrentPatient() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
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

    private void checkAndGenerateReminders(Patient patient) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        
        // Fetch all patient notifications for today to prevent duplicates
        List<Notification> existingNotifications = notificationRepository.findByPatientIdOrderByCreatedAtDesc(patient.getId());
        
        // Helper to check if a specific notification type was already created today
        java.util.Set<String> todayNotifKeys = new java.util.HashSet<>();
        
        // Specific checks for Exercise and Diet reminders (to prevent duplicating different text with the same notification type)
        boolean hasMissedExerciseNotif = false;
        boolean hasAchievedExerciseNotif = false;
        boolean hasExcessiveExerciseNotif = false;

        boolean hasBreakfastDietNotif = false;
        boolean hasLunchDietNotif = false;
        boolean hasDinnerDietNotif = false;
        boolean hasCalLimitDietNotif = false;
        boolean hasSaltLimitDietNotif = false;
        boolean hasCarbLimitDietNotif = false;

        for (Notification n : existingNotifications) {
            if (n.getCreatedAt().toLocalDate().isEqual(today)) {
                String type = n.getNotificationType();
                if (type != null) {
                    todayNotifKeys.add(type);
                }
                
                if ("EXERCISE_REMINDER".equals(type)) {
                    String content = n.getContent();
                    if (content.contains("chưa ghi nhận vận động")) {
                        hasMissedExerciseNotif = true;
                    } else if (content.contains("đạt mục tiêu vận động")) {
                        hasAchievedExerciseNotif = true;
                    } else if (content.contains("vận động khá nhiều")) {
                        hasExcessiveExerciseNotif = true;
                    }
                } else if ("DIET_REMINDER".equals(type)) {
                    String content = n.getContent();
                    if (content.contains("bữa sáng")) {
                        hasBreakfastDietNotif = true;
                    } else if (content.contains("bữa trưa")) {
                        hasLunchDietNotif = true;
                    } else if (content.contains("bữa tối")) {
                        hasDinnerDietNotif = true;
                    } else if (content.contains("lượng Calo")) {
                        hasCalLimitDietNotif = true;
                    } else if (content.contains("lượng muối")) {
                        hasSaltLimitDietNotif = true;
                    } else if (content.contains("lượng Carbohydrate") || content.contains("lượng Carbs")) {
                        hasCarbLimitDietNotif = true;
                    }
                }
            }
        }

        // Fetch patient medications
        List<PatientMedication> activeMeds = patientMedicationRepository.findByPatientIdAndIsActiveTrue(patient.getId());
        
        for (PatientMedication med : activeMeds) {
            String schedStr = med.getScheduledTime();
            if (schedStr == null || !schedStr.matches("^\\d{2}:\\d{2}$")) {
                continue;
            }
            
            try {
                LocalTime scheduledTime = LocalTime.parse(schedStr);
                
                // 1. Check upcoming medication: within 30 minutes before scheduledTime
                long minutesUntil = java.time.Duration.between(now, scheduledTime).toMinutes();
                if (minutesUntil >= 0 && minutesUntil <= 30) {
                    String upcomingTypeKey = "UPCOMING_MED_REMINDER_" + med.getId();
                    if (!todayNotifKeys.contains(upcomingTypeKey)) {
                        String content = "Sắp đến giờ uống thuốc: " + med.getMedicineName() + " (" + med.getDosage() + ") lúc " + schedStr + ".";
                        Notification notif = Notification.builder()
                                .patient(patient)
                                .recipientType("PATIENT")
                                .recipientId(patient.getId())
                                .notificationType(upcomingTypeKey)
                                .channel("IN_APP")
                                .status("SENT")
                                .title("Nhắc nhở uống thuốc sắp tới")
                                .content(content)
                                .isRead(false)
                                .createdAt(LocalDateTime.now())
                                .build();
                        notificationRepository.save(notif);
                        todayNotifKeys.add(upcomingTypeKey);
                    }
                }
                
                // 2. Check overdue logging: past scheduledTime and not logged taken
                if (now.isAfter(scheduledTime)) {
                    boolean logged = medicationLogRepository.findByPatientMedicationIdAndLogDate(med.getId(), today)
                            .map(log -> Boolean.TRUE.equals(log.getIsTaken()))
                            .orElse(false);
                    
                    if (!logged) {
                        String overdueTypeKey = "OVERDUE_MED_REMINDER_" + med.getId();
                        if (!todayNotifKeys.contains(overdueTypeKey)) {
                            String content = "Đã quá giờ hẹn uống thuốc: " + med.getMedicineName() + " (" + med.getDosage() + ") lúc " + schedStr + " nhưng bạn chưa ghi nhận kết quả.";
                            Notification notif = Notification.builder()
                                    .patient(patient)
                                    .recipientType("PATIENT")
                                    .recipientId(patient.getId())
                                    .notificationType(overdueTypeKey)
                                    .channel("IN_APP")
                                    .status("SENT")
                                    .title("Nhắc nhở quá giờ uống thuốc")
                                    .content(content)
                                    .isRead(false)
                                    .createdAt(LocalDateTime.now())
                                    .build();
                            notificationRepository.save(notif);
                            todayNotifKeys.add(overdueTypeKey);
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }
        
        // 3. Check health log reminder: past 20:00 (8 PM) and no health log submitted today
        if (now.isAfter(LocalTime.of(20, 0))) {
            List<DailyHealthLog> todayLogs = healthLogRepository.findByPatientIdAndLogDate(patient.getId(), today);
            if (todayLogs.isEmpty()) {
                String healthTypeKey = "HEALTH_LOG_REMINDER";
                if (!todayNotifKeys.contains(healthTypeKey)) {
                    Notification notif = Notification.builder()
                            .patient(patient)
                            .recipientType("PATIENT")
                            .recipientId(patient.getId())
                            .notificationType(healthTypeKey)
                            .channel("IN_APP")
                            .status("SENT")
                            .title("Nhắc nhở nhập chỉ số sức khỏe")
                            .content("Đã quá 20:00 nhưng bạn chưa nhập chỉ số sức khỏe (huyết áp, đường huyết...) cho hôm nay. Vui lòng ghi nhận kết quả.")
                            .isRead(false)
                            .createdAt(LocalDateTime.now())
                            .build();
                    notificationRepository.save(notif);
                    todayNotifKeys.add(healthTypeKey);
                }
            }
        }

        // 4. Exercise reminders:
        // 4.1 Missed exercise reminder: past 20:00 (8 PM) and no exercise log recorded today
        if (now.isAfter(LocalTime.of(20, 0))) {
            List<ExerciseLog> todayExercises = exerciseLogRepository.findByPatientIdAndLogDate(patient.getId(), today);
            if (todayExercises.isEmpty()) {
                if (!hasMissedExerciseNotif) {
                    Notification notif = Notification.builder()
                            .patient(patient)
                            .recipientType("PATIENT")
                            .recipientId(patient.getId())
                            .notificationType("EXERCISE_REMINDER")
                            .channel("IN_APP")
                            .status("SENT")
                            .title("Nhắc nhở tập luyện")
                            .content("Bạn chưa ghi nhận vận động hôm nay. Hãy dành ít phút hoạt động để đạt mục tiêu sức khỏe nhé!")
                            .isRead(false)
                            .createdAt(LocalDateTime.now())
                            .build();
                    notificationRepository.save(notif);
                }
            }
        }

        // 4.2 Achieved daily goal warning / Excessive exercise check
        try {
            Map<String, Object> exerciseSummary = exerciseLogService.getTodaySummary(patient.getId());
            int totalMinutes = exerciseSummary.get("totalMinutes") != null ? (int) exerciseSummary.get("totalMinutes") : 0;
            int targetMinutes = exerciseSummary.get("targetMinutes") != null ? (int) exerciseSummary.get("targetMinutes") : 30;

            if (totalMinutes >= targetMinutes && totalMinutes > 0) {
                if (!hasAchievedExerciseNotif) {
                    Notification notif = Notification.builder()
                            .patient(patient)
                            .recipientType("PATIENT")
                            .recipientId(patient.getId())
                            .notificationType("EXERCISE_REMINDER")
                            .channel("IN_APP")
                            .status("SENT")
                            .title("Đạt mục tiêu tập luyện")
                            .content("Chúc mừng! Bạn đã đạt mục tiêu vận động hôm nay 🎉")
                            .isRead(false)
                            .createdAt(LocalDateTime.now())
                            .build();
                    notificationRepository.save(notif);
                }
            }

            if (totalMinutes > 120) {
                if (!hasExcessiveExerciseNotif) {
                    Notification notif = Notification.builder()
                            .patient(patient)
                            .recipientType("PATIENT")
                            .recipientId(patient.getId())
                            .notificationType("EXERCISE_REMINDER")
                            .channel("IN_APP")
                            .status("SENT")
                            .title("Cảnh báo vận động quá mức")
                            .content("Bạn đã vận động khá nhiều hôm nay (120+ phút). Hãy chú ý theo dõi huyết áp sau khi tập và nghỉ ngơi đầy đủ nhé.")
                            .isRead(false)
                            .createdAt(LocalDateTime.now())
                            .build();
                    notificationRepository.save(notif);
                }
            }
        } catch (Exception ignored) {
        }

        // 5. Diet reminders:
        try {
            List<PatientMeal> todayMeals = patientMealRepository.findByPatientIdAndLogDate(patient.getId(), today);
            boolean hasBreakfast = false;
            boolean hasLunch = false;
            boolean hasDinner = false;
            int totalCalories = 0;
            double totalSalt = 0.0;
            double totalCarbs = 0.0;

            for (PatientMeal m : todayMeals) {
                String mt = m.getMealType();
                if ("BREAKFAST".equalsIgnoreCase(mt)) {
                    hasBreakfast = true;
                } else if ("LUNCH".equalsIgnoreCase(mt)) {
                    hasLunch = true;
                } else if ("DINNER".equalsIgnoreCase(mt)) {
                    hasDinner = true;
                }

                if (m.getCalories() != null) totalCalories += m.getCalories();
                if (m.getSaltG() != null) totalSalt += m.getSaltG();
                if (m.getGlucidG() != null) totalCarbs += m.getGlucidG();
            }

            // Breakfast reminder: past 9:00 AM
            if (now.isAfter(LocalTime.of(9, 0))) {
                if (!hasBreakfast && !hasBreakfastDietNotif) {
                    Notification notif = Notification.builder()
                            .patient(patient)
                            .recipientType("PATIENT")
                            .recipientId(patient.getId())
                            .notificationType("DIET_REMINDER")
                            .channel("IN_APP")
                            .status("SENT")
                            .title("Nhắc nhở ghi nhận bữa ăn")
                            .content("Bạn chưa ghi nhận bữa sáng hôm nay.")
                            .isRead(false)
                            .createdAt(LocalDateTime.now())
                            .build();
                    notificationRepository.save(notif);
                }
            }

            // Lunch reminder: past 14:00 (2 PM)
            if (now.isAfter(LocalTime.of(14, 0))) {
                if (!hasLunch && !hasLunchDietNotif) {
                    Notification notif = Notification.builder()
                            .patient(patient)
                            .recipientType("PATIENT")
                            .recipientId(patient.getId())
                            .notificationType("DIET_REMINDER")
                            .channel("IN_APP")
                            .status("SENT")
                            .title("Nhắc nhở ghi nhận bữa ăn")
                            .content("Bạn chưa ghi nhận bữa trưa hôm nay.")
                            .isRead(false)
                            .createdAt(LocalDateTime.now())
                            .build();
                    notificationRepository.save(notif);
                }
            }

            // Dinner reminder: past 21:00 (9 PM)
            if (now.isAfter(LocalTime.of(21, 0))) {
                if (!hasDinner && !hasDinnerDietNotif) {
                    Notification notif = Notification.builder()
                            .patient(patient)
                            .recipientType("PATIENT")
                            .recipientId(patient.getId())
                            .notificationType("DIET_REMINDER")
                            .channel("IN_APP")
                            .status("SENT")
                            .title("Nhắc nhở ghi nhận bữa ăn")
                            .content("Bạn chưa ghi nhận bữa tối hôm nay.")
                            .isRead(false)
                            .createdAt(LocalDateTime.now())
                            .build();
                    notificationRepository.save(notif);
                }
            }

            // Check rules limits
            Optional<NutritionRule> ruleOpt = nutritionRuleRepository.findByPatientIdAndIsCurrent(patient.getId(), true);
            if (ruleOpt.isPresent()) {
                NutritionRule rule = ruleOpt.get();
                
                if (rule.getMaxCaloriesPerDay() != null && totalCalories > rule.getMaxCaloriesPerDay()) {
                    if (!hasCalLimitDietNotif) {
                        Notification notif = Notification.builder()
                                .patient(patient)
                                .recipientType("PATIENT")
                                .recipientId(patient.getId())
                                .notificationType("DIET_REMINDER")
                                .channel("IN_APP")
                                .status("SENT")
                                .title("Nhắc nhở dinh dưỡng")
                                .content("Hôm nay bạn đã tiêu thụ " + totalCalories + " kcal, vượt quá giới hạn lượng Calo hàng ngày (" + rule.getMaxCaloriesPerDay() + " kcal).")
                                .isRead(false)
                                .createdAt(LocalDateTime.now())
                                .build();
                        notificationRepository.save(notif);
                    }
                }

                if (rule.getMaxSaltG() != null && totalSalt > rule.getMaxSaltG().doubleValue()) {
                    if (!hasSaltLimitDietNotif) {
                        Notification notif = Notification.builder()
                                .patient(patient)
                                .recipientType("PATIENT")
                                .recipientId(patient.getId())
                                .notificationType("DIET_REMINDER")
                                .channel("IN_APP")
                                .status("SENT")
                                .title("Nhắc nhở dinh dưỡng")
                                .content("Hôm nay lượng muối bạn tiêu thụ (" + String.format("%.1f", totalSalt) + "g) đã vượt quá giới hạn hàng ngày (" + String.format("%.1f", rule.getMaxSaltG().doubleValue()) + "g).")
                                .isRead(false)
                                .createdAt(LocalDateTime.now())
                                .build();
                        notificationRepository.save(notif);
                    }
                }

                if (rule.getMaxCarbsG() != null && totalCarbs > rule.getMaxCarbsG().doubleValue()) {
                    if (!hasCarbLimitDietNotif) {
                        Notification notif = Notification.builder()
                                .patient(patient)
                                .recipientType("PATIENT")
                                .recipientId(patient.getId())
                                .notificationType("DIET_REMINDER")
                                .channel("IN_APP")
                                .status("SENT")
                                .title("Nhắc nhở dinh dưỡng")
                                .content("Hôm nay lượng Carbohydrate bạn tiêu thụ (" + String.format("%.1f", totalCarbs) + "g) đã vượt quá giới hạn hàng ngày (" + String.format("%.1f", rule.getMaxCarbsG().doubleValue()) + "g).")
                                .isRead(false)
                                .createdAt(LocalDateTime.now())
                                .build();
                        notificationRepository.save(notif);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    @ModelAttribute
    public void addPatientNotificationsToModel(Model model) {
        Patient patient = getCurrentPatient();
        if (patient != null) {
            // Generate reminders on the fly
            checkAndGenerateReminders(patient);
            
            List<Notification> patientNotifications = notificationRepository.findByPatientIdOrderByCreatedAtDesc(patient.getId());
            long patientUnreadCount = notificationRepository.countByPatientIdAndIsReadFalse(patient.getId());
            
            // Limit to top 10 notifications for the dropdown
            if (patientNotifications.size() > 10) {
                patientNotifications = patientNotifications.subList(0, 10);
            }
            
            model.addAttribute("patientNotifications", patientNotifications);
            model.addAttribute("patientUnreadCount", patientUnreadCount);
        } else {
            model.addAttribute("patientNotifications", Collections.emptyList());
            model.addAttribute("patientUnreadCount", 0L);
        }
    }
}
