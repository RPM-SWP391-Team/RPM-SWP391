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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/patient")
public class HealthLogController {

    @Autowired
    private HealthLogRepository healthLogRepository;

    @Autowired
    private PatientRepository patientRepository;

    @GetMapping("/log")
    public String showLogForm(Model model) {
        Patient patient = patientRepository.findAll().stream().findFirst().orElse(null);
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
        Patient patient = patientRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No patient found in the database. Please initialize data first."));
        
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
