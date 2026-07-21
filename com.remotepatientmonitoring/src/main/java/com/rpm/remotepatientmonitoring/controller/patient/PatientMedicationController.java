package com.rpm.remotepatientmonitoring.controller.patient;

import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.model.PatientMedication;
import com.rpm.remotepatientmonitoring.config.CustomUserDetails;
import com.rpm.remotepatientmonitoring.service.patient.PatientHealthService;
import com.rpm.remotepatientmonitoring.service.patient.PatientMedicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@RequestMapping("/patient/api/medications")
public class PatientMedicationController {

    @Autowired
    private PatientMedicationService patientMedicationService;

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
        if (!all.isEmpty()) {
            return all.get(0);
        }
        throw new IllegalStateException("Không tìm thấy bệnh nhân trong hệ thống.");
    }

    private String validateMedicationInput(String medicineName, String dosage, String scheduledTime) {
        if (medicineName == null || medicineName.trim().isEmpty()) {
            return "Tên thuốc không được để trống.";
        }
        if (medicineName.trim().length() < 2 || medicineName.trim().length() > 100) {
            return "Tên thuốc phải từ 2 đến 100 ký tự.";
        }
        if (dosage == null || dosage.trim().isEmpty()) {
            return "Liều lượng không được để trống.";
        }
        if (dosage.trim().length() < 1 || dosage.trim().length() > 50) {
            return "Liều lượng phải từ 1 đến 50 ký tự.";
        }
        if (!dosage.trim().matches("^\\d+(\\.\\d+|/\\d+)?\\s*[a-zA-Z\\p{L}]+$")) {
            return "Liều lượng phải bắt đầu bằng số và đi kèm đơn vị (ví dụ: '1 viên', '10ml', '1/2 viên').";
        }
        if (scheduledTime == null || scheduledTime.trim().isEmpty()) {
            return "Giờ uống thuốc không được để trống.";
        }
        if (!scheduledTime.trim().matches("^(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$")) {
            return "Giờ uống thuốc không đúng định dạng (HH:mm).";
        }
        return null;
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addMedication(
            @RequestParam("medicineName") String medicineName,
            @RequestParam("dosage") String dosage,
            @RequestParam("scheduledTime") String scheduledTime) {

        String valError = validateMedicationInput(medicineName, dosage, scheduledTime);
        if (valError != null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", valError
            ));
        }

        Patient patient = getCurrentPatient();
        PatientMedication saved = patientMedicationService.addMedication(patient, medicineName, dosage, scheduledTime);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Thêm thuốc thành công!",
                "medicationId", saved.getId(),
                "medicineName", saved.getMedicineName(),
                "dosage", saved.getDosage(),
                "scheduledTime", saved.getScheduledTime()
        ));
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<Map<String, Object>> updateMedication(
            @PathVariable("id") Integer id,
            @RequestParam("medicineName") String medicineName,
            @RequestParam("dosage") String dosage,
            @RequestParam("scheduledTime") String scheduledTime) {

        String valError = validateMedicationInput(medicineName, dosage, scheduledTime);
        if (valError != null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", valError
            ));
        }

        PatientMedication medication = patientMedicationService.updateMedication(id, medicineName, dosage, scheduledTime);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Cập nhật thuốc thành công!",
                "medicationId", medication.getId(),
                "medicineName", medication.getMedicineName(),
                "dosage", medication.getDosage(),
                "scheduledTime", medication.getScheduledTime()
        ));
    }

    @PostMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteMedication(@PathVariable("id") Integer id) {
        PatientMedication medication = patientMedicationService.deleteMedication(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã xóa thuốc: " + medication.getMedicineName()
        ));
    }

    @PostMapping("/toggle-take")
    public ResponseEntity<Map<String, Object>> toggleTakeMedication(
            @RequestParam("medicationId") Integer medicationId,
            @RequestParam("status") boolean status) {

        patientMedicationService.toggleTakeMedication(medicationId, status);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", status ? "Đã đánh dấu uống thuốc thành công" : "Đã hủy đánh dấu thành công",
                "medicationId", medicationId,
                "status", status
        ));
    }

    @PostMapping("/history/save")
    public ResponseEntity<Map<String, Object>> saveMedicationHistory(
            @RequestParam("medicationId") Integer medicationId,
            @RequestParam("date") String dateStr,
            @RequestParam("isTaken") boolean isTaken,
            @RequestParam(value = "takenTime", required = false) String takenTimeStr) {
        
        LocalDate date;
        try {
            date = LocalDate.parse(dateStr);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Định dạng ngày không hợp lệ."
            ));
        }

        LocalDateTime takenAt = null;
        if (isTaken) {
            if (takenTimeStr != null && !takenTimeStr.trim().isEmpty()) {
                try {
                    LocalTime time = LocalTime.parse(takenTimeStr.trim());
                    takenAt = LocalDateTime.of(date, time);
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "message", "Định dạng giờ không hợp lệ (HH:mm)."
                    ));
                }
            } else {
                takenAt = LocalDateTime.of(date, LocalTime.now());
            }
        }

        patientMedicationService.addOrUpdateMedicationLog(medicationId, date, isTaken, takenAt);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Lưu lịch sử uống thuốc thành công!"
        ));
    }

    @PostMapping("/history/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteMedicationHistory(@PathVariable("id") Integer id) {
        patientMedicationService.deleteMedicationLog(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Xóa lịch sử uống thuốc thành công!"
        ));
    }
}
