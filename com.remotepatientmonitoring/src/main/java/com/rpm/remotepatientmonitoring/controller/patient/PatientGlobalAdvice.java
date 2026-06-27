package com.rpm.remotepatientmonitoring.controller.patient;

import com.rpm.remotepatientmonitoring.model.Account;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.repository.NotificationRepository;
import com.rpm.remotepatientmonitoring.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;
import com.rpm.remotepatientmonitoring.model.Notification;
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

    private Patient getCurrentPatient() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof Account) {
            Account account = (Account) principal;
            return patientRepository.findByAccountId(account.getId()).orElse(null);
        } else if (principal instanceof org.springframework.security.core.userdetails.User) {
            String email = ((org.springframework.security.core.userdetails.User) principal).getUsername();
            return accountRepository.findByEmail(email)
                    .flatMap(acc -> patientRepository.findByAccountId(acc.getId()))
                    .orElse(null);
        }
        return null;
    }

    @ModelAttribute
    public void addPatientNotificationsToModel(Model model) {
        Patient patient = getCurrentPatient();
        if (patient != null) {
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
