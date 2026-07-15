package com.rpm.remotepatientmonitoring.service.patient;

import com.rpm.remotepatientmonitoring.model.Notification;
import com.rpm.remotepatientmonitoring.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PatientNotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Transactional
    public void markAsRead(Integer id) {
        Optional<Notification> notificationOptional = notificationRepository.findById(id);
        if (notificationOptional.isPresent()) {
            Notification notification = notificationOptional.get();
            if (notification.getIsRead() == false) {
                notification.setIsRead(true);
                notificationRepository.save(notification);
            }
        }
    }

    @Transactional
    public void markAllAsRead(Integer patientId) {
        List<Notification> allNotifications = notificationRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
        List<Notification> unreadNotifications = new ArrayList<>();
        for (int i = 0; i < allNotifications.size(); i++) {
            Notification notification = allNotifications.get(i);
            if (notification.getIsRead() == false) {
                notification.setIsRead(true);
                unreadNotifications.add(notification);
            }
        }
        if (!unreadNotifications.isEmpty()) {
            notificationRepository.saveAll(unreadNotifications);
        }
    }

    public Optional<Notification> getNotificationById(Integer id) {
        return notificationRepository.findById(id);
    }
}
