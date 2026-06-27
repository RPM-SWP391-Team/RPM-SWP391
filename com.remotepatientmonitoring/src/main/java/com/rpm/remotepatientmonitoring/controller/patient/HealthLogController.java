package com.rpm.remotepatientmonitoring.controller.patient;

import com.rpm.remotepatientmonitoring.model.DailyHealthLog;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.repository.HealthLogRepository;
import com.rpm.remotepatientmonitoring.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import com.rpm.remotepatientmonitoring.config.CustomUserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/patient")
public class HealthLogController {

    @Autowired
    private HealthLogRepository healthLogRepository;

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

    @GetMapping("/log")
    public String showLogForm(Model model) {
        Patient patient = getCurrentPatient();
        if (patient == null) {
            return "redirect:/auth/login";
        }
        if ("NEW".equals(patient.getStatus())) {
            return "redirect:/patient/appointments";
        }

        DailyHealthLog log = DailyHealthLog.builder()
                .logDate(LocalDate.now())
                .logTime(LocalDateTime.now())
                .inputMethod("MANUAL")
                .isOcrValidated(false)
                .isAlertProcessed(false)
                .build();
        model.addAttribute("healthLog", log);
        model.addAttribute("patient", patient);
        return "patient/log";
    }

    @PostMapping("/log")
    public String processLogForm(@ModelAttribute("healthLog") DailyHealthLog healthLog) {
        Patient patient = getCurrentPatient();
        if (patient == null) {
            return "redirect:/auth/login";
        }
        if ("NEW".equals(patient.getStatus())) {
            return "redirect:/patient/appointments";
        }
        
        healthLog.setPatient(patient);
        if (healthLog.getLogDate() == null) {
            healthLog.setLogDate(LocalDate.now());
        }
        if (healthLog.getLogTime() == null) {
            healthLog.setLogTime(LocalDateTime.now());
        }
        healthLog.setInputMethod("MANUAL");
        healthLog.setIsOcrValidated(false);
        healthLog.setIsAlertProcessed(false);
        healthLog.setCreatedAt(LocalDateTime.now());

        healthLogRepository.save(healthLog);

        return "redirect:/patient/dashboard?logSuccess=true";
    }
}
