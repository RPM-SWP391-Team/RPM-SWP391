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
import java.time.LocalDate;

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
        if (amount == null || amount <= 0 || amount > 10000) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Lượng nước không hợp lệ (từ 1 đến 10000 ml)."
            ));
        }
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

    @PostMapping("/history/save")
    public ResponseEntity<Map<String, Object>> saveWaterHistory(
            @RequestParam(value = "id", required = false) Integer id,
            @RequestParam("date") String dateStr,
            @RequestParam("amount") Integer amount) {
        if (amount == null || amount < 0 || amount > 10000) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Lượng nước không hợp lệ (từ 0 đến 10000 ml)."
            ));
        }
        LocalDate date;
        try {
            date = LocalDate.parse(dateStr);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Định dạng ngày không hợp lệ."
            ));
        }
        Patient patient = getCurrentPatient();
        patientService.addOrUpdateWaterLog(id, patient, date, amount);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Lưu lịch sử uống nước thành công!"
        ));
    }

    @PostMapping("/history/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteWaterHistory(@PathVariable("id") Integer id) {
        patientService.deleteWaterLog(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Xóa lịch sử uống nước thành công!"
        ));
    }
}
