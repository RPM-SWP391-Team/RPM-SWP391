package com.rpm.remotepatientmonitoring.controller.patient;

import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.model.WaterLog;
import com.rpm.remotepatientmonitoring.config.CustomUserDetails;
import com.rpm.remotepatientmonitoring.repository.PatientRepository;
import com.rpm.remotepatientmonitoring.repository.WaterLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Map;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/patient/api/water")
public class PatientWaterController {

    @Autowired
    private WaterLogRepository waterLogRepository;

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
        if (all.isEmpty() == false) {
            return all.get(0);
        }
        throw new IllegalStateException("Không tìm thấy bệnh nhân trong hệ thống.");
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addWater(@RequestParam("amount") Integer amount) {
        Patient patient = getCurrentPatient();
        LocalDate today = LocalDate.now();

        Optional<WaterLog> logOpt = waterLogRepository.findByPatientIdAndLogDate(patient.getId(), today);
        WaterLog log;
        if (logOpt.isPresent()) {
            log = logOpt.get();
        } else {
            log = WaterLog.builder()
                    .patient(patient)
                    .logDate(today)
                    .amountMl(0)
                    .build();
        }

        log.setAmountMl(log.getAmountMl() + amount);
        if (log.getAmountMl() < 0) {
            log.setAmountMl(0);
        }
        waterLogRepository.save(log);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Ghi nhận uống nước thành công!",
                "currentAmount", log.getAmountMl()
        ));
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetWater() {
        Patient patient = getCurrentPatient();
        LocalDate today = LocalDate.now();

        Optional<WaterLog> logOpt = waterLogRepository.findByPatientIdAndLogDate(patient.getId(), today);
        WaterLog log = null;
        if (logOpt.isPresent()) {
            log = logOpt.get();
        }

        if (log != null) {
            log.setAmountMl(0);
            waterLogRepository.save(log);
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đặt lại tiến độ uống nước thành công!",
                "currentAmount", 0
        ));
    }
}
