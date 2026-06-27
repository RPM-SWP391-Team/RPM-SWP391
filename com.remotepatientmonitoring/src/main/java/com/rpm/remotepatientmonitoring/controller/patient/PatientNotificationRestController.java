package com.rpm.remotepatientmonitoring.controller.patient;

import com.rpm.remotepatientmonitoring.config.CustomUserDetails;
import com.rpm.remotepatientmonitoring.model.Notification;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.repository.NotificationRepository;
import com.rpm.remotepatientmonitoring.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/patient/notifications")
public class PatientNotificationRestController {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PatientRepository patientRepository;

    private Patient getCurrentPatient() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            Object principal = auth.getPrincipal();
            if (principal instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) principal;
                Optional<Patient> opt = patientRepository.findByAccountId(userDetails.getAccount().getId());
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

    @GetMapping("/unread-count")
    public long getUnreadCount() {
        Patient patient = getCurrentPatient();
        if (patient == null) {
            return 0;
        }
        return notificationRepository.countByPatientIdAndIsReadFalse(patient.getId());
    }

    @GetMapping
    public List<Notification> getNotifications() {
        Patient patient = getCurrentPatient();
        if (patient == null) {
            return new ArrayList<>();
        }
        return notificationRepository.findByPatientIdOrderByCreatedAtDesc(patient.getId());
    }

    @PostMapping("/{id}/read")
    public void markAsRead(@PathVariable("id") Integer id) {
        Optional<Notification> opt = notificationRepository.findById(id);
        if (opt.isPresent()) {
            Notification n = opt.get();
            n.setIsRead(true);
            notificationRepository.save(n);
        }
    }
}
