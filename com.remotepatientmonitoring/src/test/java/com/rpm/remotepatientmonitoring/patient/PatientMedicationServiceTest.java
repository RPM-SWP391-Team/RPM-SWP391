package com.rpm.remotepatientmonitoring.patient;

import com.rpm.remotepatientmonitoring.model.MedicationLog;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.model.PatientMedication;
import com.rpm.remotepatientmonitoring.repository.MedicationLogRepository;
import com.rpm.remotepatientmonitoring.repository.NotificationRepository;
import com.rpm.remotepatientmonitoring.repository.PatientMedicationRepository;
import com.rpm.remotepatientmonitoring.service.patient.PatientMedicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientMedicationServiceTest {

    @Mock
    private PatientMedicationRepository medicationRepository;

    @Mock
    private MedicationLogRepository medicationLogRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private PatientMedicationService patientMedicationService;

    private Patient patient;
    private PatientMedication medication;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setId(1);
        patient.setFullName("Test Patient");

        medication = new PatientMedication();
        medication.setId(10);
        medication.setPatient(patient);
        medication.setMedicineName("Aspirin");
        medication.setDosage("100mg");
        medication.setScheduledTime("08:00");
        medication.setIsActive(true);
    }

    @Test
    void addMedication_UT01() {
        String medicineName = "Aspirin";
        String dosage = "100mg";
        String scheduledTime = "08:00";

        when(medicationRepository.save(any(PatientMedication.class))).thenReturn(medication);

        PatientMedication actual = patientMedicationService.addMedication(patient, medicineName, dosage, scheduledTime);
        assertNotNull(actual);
        assertEquals("Aspirin", actual.getMedicineName());
    }

    @Test
    void addMedication_UT02() {
        String medicineName = null;
        String dosage = "100mg";
        String scheduledTime = "08:00";

        try {
            patientMedicationService.addMedication(patient, medicineName, dosage, scheduledTime);
            fail("Nên quăng lỗi NullPointerException khi medicineName bị null");
        } catch (NullPointerException e) {
            assertNotNull(e);
        }
    }

    @Test
    void addMedication_UT03() {
        String medicineName = "Aspirin";
        String dosage = null;
        String scheduledTime = "08:00";

        try {
            patientMedicationService.addMedication(patient, medicineName, dosage, scheduledTime);
            fail("Nên quăng lỗi NullPointerException khi dosage bị null");
        } catch (NullPointerException e) {
            assertNotNull(e);
        }
    }

    @Test
    void toggleTakeMedication_UT04() {
        Integer medicationId = 10;
        boolean status = true;

        when(medicationRepository.findById(medicationId)).thenReturn(Optional.of(medication));
        when(medicationLogRepository.findByPatientMedicationIdAndLogDate(eq(medicationId), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        MedicationLog newLog = new MedicationLog();
        newLog.setIsTaken(true);
        when(medicationLogRepository.save(any(MedicationLog.class))).thenReturn(newLog);

        patientMedicationService.toggleTakeMedication(medicationId, status);
        verify(medicationLogRepository, times(2)).save(any(MedicationLog.class));
    }
}
