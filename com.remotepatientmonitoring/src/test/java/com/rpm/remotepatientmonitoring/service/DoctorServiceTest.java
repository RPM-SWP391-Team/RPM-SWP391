package com.rpm.remotepatientmonitoring.service;

import com.rpm.remotepatientmonitoring.service.hospital.DoctorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rpm.remotepatientmonitoring.model.*;
import com.rpm.remotepatientmonitoring.repository.*;
import com.rpm.remotepatientmonitoring.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private HospitalRepository hospitalRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private AuditTrailRepository auditTrailRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private DoctorService doctorService;

    private Hospital hospital;
    private Doctor oldDoctor;
    private Account oldDoctorAccount;

    @BeforeEach
    public void setUp() {
        hospital = new Hospital();
        hospital.setId(1);
        hospital.setFullName("Bệnh viện Đa khoa Mẫu");

        oldDoctorAccount = new Account();
        oldDoctorAccount.setId(101);
        oldDoctorAccount.setEmail("doctor_old@hospital.com");
        oldDoctorAccount.setRole("DOCTOR");
        oldDoctorAccount.setIsActive(true);

        oldDoctor = new Doctor();
        oldDoctor.setId(1);
        oldDoctor.setDoctorCode("BS001");
        oldDoctor.setFullName("Bác sĩ A");
        oldDoctor.setSpecialty("Tiểu đường");
        oldDoctor.setCapacityLimit(50);
        oldDoctor.setCurrentPatientCount(5);
        oldDoctor.setIsActive(true);
        oldDoctor.setHospital(hospital);
        oldDoctor.setAccount(oldDoctorAccount);
    }

    // =========================================================================
    // TESTS FOR deactivateDoctor
    // =========================================================================

    @Test
    public void testDeactivateDoctor_DoctorNotFound_ThrowsException() {
        // Arrange
        Integer targetDoctorId = 999;
        when(doctorRepository.findById(targetDoctorId)).thenReturn(Optional.empty());

        // Act & Assert
        try {
            doctorService.deactivateDoctor(targetDoctorId);
            fail("Nên ném ngoại lệ IllegalArgumentException khi không tìm thấy bác sĩ");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Không tìm thấy bác sĩ với ID: " + targetDoctorId));
        }

        verify(doctorRepository, never()).save(any(Doctor.class));
    }

    @Test
    public void testDeactivateDoctor_NoActivePatients_DeactivatesSuccessfully() {
        // Arrange
        when(doctorRepository.findById(1)).thenReturn(Optional.of(oldDoctor));
        when(patientRepository.findByDoctorIdAndIsActiveTrue(1)).thenReturn(new ArrayList<Patient>());

        // Act
        doctorService.deactivateDoctor(1);

        // Assert
        assertFalse(oldDoctor.getIsActive());
        assertEquals(0, oldDoctor.getCurrentPatientCount());
        assertFalse(oldDoctorAccount.getIsActive());
        verify(doctorRepository).save(oldDoctor);
        verify(accountRepository).save(oldDoctorAccount);
    }

    @Test
    public void testDeactivateDoctor_WithPatients_NoReplacementsAvailable_ThrowsException() {
        // Arrange
        Patient patient = new Patient();
        patient.setId(10);
        patient.setFullName("Bệnh nhân X");

        when(doctorRepository.findById(1)).thenReturn(Optional.of(oldDoctor));
        when(patientRepository.findByDoctorIdAndIsActiveTrue(1)).thenReturn(Arrays.asList(patient));
        when(doctorRepository.findBestReplacementDoctors(1, 1)).thenReturn(new ArrayList<Doctor>());

        // Act & Assert
        try {
            doctorService.deactivateDoctor(1);
            fail("Nên ném ngoại lệ IllegalStateException khi không tìm thấy bác sĩ thay thế");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Không thể vô hiệu hóa! Toàn bộ bác sĩ khác trong viện đều đã QUÁ TẢI."));
        }

        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    public void testDeactivateDoctor_WithPatients_NoCompatibleReplacement_ThrowsException() {
        // Arrange
        DiseaseProfile diabetesProfile = new DiseaseProfile();
        diabetesProfile.setId(1);
        diabetesProfile.setProfileCode("DIABETES");
        diabetesProfile.setProfileName("Tiểu đường");

        Patient patient = new Patient();
        patient.setId(10);
        patient.setFullName("Bệnh nhân X");
        patient.setDiseaseProfile(diabetesProfile);

        Doctor replacement = new Doctor();
        replacement.setId(2);
        replacement.setFullName("Bác sĩ B");
        replacement.setSpecialty("Huyết áp"); // Không khớp với Tiểu đường
        replacement.setCapacityLimit(10);
        replacement.setCurrentPatientCount(2);
        replacement.setHospital(hospital);

        when(doctorRepository.findById(1)).thenReturn(Optional.of(oldDoctor));
        when(patientRepository.findByDoctorIdAndIsActiveTrue(1)).thenReturn(Arrays.asList(patient));
        when(doctorRepository.findBestReplacementDoctors(1, 1)).thenReturn(Arrays.asList(replacement));

        // Act & Assert
        try {
            doctorService.deactivateDoctor(1);
            fail("Nên ném ngoại lệ IllegalStateException khi không có bác sĩ cùng chuyên khoa");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Không thể vô hiệu hóa bác sĩ! Không tìm thấy bác sĩ thay thế phù hợp chuyên khoa"));
        }

        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    public void testDeactivateDoctor_WithPatients_ReplacementCapacityExceeded_ThrowsException() {
        // Arrange
        DiseaseProfile diabetesProfile = new DiseaseProfile();
        diabetesProfile.setId(1);
        diabetesProfile.setProfileCode("DIABETES");
        diabetesProfile.setProfileName("Tiểu đường");

        Patient patient = new Patient();
        patient.setId(10);
        patient.setFullName("Bệnh nhân X");
        patient.setDiseaseProfile(diabetesProfile);

        Doctor replacement = new Doctor();
        replacement.setId(2);
        replacement.setFullName("Bác sĩ B");
        replacement.setSpecialty("Tiểu đường");
        replacement.setCapacityLimit(5);
        replacement.setCurrentPatientCount(5); // Đã đầy tải lượng
        replacement.setHospital(hospital);

        when(doctorRepository.findById(1)).thenReturn(Optional.of(oldDoctor));
        when(patientRepository.findByDoctorIdAndIsActiveTrue(1)).thenReturn(Arrays.asList(patient));
        when(doctorRepository.findBestReplacementDoctors(1, 1)).thenReturn(Arrays.asList(replacement));

        // Act & Assert
        try {
            doctorService.deactivateDoctor(1);
            fail("Nên ném ngoại lệ IllegalStateException khi bác sĩ thay thế đã đầy tải lượng");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Không thể vô hiệu hóa bác sĩ! Không tìm thấy bác sĩ thay thế phù hợp chuyên khoa"));
        }

        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    public void testDeactivateDoctor_WithPatients_SuccessfulTransfer() {
        // Arrange
        DiseaseProfile diabetesProfile = new DiseaseProfile();
        diabetesProfile.setId(1);
        diabetesProfile.setProfileCode("DIABETES");
        diabetesProfile.setProfileName("Tiểu đường");

        Patient patient = new Patient();
        patient.setId(10);
        patient.setFullName("Bệnh nhân X");
        patient.setDiseaseProfile(diabetesProfile);

        Doctor bestReplacement = new Doctor();
        bestReplacement.setId(2);
        bestReplacement.setFullName("Bác sĩ B");
        bestReplacement.setSpecialty("Tiểu đường");
        bestReplacement.setCapacityLimit(10);
        bestReplacement.setCurrentPatientCount(2); // Còn trống 8 chỗ
        bestReplacement.setHospital(hospital);

        Doctor secondaryReplacement = new Doctor();
        secondaryReplacement.setId(3);
        secondaryReplacement.setFullName("Bác sĩ C");
        secondaryReplacement.setSpecialty("Tiểu đường");
        secondaryReplacement.setCapacityLimit(10);
        secondaryReplacement.setCurrentPatientCount(6); // Còn trống 4 chỗ (ít hơn bác sĩ B)
        secondaryReplacement.setHospital(hospital);

        Alert alert = new Alert();
        alert.setId(50);
        alert.setPatient(patient);
        alert.setIsResolved(false);

        Appointment appointment = new Appointment();
        appointment.setId(60);
        appointment.setPatient(patient);
        appointment.setStatus("ACCEPTED");

        when(doctorRepository.findById(1)).thenReturn(Optional.of(oldDoctor));
        when(patientRepository.findByDoctorIdAndIsActiveTrue(1)).thenReturn(Arrays.asList(patient));
        when(doctorRepository.findBestReplacementDoctors(1, 1)).thenReturn(Arrays.asList(bestReplacement, secondaryReplacement));
        when(alertRepository.findByPatientIdAndIsResolvedFalse(10)).thenReturn(Arrays.asList(alert));
        when(appointmentRepository.findByPatientIdAndDoctorIdAndStatusIn(eq(10), eq(1), anyList()))
                .thenReturn(Arrays.asList(appointment));

        // Act
        doctorService.deactivateDoctor(1);

        // Assert
        assertFalse(oldDoctor.getIsActive());
        assertEquals(0, oldDoctor.getCurrentPatientCount());
        assertFalse(oldDoctorAccount.getIsActive());

        // Kiểm tra điều chuyển sang bác sĩ có sức chứa trống lớn nhất (Bác sĩ B)
        assertEquals(bestReplacement, patient.getDoctor());
        assertEquals(3, bestReplacement.getCurrentPatientCount()); // 2 + 1 = 3
        assertEquals(bestReplacement, alert.getDoctor());
        assertEquals("CANCELLED", appointment.getStatus());

        verify(patientRepository).save(patient);
        verify(alertRepository).save(alert);
        verify(appointmentRepository).save(appointment);
        verify(notificationRepository).save(any(Notification.class));
        verify(doctorRepository).save(bestReplacement);
        verify(doctorRepository).save(oldDoctor);
        verify(accountRepository).save(oldDoctorAccount);
    }

    // =========================================================================
    // TESTS FOR createDoctor
    // =========================================================================

    @Test
    public void testCreateDoctor_InvalidFullName_ThrowsException() {
        // Act & Assert
        try {
            doctorService.createDoctor(1, "BS002", "Nguyen Van A 123", "0988776655", "dr.a@hospital.com", "MALE", LocalDate.of(1985, 5, 20), "pass123", "Tiểu đường", 50);
            fail("Nên ném ngoại lệ khi tên chứa chữ số");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Họ và tên bác sĩ không hợp lệ"));
        }
    }

    @Test
    public void testCreateDoctor_DuplicateDoctorCode_ThrowsException() {
        // Arrange
        when(doctorRepository.existsByDoctorCode("BS001")).thenReturn(true);

        // Act & Assert
        try {
            doctorService.createDoctor(1, "BS001", "Nguyen Van A", "0988776655", "dr.a@hospital.com", "MALE", LocalDate.of(1985, 5, 20), "pass123", "Tiểu đường", 50);
            fail("Nên ném ngoại lệ khi mã bác sĩ trùng");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Mã bác sĩ đã tồn tại"));
        }
    }

    @Test
    public void testCreateDoctor_DuplicatePhone_ThrowsException() {
        // Arrange
        when(doctorRepository.existsByPhone("0988776655")).thenReturn(true);

        // Act & Assert
        try {
            doctorService.createDoctor(1, "BS002", "Nguyen Van A", "0988776655", "dr.a@hospital.com", "MALE", LocalDate.of(1985, 5, 20), "pass123", "Tiểu đường", 50);
            fail("Nên ném ngoại lệ khi số điện thoại trùng");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Số điện thoại đã được đăng ký"));
        }
    }

    @Test
    public void testCreateDoctor_DuplicateEmail_ThrowsException() {
        // Arrange
        when(accountRepository.existsByEmail("dr.a@hospital.com")).thenReturn(true);

        // Act & Assert
        try {
            doctorService.createDoctor(1, "BS002", "Nguyen Van A", "0988776655", "dr.a@hospital.com", "MALE", LocalDate.of(1985, 5, 20), "pass123", "Tiểu đường", 50);
            fail("Nên ném ngoại lệ khi email trùng");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Email đã được đăng ký"));
        }
    }

    @Test
    public void testCreateDoctor_HospitalNotFound_ThrowsException() {
        // Arrange
        when(hospitalRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        try {
            doctorService.createDoctor(99, "BS002", "Nguyen Van A", "0988776655", "dr.a@hospital.com", "MALE", LocalDate.of(1985, 5, 20), "pass123", "Tiểu đường", 50);
            fail("Nên ném ngoại lệ khi không tìm thấy bệnh viện");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Không tìm thấy bệnh viện"));
        }
    }

    @Test
    public void testCreateDoctor_SuccessfulCreation() {
        // Arrange
        Account newAccount = new Account();
        newAccount.setId(201);
        newAccount.setEmail("dr.a@hospital.com");

        Doctor newDoctor = new Doctor();
        newDoctor.setId(2);
        newDoctor.setDoctorCode("BS002");
        newDoctor.setFullName("Nguyen Van A");

        when(hospitalRepository.findById(1)).thenReturn(Optional.of(hospital));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_pass");
        when(accountRepository.save(any(Account.class))).thenReturn(newAccount);
        when(doctorRepository.save(any(Doctor.class))).thenReturn(newDoctor);
        when(emailService.sendDoctorPassword(eq("dr.a@hospital.com"), eq("Nguyen Van A"), anyString())).thenReturn(true);

        // Act
        Doctor result = doctorService.createDoctor(1, "BS002", "Nguyen Van A", "0988776655", "dr.a@hospital.com", "MALE", LocalDate.of(1985, 5, 20), "pass123", "Tiểu đường", 50);

        // Assert
        assertNotNull(result);
        assertEquals("BS002", result.getDoctorCode());
        assertEquals("Nguyen Van A", result.getFullName());
        verify(accountRepository).save(any(Account.class));
        verify(doctorRepository).save(any(Doctor.class));
        verify(emailService).sendDoctorPassword(eq("dr.a@hospital.com"), eq("Nguyen Van A"), anyString());
    }

    // =========================================================================
    // TESTS FOR activateDoctor
    // =========================================================================

    @Test
    public void testActivateDoctor_DoctorNotFound_ThrowsException() {
        // Arrange
        when(doctorRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        try {
            doctorService.activateDoctor(99);
            fail("Nên ném ngoại lệ khi không tìm thấy bác sĩ");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Không tìm thấy bác sĩ với ID: 99"));
        }
    }

    @Test
    public void testActivateDoctor_AlreadyActive_ThrowsException() {
        // Arrange
        oldDoctor.setIsActive(true);
        when(doctorRepository.findById(1)).thenReturn(Optional.of(oldDoctor));

        // Act & Assert
        try {
            doctorService.activateDoctor(1);
            fail("Nên ném ngoại lệ khi bác sĩ đã hoạt động");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("hiện đã ở trạng thái hoạt động"));
        }
    }

    @Test
    public void testActivateDoctor_SuccessfulActivation() {
        // Arrange
        oldDoctor.setIsActive(false);
        oldDoctorAccount.setIsActive(false);
        when(doctorRepository.findById(1)).thenReturn(Optional.of(oldDoctor));

        // Act
        doctorService.activateDoctor(1);

        // Assert
        assertTrue(oldDoctor.getIsActive());
        assertTrue(oldDoctorAccount.getIsActive());
        verify(doctorRepository).save(oldDoctor);
        verify(accountRepository).save(oldDoctorAccount);
    }
}
