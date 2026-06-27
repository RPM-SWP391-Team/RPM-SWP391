package com.rpm.remotepatientmonitoring.controller.patient;

import com.rpm.remotepatientmonitoring.model.Notification;
import com.rpm.remotepatientmonitoring.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/patient/notifications")
public class PatientNotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping("/{id}/read-redirect")
    public String markAsReadAndRedirect(@PathVariable Integer id, HttpServletRequest request) {
        notificationRepository.findById(id).ifPresent(notification -> {
            if (!notification.getIsRead()) {
                notification.setIsRead(true);
                notificationRepository.save(notification);
            }
        });
        
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/patient/dashboard");
    }

    @Autowired
    private com.rpm.remotepatientmonitoring.repository.AccountRepository accountRepository;

    @Autowired
    private com.rpm.remotepatientmonitoring.repository.PatientRepository patientRepository;

    private com.rpm.remotepatientmonitoring.model.Patient getCurrentPatient() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof com.rpm.remotepatientmonitoring.model.Account) {
            com.rpm.remotepatientmonitoring.model.Account account = (com.rpm.remotepatientmonitoring.model.Account) principal;
            return patientRepository.findByAccountId(account.getId()).orElse(null);
        } else if (principal instanceof org.springframework.security.core.userdetails.User) {
            String email = ((org.springframework.security.core.userdetails.User) principal).getUsername();
            return accountRepository.findByEmail(email)
                    .flatMap(acc -> patientRepository.findByAccountId(acc.getId()))
                    .orElse(null);
        }
        return null;
    }

    @GetMapping("/read-all-redirect")
    public String markAllAsReadAndRedirect(HttpServletRequest request) {
        com.rpm.remotepatientmonitoring.model.Patient patient = getCurrentPatient();
        if (patient != null) {
            java.util.List<Notification> unread = notificationRepository.findByPatientIdOrderByCreatedAtDesc(patient.getId())
                    .stream().filter(n -> !n.getIsRead()).collect(java.util.stream.Collectors.toList());
            for (Notification n : unread) {
                n.setIsRead(true);
            }
            notificationRepository.saveAll(unread);
        }
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/patient/dashboard");
    }
}
