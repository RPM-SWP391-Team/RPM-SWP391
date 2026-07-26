package com.rpm.remotepatientmonitoring.controller.patient;

import com.rpm.remotepatientmonitoring.model.Notification;

import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.config.CustomUserDetails;
import com.rpm.remotepatientmonitoring.service.patient.PatientHealthService;
import com.rpm.remotepatientmonitoring.service.patient.PatientNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/patient/notifications")
public class PatientNotificationController {

    @Autowired
    private PatientNotificationService patientNotificationService;

    @Autowired
    private PatientHealthService patientHealthService;

    private Patient getCurrentPatient() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) principal;
            return patientHealthService.getPatientByAccountId(userDetails.getAccount().getId());
        }
        
        List<Patient> all = patientHealthService.getAllPatients();
        if (!all.isEmpty()) {
            return all.get(0);
        }
        return null;
    }

    @GetMapping("/{id}/read-redirect")
    public String markAsReadAndRedirect(@PathVariable Integer id, HttpServletRequest request) {
        Optional<Notification> notificationOptional = 
                patientNotificationService.getNotificationById(id);

        String targetUrl = null;

        if (notificationOptional.isPresent()) {
            Notification notification = notificationOptional.get();
            patientNotificationService.markAsRead(id);

            String type = notification.getNotificationType();
            if (type != null) {
                if (type.startsWith("EXERCISE") || type.equals("EXERCISE_REMINDER") || type.equals("EXERCISE_STREAK_MILESTONE") || type.equals("EXERCISE_BP_REMINDER") || type.equals("EXERCISE_INACTIVITY_REMINDER")) {
                    targetUrl = "/patient/exercise";
                } else if (type.startsWith("DIET") || type.equals("DIET_REMINDER")) {
                    targetUrl = "/patient/nutrition";
                } else if (type.contains("MED_REMINDER")) {
                    targetUrl = "/patient/adherence";
                } else if (type.equals("HEALTH_LOG_REMINDER")) {
                    targetUrl = "/patient/dashboard";
                }
            }
        }

        if (targetUrl != null) {
            return "redirect:" + targetUrl;
        }

        String referer = request.getHeader("Referer");
        if (referer != null) {
            return "redirect:" + referer;
        } else {
            return "redirect:/patient/dashboard";
        }
    }

    @GetMapping("/read-all-redirect")
    public String markAllAsReadAndRedirect(HttpServletRequest request) {
        Patient patient = getCurrentPatient();
        if (patient != null) {
            patientNotificationService.markAllAsRead(patient.getId());
        }

        String referer = request.getHeader("Referer");
        if (referer != null) {
            return "redirect:" + referer;
        } else {
            return "redirect:/patient/dashboard";
        }
    }
}
