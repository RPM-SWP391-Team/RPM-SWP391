package com.rpm.remotepatientmonitoring.controller.patient;

import com.rpm.remotepatientmonitoring.model.MedicationLog;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.model.PatientMedication;
import com.rpm.remotepatientmonitoring.repository.MedicationLogRepository;
import com.rpm.remotepatientmonitoring.repository.PatientMedicationRepository;
import com.rpm.remotepatientmonitoring.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/patient/api/medications")
public class PatientMedicationController {

    @Autowired
    private PatientMedicationRepository medicationRepository;

    @Autowired
    private MedicationLogRepository medicationLogRepository;

    @Autowired
    private PatientRepository patientRepository;

    // ===================== Lấy Patient mock đầu tiên =====================
    private Patient getCurrentPatient() {
        return patientRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy bệnh nhân trong hệ thống."));
    }

    // ===================== 1. Thêm mới thuốc =====================
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addMedication(
            @RequestParam("medicineName") String medicineName,
            @RequestParam("dosage") String dosage,
            @RequestParam("scheduledTime") String scheduledTime) {

        Patient patient = getCurrentPatient();

        PatientMedication medication = PatientMedication.builder()
                .patient(patient)
                .medicineName(medicineName)
                .dosage(dosage)
                .scheduledTime(scheduledTime)
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

        PatientMedication medication = medicationRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy thuốc với ID: " + id));

        medication.setMedicineName(medicineName);
        medication.setDosage(dosage);
        medication.setScheduledTime(scheduledTime);
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

        PatientMedication medication = medicationRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy thuốc với ID: " + id));

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
        PatientMedication medication = medicationRepository.findById(medicationId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy thuốc với ID: " + medicationId));

        LocalDate today = LocalDate.now();

        // Tìm hoặc tạo MedicationLog cho ngày hôm nay
        MedicationLog log = medicationLogRepository
                .findByPatientMedicationIdAndLogDate(medicationId, today)
                .orElseGet(() -> {
                    MedicationLog newLog = MedicationLog.builder()
                            .patientMedication(medication)
                            .logDate(today)
                            .isTaken(false)
                            .build();
                    return medicationLogRepository.save(newLog);
                });

        // Cập nhật trạng thái
        log.setIsTaken(status);
        log.setTakenAt(status ? LocalDateTime.now() : null);
        medicationLogRepository.save(log);

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
