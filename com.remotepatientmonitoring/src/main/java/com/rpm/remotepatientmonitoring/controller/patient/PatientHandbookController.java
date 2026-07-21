package com.rpm.remotepatientmonitoring.controller.patient;

import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.model.EmergencyProtocol;
import com.rpm.remotepatientmonitoring.config.CustomUserDetails;
import com.rpm.remotepatientmonitoring.service.patient.PatientHealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/patient")
public class PatientHandbookController {

    @Autowired
    private PatientHealthService healthService;

    private Patient getCurrentPatient() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            Object principal = auth.getPrincipal();
            if (principal instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) principal;
                Patient patient = healthService.getPatientByAccountId(userDetails.getAccount().getId());
                if (patient != null) {
                    return patient;
                }
            }
        }
        List<Patient> allPatients = healthService.getAllPatients();
        if (!allPatients.isEmpty()) {
            return allPatients.get(0);
        }
        return null;
    }

    @GetMapping("/handbook")
    public String showHandbook(Model model) {
        Patient patient = getCurrentPatient();
        List<EmergencyProtocol> bpProtocols = new ArrayList<>();
        List<EmergencyProtocol> glucoseProtocols = new ArrayList<>();

        if (patient != null && patient.getHospital() != null) {
            List<EmergencyProtocol> protocols = healthService.getEmergencyProtocols(patient.getHospital().getId());
            for (EmergencyProtocol p : protocols) {
                String type = p.getConditionType();
                if ("HYPERTENSIVE_CRISIS".equalsIgnoreCase(type)) {
                    bpProtocols.add(p);
                } else if ("HYPOGLYCEMIA".equalsIgnoreCase(type) || "HYPERGLYCEMIA".equalsIgnoreCase(type)) {
                    glucoseProtocols.add(p);
                }
            }
        }

        model.addAttribute("bpProtocols", bpProtocols);
        model.addAttribute("glucoseProtocols", glucoseProtocols);
        return "patient/handbook";
    }
}
