package com.rpm.remotepatientmonitoring.controller.patient;

import com.rpm.remotepatientmonitoring.model.MedicationLog;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.model.PatientMedication;
import com.rpm.remotepatientmonitoring.model.Notification;
import com.rpm.remotepatientmonitoring.config.CustomUserDetails;
import com.rpm.remotepatientmonitoring.repository.MedicationLogRepository;
import com.rpm.remotepatientmonitoring.repository.PatientMedicationRepository;
import com.rpm.remotepatientmonitoring.repository.PatientRepository;
import com.rpm.remotepatientmonitoring.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;
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

    @Autowired
    private NotificationRepository notificationRepository;

    // ===================== Lấy Patient =====================
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

    // ===================== 1. Thêm mới thuốc =====================
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

        PatientMedication medication = PatientMedication.builder()
                .patient(patient)
                .medicineName(medicineName.trim())
                .dosage(dosage.trim())
                .scheduledTime(scheduledTime.trim())
                .build();

        PatientMedication saved = medicationRepository.save(medication);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Thêm thuốc thành công!",
                "medicationId", saved.getId(),
                "medicineName", saved.getMedicineName(),
                "dosage", saved.getDosage(),
                "scheduledTime", saved.getScheduledTime()
        ));
    }

    // ===================== 2. Chỉnh sửa thuốc =====================
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

        Optional<PatientMedication> medOpt = medicationRepository.findById(id);
        if (medOpt.isPresent() == false) {
            throw new IllegalStateException("Không tìm thấy thuốc với ID: " + id);
        }
        PatientMedication medication = medOpt.get();

        medication.setMedicineName(medicineName.trim());
        medication.setDosage(dosage.trim());
        medication.setScheduledTime(scheduledTime.trim());
        medication.setUpdatedAt(LocalDateTime.now());

        medicationRepository.save(medication);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Cập nhật thuốc thành công!",
                "medicationId", medication.getId(),
                "medicineName", medication.getMedicineName(),
                "dosage", medication.getDosage(),
                "scheduledTime", medication.getScheduledTime()
        ));
    }

    // ===================== 3. Xóa thuốc (soft delete) =====================
    @PostMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteMedication(@PathVariable("id") Integer id) {

        Optional<PatientMedication> medOpt = medicationRepository.findById(id);
        if (medOpt.isPresent() == false) {
            throw new IllegalStateException("Không tìm thấy thuốc với ID: " + id);
        }
        PatientMedication medication = medOpt.get();

        medication.setIsActive(false);
        medication.setUpdatedAt(LocalDateTime.now());
        medicationRepository.save(medication);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã xóa thuốc: " + medication.getMedicineName()
        ));
    }

    // ===================== 4. Toggle uống thuốc hôm nay =====================
    @PostMapping("/toggle-take")
    public ResponseEntity<Map<String, Object>> toggleTakeMedication(
            @RequestParam("medicationId") Integer medicationId,
            @RequestParam("status") boolean status) {

        // Kiểm tra thuốc có tồn tại không
        Optional<PatientMedication> medOpt = medicationRepository.findById(medicationId);
        if (medOpt.isPresent() == false) {
            throw new IllegalStateException("Không tìm thấy thuốc với ID: " + medicationId);
        }
        PatientMedication medication = medOpt.get();

        LocalDate today = LocalDate.now();

        // Tìm hoặc tạo MedicationLog cho ngày hôm nay
        Optional<MedicationLog> logOpt = medicationLogRepository
                .findByPatientMedicationIdAndLogDate(medicationId, today);
        MedicationLog log;
        if (logOpt.isPresent()) {
            log = logOpt.get();
        } else {
            MedicationLog newLog = MedicationLog.builder()
                    .patientMedication(medication)
                    .logDate(today)
                    .isTaken(false)
                    .build();
            log = medicationLogRepository.save(newLog);
        }

        // Cập nhật trạng thái
        log.setIsTaken(status);
        log.setTakenAt(status ? LocalDateTime.now() : null);
        medicationLogRepository.save(log);

        // Nếu bệnh nhân đã uống thuốc (status = true), tự động xóa thông báo nhắc quá giờ (nếu có)
        if (status) {
            try {
                String overdueTypeKey = "OVERDUE_MED_REMINDER_" + medicationId;
                Patient patient = medication.getPatient();
                if (patient != null) {
                    List<Notification> allNotifications = 
                        notificationRepository.findByPatientIdOrderByCreatedAtDesc(patient.getId());
                    for (int i = 0; i < allNotifications.size(); i++) {
                        Notification n = allNotifications.get(i);
                        if (overdueTypeKey.equals(n.getNotificationType()) && n.getCreatedAt().toLocalDate().isEqual(today)) {
                            notificationRepository.delete(n);
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", status ? "Đã đánh dấu uống thuốc: " + medication.getMedicineName()
                                  : "Đã hủy đánh dấu: " + medication.getMedicineName(),
                "medicationId", medicationId,
                "status", status,
                "medicineName", medication.getMedicineName()
        ));
    }
}
