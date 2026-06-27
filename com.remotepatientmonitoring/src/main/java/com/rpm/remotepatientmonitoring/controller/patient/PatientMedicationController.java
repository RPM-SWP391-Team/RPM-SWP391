package com.rpm.remotepatientmonitoring.controller.patient;

import com.rpm.remotepatientmonitoring.model.MedicationLog;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.model.PatientMedication;
import com.rpm.remotepatientmonitoring.repository.MedicationLogRepository;
import com.rpm.remotepatientmonitoring.repository.PatientMedicationRepository;
import com.rpm.remotepatientmonitoring.repository.PatientRepository;
import com.rpm.remotepatientmonitoring.config.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/patient/api/medications")
public class PatientMedicationController {

    @Autowired
    private PatientMedicationRepository medicationRepository;

    @Autowired
    private MedicationLogRepository medicationLogRepository;

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
            Map<String, Object> errRes = new HashMap<>();
            errRes.put("success", false);
            errRes.put("message", valError);
            return ResponseEntity.badRequest().body(errRes);
        }

        Patient patient = getCurrentPatient();
        if (patient == null) {
            Map<String, Object> errRes = new HashMap<>();
            errRes.put("success", false);
            errRes.put("message", "Chưa đăng nhập.");
            return ResponseEntity.badRequest().body(errRes);
        }

        PatientMedication medication = PatientMedication.builder()
                .patient(patient)
                .medicineName(medicineName.trim())
                .dosage(dosage.trim())
                .scheduledTime(scheduledTime.trim())
                .build();

        PatientMedication saved = medicationRepository.save(medication);

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", "Thêm thuốc thành công!");
        res.put("medicationId", saved.getId());
        res.put("medicineName", saved.getMedicineName());
        res.put("dosage", saved.getDosage());
        res.put("scheduledTime", saved.getScheduledTime());

        return ResponseEntity.ok(res);
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<Map<String, Object>> updateMedication(
            @PathVariable("id") Integer id,
            @RequestParam("medicineName") String medicineName,
            @RequestParam("dosage") String dosage,
            @RequestParam("scheduledTime") String scheduledTime) {

        String valError = validateMedicationInput(medicineName, dosage, scheduledTime);
        if (valError != null) {
            Map<String, Object> errRes = new HashMap<>();
            errRes.put("success", false);
            errRes.put("message", valError);
            return ResponseEntity.badRequest().body(errRes);
        }

        Optional<PatientMedication> optMed = medicationRepository.findById(id);
        if (!optMed.isPresent()) {
            Map<String, Object> errRes = new HashMap<>();
            errRes.put("success", false);
            errRes.put("message", "Không tìm thấy thuốc với ID: " + id);
            return ResponseEntity.badRequest().body(errRes);
        }

        PatientMedication medication = optMed.get();
        medication.setMedicineName(medicineName.trim());
        medication.setDosage(dosage.trim());
        medication.setScheduledTime(scheduledTime.trim());
        medication.setUpdatedAt(LocalDateTime.now());

        medicationRepository.save(medication);

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", "Cập nhật thuốc thành công!");
        res.put("medicationId", medication.getId());
        res.put("medicineName", medication.getMedicineName());
        res.put("dosage", medication.getDosage());
        res.put("scheduledTime", medication.getScheduledTime());

        return ResponseEntity.ok(res);
    }

    @PostMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteMedication(@PathVariable("id") Integer id) {
        Optional<PatientMedication> optMed = medicationRepository.findById(id);
        if (!optMed.isPresent()) {
            Map<String, Object> errRes = new HashMap<>();
            errRes.put("success", false);
            errRes.put("message", "Không tìm thấy thuốc với ID: " + id);
            return ResponseEntity.badRequest().body(errRes);
        }

        PatientMedication medication = optMed.get();
        medication.setIsActive(false);
        medication.setUpdatedAt(LocalDateTime.now());
        medicationRepository.save(medication);

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", "Đã xóa thuốc: " + medication.getMedicineName());

        return ResponseEntity.ok(res);
    }

    @PostMapping("/toggle-take")
    public ResponseEntity<Map<String, Object>> toggleTakeMedication(
            @RequestParam("medicationId") Integer medicationId,
            @RequestParam("status") boolean status) {

        Optional<PatientMedication> optMed = medicationRepository.findById(medicationId);
        if (!optMed.isPresent()) {
            Map<String, Object> errRes = new HashMap<>();
            errRes.put("success", false);
            errRes.put("message", "Không tìm thấy thuốc với ID: " + medicationId);
            return ResponseEntity.badRequest().body(errRes);
        }

        PatientMedication medication = optMed.get();
        LocalDate today = LocalDate.now();

        Optional<MedicationLog> optLog = medicationLogRepository.findByPatientMedicationIdAndLogDate(medicationId, today);
        MedicationLog log;
        if (optLog.isPresent()) {
            log = optLog.get();
        } else {
            MedicationLog newLog = MedicationLog.builder()
                    .patientMedication(medication)
                    .logDate(today)
                    .isTaken(false)
                    .build();
            log = medicationLogRepository.save(newLog);
        }

        log.setIsTaken(status);
        log.setTakenAt(status ? LocalDateTime.now() : null);
        medicationLogRepository.save(log);

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", status ? "Đã đánh dấu uống thuốc: " + medication.getMedicineName()
                                  : "Đã hủy đánh dấu: " + medication.getMedicineName());
        res.put("medicationId", medicationId);
        res.put("status", status);
        res.put("medicineName", medication.getMedicineName());

        return ResponseEntity.ok(res);
    }
}
