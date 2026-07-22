package com.rpm.remotepatientmonitoring.scheduler;

import com.rpm.remotepatientmonitoring.model.Appointment;
import com.rpm.remotepatientmonitoring.model.Notification;
import com.rpm.remotepatientmonitoring.repository.AppointmentRepository;
import com.rpm.remotepatientmonitoring.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class AppointmentReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(AppointmentReminderScheduler.class);

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    /**
     * Chạy định kỳ hàng ngày lúc 08:00 để quét các lịch hẹn sắp diễn ra trong vòng 2 ngày (48 giờ)
     * và chưa được gửi nhắc nhở.
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void sendAppointmentReminders() {
        log.info("Bắt đầu chạy job gửi nhắc nhở lịch hẹn tái khám trước 2 ngày.");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoDaysLater = now.plusDays(2);

        // Tìm tất cả các lịch hẹn ở trạng thái ACCEPTED, chưa gửi nhắc nhở và có thời gian hẹn trong 2 ngày tới
        List<Appointment> upcomingAppointments = appointmentRepository
                .findByStatusAndReminderSent2daysFalseAndAppointmentTimeBetween("ACCEPTED", now, twoDaysLater);

        log.info("Tìm thấy {} lịch hẹn sắp diễn ra cần gửi nhắc nhở.", upcomingAppointments.size());

        for (Appointment appt : upcomingAppointments) {
            try {
                if (appt.getPatient() != null) {
                    String doctorName = appt.getDoctor() != null ? appt.getDoctor().getFullName() : "Bác sĩ";
                    String locationStr = appt.getLocation() != null ? appt.getLocation() : "Phòng khám";
                    String formattedTime = appt.getAppointmentTime().format(formatter);

                    String content = String.format("Bạn có lịch hẹn tái khám với %s vào lúc %s tại %s. Vui lòng đến đúng giờ!",
                            doctorName, formattedTime, locationStr);

                    Notification notif = Notification.builder()
                            .patient(appt.getPatient())
                            .doctor(appt.getDoctor())
                            .recipientType("PATIENT")
                            .recipientId(appt.getPatient().getId())
                            .notificationType("APPOINTMENT_REMINDER")
                            .channel("IN_APP")
                            .status("SENT")
                            .title("Nhắc nhở lịch hẹn tái khám")
                            .content(content)
                            .isRead(false)
                            .createdAt(LocalDateTime.now())
                            .build();

                    notificationRepository.save(notif);
                    
                    appt.setReminderSent2days(true);
                    appt.setUpdatedAt(LocalDateTime.now());
                    appointmentRepository.save(appt);

                    log.info("Đã gửi nhắc nhở tái khám trước 2 ngày cho bệnh nhân {}, id lịch hẹn={}", 
                            appt.getPatient().getFullName(), appt.getId());
                }
            } catch (Exception e) {
                log.error("Lỗi khi xử lý nhắc nhở lịch hẹn id={}: {}", appt.getId(), e.getMessage(), e);
            }
        }
    }
}
