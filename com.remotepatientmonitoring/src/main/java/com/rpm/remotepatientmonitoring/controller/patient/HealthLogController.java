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
        return null;
    }

    @GetMapping("/log")
    public String showLogForm(
            @org.springframework.web.bind.annotation.RequestParam(value = "id", required = false) Integer id,
            Model model) {
        Patient patient = getCurrentPatient();
        if (patient == null) {
            return "redirect:/auth/login";
        }
        if ("NEW".equals(patient.getStatus())) {
            return "redirect:/patient/appointments";
        }

        DailyHealthLogFormDto formDto = new DailyHealthLogFormDto();
        if (id != null) {
            com.rpm.remotepatientmonitoring.model.DailyHealthLog existing = patientHealthService.getDailyHealthLogById(id);
            if (existing != null && existing.getPatient().getId().equals(patient.getId())) {
                formDto.setLogType(existing.getLogType());
                formDto.setInputMethod(existing.getInputMethod());
                formDto.setSystolicBp(existing.getSystolicBp());
                formDto.setDiastolicBp(existing.getDiastolicBp());
                formDto.setHeartRate(existing.getHeartRate());
                formDto.setGlucoseLevel(existing.getGlucoseLevel());
                formDto.setPatientNotes(existing.getPatientNotes());
                model.addAttribute("logId", id);
            } else {
                formDto.setLogType("MORNING");
            }
        } else {
            formDto.setLogType("MORNING");
        }
        model.addAttribute("healthLog", formDto);
        model.addAttribute("org.springframework.validation.BindingResult.healthLog", 
            new org.springframework.validation.BeanPropertyBindingResult(formDto, "healthLog"));
        model.addAttribute("patient", patient);
        model.addAttribute("healthLogs", patientHealthService.getRecentDailyHealthLogs(patient.getId()));
        return "patient/log";
    }

    @PostMapping("/log")
    public String processLogForm(
            @org.springframework.web.bind.annotation.RequestParam(value = "id", required = false) Integer id,
            @Valid @ModelAttribute("healthLog") DailyHealthLogFormDto formDto,
            BindingResult bindingResult,
            Model model) {
        Patient patient = getCurrentPatient();
        if (patient == null) {
            return "redirect:/auth/login";
        }
        if ("NEW".equals(patient.getStatus())) {
            return "redirect:/patient/appointments";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("patient", patient);
            model.addAttribute("healthLogs", patientHealthService.getRecentDailyHealthLogs(patient.getId()));
            if (id != null) {
                model.addAttribute("logId", id);
            }
            return "patient/log";
        }

        patientHealthService.saveDailyHealthLog(id, patient, formDto);

        return "redirect:/patient/log?saveSuccess=true";
    }

    @PostMapping("/log/delete")
    public String deleteLog(
            @org.springframework.web.bind.annotation.RequestParam("id") Integer id,
            jakarta.servlet.http.HttpServletRequest request) {
        Patient patient = getCurrentPatient();
        if (patient == null) {
            return "redirect:/auth/login";
        }
        patientHealthService.deleteDailyHealthLog(id);
        
        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("/appointments")) {
            return "redirect:/patient/appointments?deleteLogSuccess=true";
        }
        return "redirect:/patient/log?deleteSuccess=true";
    }
}
