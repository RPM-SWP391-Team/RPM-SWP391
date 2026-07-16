package com.rpm.remotepatientmonitoring.controller.patient;

import com.rpm.remotepatientmonitoring.dto.patient.DailyHealthLogFormDto;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.service.patient.PatientHealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import com.rpm.remotepatientmonitoring.config.CustomUserDetails;
import jakarta.validation.Valid;

import java.util.List;

@Controller
@RequestMapping("/patient")
public class HealthLogController {

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

    @GetMapping("/log")
    public String showLogForm(Model model) {
        Patient patient = getCurrentPatient();
        if (patient == null) {
            return "redirect:/auth/login";
        }
        if ("NEW".equals(patient.getStatus())) {
            return "redirect:/patient/appointments";
        }

        DailyHealthLogFormDto formDto = new DailyHealthLogFormDto();
        formDto.setLogType("MORNING");
        model.addAttribute("healthLog", formDto);
        model.addAttribute("patient", patient);
        return "patient/log";
    }

    @PostMapping("/log")
    public String processLogForm(@Valid @ModelAttribute("healthLog") DailyHealthLogFormDto formDto, BindingResult bindingResult, Model model) {
        Patient patient = getCurrentPatient();
        if (patient == null) {
            return "redirect:/auth/login";
        }
        if ("NEW".equals(patient.getStatus())) {
            return "redirect:/patient/appointments";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("patient", patient);
            return "patient/log";
        }

        patientHealthService.saveDailyHealthLog(patient, formDto);

        return "redirect:/patient/dashboard?logSuccess=true";
    }
}
