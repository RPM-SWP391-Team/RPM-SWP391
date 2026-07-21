package com.rpm.remotepatientmonitoring.service.patient;

import com.rpm.remotepatientmonitoring.model.MedicationLog;
import com.rpm.remotepatientmonitoring.model.Notification;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.model.PatientMedication;
import com.rpm.remotepatientmonitoring.repository.MedicationLogRepository;
import com.rpm.remotepatientmonitoring.repository.NotificationRepository;
import com.rpm.remotepatientmonitoring.repository.PatientMedicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PatientMedicationService {

    @Autowired
    private PatientMedicationRepository medicationRepository;

    @Autowired
    private MedicationLogRepository medicationLogRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Transactional
    public PatientMedication addMedication(Patient patient, String medicineName, String dosage, String scheduledTime) {
        PatientMedication medication = PatientMedication.builder()
                .patient(patient)
                .medicineName(medicineName.trim())
                .dosage(dosage.trim())
                .scheduledTime(scheduledTime.trim())
                .build();
        return medicationRepository.save(medication);
    }

    @Transactional
    public PatientMedication updateMedication(Integer id, String medicineName, String dosage, String scheduledTime) {
        Optional<PatientMedication> medOpt = medicationRepository.findById(id);
        if (medOpt.isEmpty()) {
            throw new IllegalStateException("Không tìm thấy thuốc với ID: " + id);
        }
        PatientMedication medication = medOpt.get();
        medication.setMedicineName(medicineName.trim());
        medication.setDosage(dosage.trim());
        medication.setScheduledTime(scheduledTime.trim());
        medication.setUpdatedAt(LocalDateTime.now());
        return medicationRepository.save(medication);
    }

    @Transactional
    public PatientMedication deleteMedication(Integer id) {
        Optional<PatientMedication> medOpt = medicationRepository.findById(id);
        if (medOpt.isEmpty()) {
            throw new IllegalStateException("Không tìm thấy thuốc với ID: " + id);
        }
        PatientMedication medication = medOpt.get();
        medication.setIsActive(false);
        medication.setUpdatedAt(LocalDateTime.now());
        return medicationRepository.save(medication);
    }

    @Transactional
    public void toggleTakeMedication(Integer medicationId, boolean status) {
        Optional<PatientMedication> medOpt = medicationRepository.findById(medicationId);
        if (medOpt.isEmpty()) {
            throw new IllegalStateException("Không tìm thấy thuốc với ID: " + medicationId);
        }
        PatientMedication medication = medOpt.get();
        LocalDate today = LocalDate.now();

        Optional<MedicationLog> logOpt = medicationLogRepository
                .findByPatientMedicationIdAndLogDate(medicationId, today);
        MedicationLog logVal;
        if (logOpt.isPresent()) {
            logVal = logOpt.get();
        } else {
            MedicationLog newLog = MedicationLog.builder()
                    .patientMedication(medication)
                    .logDate(today)
                    .isTaken(false)
                    .build();
            logVal = medicationLogRepository.save(newLog);
        }

        logVal.setIsTaken(status);
        logVal.setTakenAt(status ? LocalDateTime.now() : null);
        medicationLogRepository.save(logVal);

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
    }

    @Transactional
    public MedicationLog addOrUpdateMedicationLog(Integer medicationId, LocalDate date, boolean isTaken, LocalDateTime takenAt) {
        Optional<PatientMedication> medOpt = medicationRepository.findById(medicationId);
        if (medOpt.isEmpty()) {
            throw new IllegalStateException("Không tìm thấy thuốc với ID: " + medicationId);
        }
        PatientMedication medication = medOpt.get();
        Optional<MedicationLog> logOpt = medicationLogRepository.findByPatientMedicationIdAndLogDate(medicationId, date);
        MedicationLog logVal;
        if (logOpt.isPresent()) {
            logVal = logOpt.get();
            logVal.setIsTaken(isTaken);
            logVal.setTakenAt(isTaken ? takenAt : null);
        } else {
            logVal = MedicationLog.builder()
                    .patientMedication(medication)
                    .logDate(date)
                    .isTaken(isTaken)
                    .takenAt(isTaken ? takenAt : null)
                    .build();
        }
        return medicationLogRepository.save(logVal);
    }

    @Transactional
    public void deleteMedicationLog(Integer id) {
        Optional<MedicationLog> logOpt = medicationLogRepository.findById(id);
        if (logOpt.isPresent()) {
            medicationLogRepository.delete(logOpt.get());
        }
    }
}
