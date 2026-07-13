package com.rpm.remotepatientmonitoring.scheduler;

import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.model.Notification;
import com.rpm.remotepatientmonitoring.repository.PatientRepository;
import com.rpm.remotepatientmonitoring.repository.NotificationRepository;
import com.rpm.remotepatientmonitoring.service.patient.ExerciseLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class ExerciseReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(ExerciseReminderScheduler.class);

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private ExerciseLogService exerciseLogService;

    @Autowired
    private NotificationRepository notificationRepository;

    /**
     * Chạy định kỳ mỗi ngày vào lúc 17:00 để gửi cảnh báo mất streak.
     * Cron: "0 0 17 * * *" (giây=0, phút=0, giờ=17, hàng ngày)
     */
    @Scheduled(cron = "0 0 17 * * *")
    public void sendStreakAtRiskReminders() {
        log.info("Bắt đầu chạy job quét nguy cơ mất streak tập luyện lúc 17h chiều.");
        List<Patient> patients = patientRepository.findAll();
        LocalDate today = LocalDate.now();

        for (Patient p : patients) {
            try {
                if (exerciseLogService.isStreakAtRiskToday(p.getId())) {
                    // Kiểm tra xem hôm nay đã gửi thông báo EXERCISE_STREAK_AT_RISK nào chưa (để tránh gửi trùng lặp)
                    List<Notification> allNotifs = notificationRepository.findByPatientIdOrderByCreatedAtDesc(p.getId());
                    boolean alreadySent = allNotifs.stream()
                            .anyMatch(n -> "EXERCISE_STREAK_AT_RISK".equals(n.getNotificationType())
                                    && n.getCreatedAt().toLocalDate().isEqual(today));

                    if (!alreadySent) {
                        int streak = exerciseLogService.getCurrentStreak(p.getId());
                        String content = "Bạn sắp mất chuỗi " + streak + " ngày liên tiếp! Hãy vận động ngay hôm nay để giữ streak nhé.";

                        Notification notif = Notification.builder()
                                .patient(p)
                                .recipientType("PATIENT")
                                .recipientId(p.getId())
                                .notificationType("EXERCISE_STREAK_AT_RISK")
                                .channel("IN_APP")
                                .status("SENT")
                                .title("Cảnh báo sắp mất streak")
                                .content(content)
                                .isRead(false)
                                .createdAt(LocalDateTime.now())
                                .build();

                        notificationRepository.save(notif);
                        log.info("Đã gửi cảnh báo mất streak cho patientId={}, streak={}", p.getId(), streak);
                    }
                }
            } catch (Exception e) {
                log.error("Lỗi khi xử lý cảnh báo mất streak cho patientId={}: {}", p.getId(), e.getMessage(), e);
            }
        }
    }
}
