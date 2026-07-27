package com.rpm.remotepatientmonitoring.patient;

import com.rpm.remotepatientmonitoring.model.Doctor;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.repository.AppointmentRepository;
import com.rpm.remotepatientmonitoring.repository.ChangeRequestRepository;
import com.rpm.remotepatientmonitoring.repository.DoctorRepository;
import com.rpm.remotepatientmonitoring.repository.NotificationRepository;
import com.rpm.remotepatientmonitoring.repository.PatientRepository;
import com.rpm.remotepatientmonitoring.service.patient.PatientInteractionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientInteractionServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private ChangeRequestRepository changeRequestRepository;

    @InjectMocks
    private PatientInteractionService patientInteractionService;

    private Patient patient;
    private Doctor doctor;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setId(1);
        patient.setFullName("Test Patient");

        doctor = new Doctor();
        doctor.setId(10);
        doctor.setFullName("Test Doctor");
        doctor.setCurrentPatientCount(5);
        doctor.setCapacityLimit(10);
    }

    @Test
    void bookAppointment_UT01() {
        Integer doctorId = 10;
        String appointmentType = "ROUTINE";
        String reason = "Routine Checkup";
        LocalDateTime appointmentTime = LocalDateTime.now().plusDays(2);

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

        patientInteractionService.bookAppointment(patient, doctorId, appointmentType, reason, appointmentTime);
        verify(appointmentRepository, times(1)).save(any());
    }

    @Test
    void bookAppointment_UT02() {
        Integer doctorId = 10;
        String appointmentType = "EMERGENCY";
        String reason = "Emergency Chest Pain";
        LocalDateTime appointmentTime = LocalDateTime.now().plusDays(1);

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

        patientInteractionService.bookAppointment(patient, doctorId, appointmentType, reason, appointmentTime);
        verify(appointmentRepository, times(1)).save(any());
    }

    @Test
    void bookAppointment_UT03() {
        Integer doctorId = 999;
        String appointmentType = "ROUTINE";
        String reason = "Invalid Doctor ID";
        LocalDateTime appointmentTime = LocalDateTime.now().plusDays(2);

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());

        try {
            patientInteractionService.bookAppointment(patient, doctorId, appointmentType, reason, appointmentTime);
            fail("Nên quăng IllegalArgumentException khi doctorId không tồn tại");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    void bookAppointment_UT04() {
        Integer doctorId = 10;
        String appointmentType = "ROUTINE";
        String reason = "Reason exceeds limit";
        LocalDateTime appointmentTime = LocalDateTime.now().plusDays(2);

        doctor.setCurrentPatientCount(10);
        doctor.setCapacityLimit(10);
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

        try {
            patientInteractionService.bookAppointment(patient, doctorId, appointmentType, reason, appointmentTime);
            fail("Nên quăng IllegalStateException khi bác sĩ đạt giới hạn bệnh nhân");
        } catch (IllegalStateException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    void bookAppointment_UT05() {
        Integer doctorId = 10;
        String appointmentType = "ROUTINE";

        StringBuilder longReason = new StringBuilder();
        for (int i = 0; i < 501; i++) {
            longReason.append("a");
        }
        String reason = longReason.toString();
        LocalDateTime appointmentTime = LocalDateTime.now().plusDays(2);

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

        try {
            patientInteractionService.bookAppointment(patient, doctorId, appointmentType, reason, appointmentTime);
            fail("Nên quăng IllegalArgumentException khi lý do dài quá 500 ký tự");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    void bookAppointment_UT06() {
        Integer doctorId = 10;
        String appointmentType = "ROUTINE";
        String reason = "Normal Reason";
        LocalDateTime appointmentTime = LocalDateTime.now().plusDays(2);

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

        patientInteractionService.bookAppointment(patient, doctorId, appointmentType, reason, appointmentTime);
        verify(appointmentRepository, times(1)).save(any());
    }
}
