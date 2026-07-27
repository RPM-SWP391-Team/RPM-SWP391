package com.rpm.remotepatientmonitoring.service.patient;

import com.rpm.remotepatientmonitoring.model.*;
import com.rpm.remotepatientmonitoring.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class PatientGlobalService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PatientMedicationRepository patientMedicationRepository;

    @Autowired
    private MedicationLogRepository medicationLogRepository;

    @Autowired
    private HealthLogRepository healthLogRepository;

    @Autowired
    private ExerciseLogRepository exerciseLogRepository;

    @Autowired
    private ExerciseLogService exerciseLogService;

    @Autowired
    private PatientMealRepository patientMealRepository;

    @Autowired
    private NutritionRuleRepository nutritionRuleRepository;

    @Transactional
    public void checkAndGenerateReminders(Patient patient) {
        if (patient == null || "NEW".equalsIgnoreCase(patient.getStatus()) || "PENDING".equalsIgnoreCase(patient.getStatus()) || patient.getHospital() == null) {
            return;
        }
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        
        List<Notification> existingNotifications = notificationRepository.findByPatientIdOrderByCreatedAtDesc(patient.getId());
        Set<String> todayNotifKeys = new HashSet<>();
        
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

        List<PatientMedication> activeMeds = patientMedicationRepository.findByPatientIdAndIsActiveTrue(patient.getId());
        
        for (int i = 0; i < activeMeds.size(); i++) {
            PatientMedication med = activeMeds.get(i);
            try {
                String schedStr = med.getScheduledTime();
                if (schedStr == null || !schedStr.matches("^\\d{2}:\\d{2}$")) {
                    continue;
                }
                LocalTime scheduledTime = LocalTime.parse(schedStr);
                
                LocalTime reminderStart = scheduledTime.minusMinutes(30);
                if (now.isAfter(reminderStart) && now.isBefore(scheduledTime)) {
                    String upcomingTypeKey = "UPCOMING_MED_REMINDER_" + med.getId();
                    if (!todayNotifKeys.contains(upcomingTypeKey)) {
                        String content = "Bạn có lịch hẹn uống thuốc: " + med.getMedicineName() + " (" + med.getDosage() + ") lúc " + schedStr + ". Vui lòng chuẩn bị và ghi nhận kết quả.";
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
                
                if (now.isAfter(scheduledTime)) {
                    boolean logged = false;
                    Optional<MedicationLog> logOpt = 
                        medicationLogRepository.findByPatientMedicationIdAndLogDate(med.getId(), today);
                    if (logOpt.isPresent()) {
                        MedicationLog log = logOpt.get();
                        if (Boolean.TRUE.equals(log.getIsTaken())) {
                            logged = true;
                        }
                    }
                    
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

    public List<Notification> getTop10Notifications(Integer patientId) {
        List<Notification> patientNotifications = notificationRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
        if (patientNotifications.size() > 10) {
            patientNotifications = patientNotifications.subList(0, 10);
        }
        return patientNotifications;
    }

    public long getUnreadCount(Integer patientId) {
        return notificationRepository.countByPatientIdAndIsReadFalse(patientId);
    }
}
