package com.rpm.remotepatientmonitoring.controller;

import com.rpm.remotepatientmonitoring.config.CustomUserDetails;
import com.rpm.remotepatientmonitoring.model.Notification;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.repository.NotificationRepository;
import com.rpm.remotepatientmonitoring.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/patient/notifications")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PatientRepository patientRepository;

    private Patient getPatient(CustomUserDetails userDetails) {
        if (userDetails != null && userDetails.getAccount() != null) {
            Optional<Patient> opt = patientRepository.findByAccountId(userDetails.getAccount().getId());
            if (opt.isPresent()) {
                return opt.get();
            }
        }
        
        List<Patient> all = patientRepository.findAll();
        if (all.size() > 0) {
            return all.get(0);
        }
        return null;
    }

    // 1. GET: Lấy danh sách thông báo của bệnh nhân
    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Patient patient = getPatient(userDetails);
        if (patient == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        List<Notification> list = notificationRepository.findByPatientIdOrderByCreatedAtDesc(patient.getId());
        return ResponseEntity.ok(list);
    }

    // 2. GET: Lấy số lượng thông báo chưa đọc
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Patient patient = getPatient(userDetails);
        if (patient == null) {
            return ResponseEntity.ok(0L);
        }
        long count = notificationRepository.countByPatientIdAndIsReadFalse(patient.getId());
        return ResponseEntity.ok(count);
    }

    // 3. POST: Đánh dấu một thông báo là đã đọc
    @PostMapping("/{id}/read")
    public ResponseEntity<String> markAsRead(@PathVariable("id") Integer id) {
        Optional<Notification> opt = notificationRepository.findById(id);
        if (opt.isPresent()) {
            Notification notif = opt.get();
            notif.setIsRead(true);
            notificationRepository.save(notif);
            return ResponseEntity.ok("Đã đánh dấu đọc thành công");
        }
        return ResponseEntity.notFound().build();
    }
}
