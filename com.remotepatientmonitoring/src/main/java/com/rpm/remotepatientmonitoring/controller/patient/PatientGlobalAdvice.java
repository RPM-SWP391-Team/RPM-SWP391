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
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Collections;

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
        for (Notification n : existingNotifications) {
            if (n.getCreatedAt().toLocalDate().isEqual(today)) {
                String type = n.getNotificationType();
                if (type != null) {
                    todayNotifKeys.add(type);
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
