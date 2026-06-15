package com.rpm.remotepatientmonitoring.controller.patient;

import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.model.WaterLog;
import com.rpm.remotepatientmonitoring.repository.PatientRepository;
import com.rpm.remotepatientmonitoring.repository.patient.WaterLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/patient/api/water")
public class PatientWaterController {

    @Autowired
    private WaterLogRepository waterLogRepository;

    @Autowired
    private PatientRepository patientRepository;

    private Patient getCurrentPatient() {
        return patientRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy bệnh nhân trong hệ thống."));
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addWater(@RequestParam("amount") Integer amount) {
        Patient patient = getCurrentPatient();
        LocalDate today = LocalDate.now();

        WaterLog log = waterLogRepository.findByPatientIdAndLogDate(patient.getId(), today)
                .orElseGet(() -> WaterLog.builder()
                        .patient(patient)
                        .logDate(today)
                        .amountMl(0)
                        .build());

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

        WaterLog log = waterLogRepository.findByPatientIdAndLogDate(patient.getId(), today)
                .orElse(null);

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
