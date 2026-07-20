package com.rpm.remotepatientmonitoring.controller.patient;

import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.model.Notification;
import com.rpm.remotepatientmonitoring.config.CustomUserDetails;
import com.rpm.remotepatientmonitoring.service.patient.PatientGlobalService;
import com.rpm.remotepatientmonitoring.service.patient.PatientHealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

import java.util.Collections;
import java.util.List;

@ControllerAdvice(basePackages = "com.rpm.remotepatientmonitoring.controller.patient")
public class PatientGlobalAdvice {

    @Autowired
    private PatientGlobalService patientGlobalService;

    @Autowired
    private PatientHealthService patientHealthService;

    private Patient getCurrentPatient() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            Object principal = auth.getPrincipal();
            if (principal instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) principal;
                Patient patient = patientHealthService.getPatientByAccountId(userDetails.getAccount().getId());
                if (patient != null) {
                    return patient;
                }
            }
        }
        List<Patient> all = patientHealthService.getAllPatients();
        if (all.size() > 0) {
            return all.get(0);
        }
        return null;
    }

    @ModelAttribute
    public void addPatientNotificationsToModel(Model model) {
        Patient patient = getCurrentPatient();
        if (patient != null) {
            // Generate reminders on the fly
            patientGlobalService.checkAndGenerateReminders(patient);
            
            List<Notification> patientNotifications = patientGlobalService.getTop10Notifications(patient.getId());
            long patientUnreadCount = patientGlobalService.getUnreadCount(patient.getId());
            
            model.addAttribute("patientNotifications", patientNotifications);
            model.addAttribute("patientUnreadCount", patientUnreadCount);
        } else {
            model.addAttribute("patientNotifications", Collections.emptyList());
            model.addAttribute("patientUnreadCount", 0L);
        }
    }
}
