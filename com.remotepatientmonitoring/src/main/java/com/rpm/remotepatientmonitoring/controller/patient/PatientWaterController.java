package com.rpm.remotepatientmonitoring.controller.patient;

import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.config.CustomUserDetails;
import com.rpm.remotepatientmonitoring.service.patient.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/patient/api/water")
public class PatientWaterController {

    @Autowired
    private PatientService patientService;

    private Patient getCurrentPatient() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            Object principal = auth.getPrincipal();
            if (principal instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) principal;
                Optional<Patient> opt = patientService.findByAccountId(userDetails.getAccount().getId());
                if (opt.isPresent()) {
                    return opt.get();
                }
            }
        }
        List<Patient> all = patientService.findAllPatients();
        if (all.isEmpty() == false) {
            return all.get(0);
        }
        throw new IllegalStateException("Không tìm thấy bệnh nhân trong hệ thống.");
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addWater(@RequestParam("amount") Integer amount) {
        Patient patient = getCurrentPatient();
        int currentAmount = patientService.addWater(patient, amount);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Ghi nhận uống nước thành công!",
                "currentAmount", currentAmount
        ));
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetWater() {
        Patient patient = getCurrentPatient();
        int currentAmount = patientService.resetWater(patient);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đặt lại tiến độ uống nước thành công!",
                "currentAmount", currentAmount
        ));
    }
}
