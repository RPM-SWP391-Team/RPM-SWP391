package com.rpm.remotepatientmonitoring.controller.patient;

import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.model.WaterLog;
import com.rpm.remotepatientmonitoring.repository.PatientRepository;
import com.rpm.remotepatientmonitoring.repository.WaterLogRepository;
import com.rpm.remotepatientmonitoring.config.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        if (all.size() > 0) {
            return all.get(0);
        }
        return null;
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addWater(@RequestParam("amount") Integer amount) {
        Patient patient = getCurrentPatient();
        if (patient == null) {
            Map<String, Object> errRes = new HashMap<>();
            errRes.put("success", false);
            errRes.put("message", "Chưa đăng nhập.");
            return ResponseEntity.badRequest().body(errRes);
        }

        LocalDate today = LocalDate.now();

        Optional<WaterLog> optLog = waterLogRepository.findByPatientIdAndLogDate(patient.getId(), today);
        WaterLog log;
        if (optLog.isPresent()) {
            log = optLog.get();
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

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", "Ghi nhận uống nước thành công!");
        res.put("currentAmount", log.getAmountMl());

        return ResponseEntity.ok(res);
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetWater() {
        Patient patient = getCurrentPatient();
        if (patient == null) {
            Map<String, Object> errRes = new HashMap<>();
            errRes.put("success", false);
            errRes.put("message", "Chưa đăng nhập.");
            return ResponseEntity.badRequest().body(errRes);
        }

        LocalDate today = LocalDate.now();

        Optional<WaterLog> optLog = waterLogRepository.findByPatientIdAndLogDate(patient.getId(), today);
        if (optLog.isPresent()) {
            WaterLog log = optLog.get();
            log.setAmountMl(0);
            waterLogRepository.save(log);
        }

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", "Đặt lại tiến độ uống nước thành công!");
        res.put("currentAmount", 0);

        return ResponseEntity.ok(res);
    }
}
