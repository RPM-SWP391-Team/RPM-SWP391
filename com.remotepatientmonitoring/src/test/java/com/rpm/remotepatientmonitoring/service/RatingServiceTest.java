package com.rpm.remotepatientmonitoring.service;

import com.rpm.remotepatientmonitoring.model.*;
import com.rpm.remotepatientmonitoring.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RatingServiceTest {

    @InjectMocks
    private RatingService ratingService;

    @Mock
    private DoctorRatingRepository doctorRatingRepository;

    @Mock
    private AppRatingRepository appRatingRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    private Account currentAccount;
    private Patient patient;
    private Doctor doctor;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        ratingService.init();

        currentAccount = new Account();
        currentAccount.setId(10);
        currentAccount.setEmail("patient@rpm.vn");

        patient = new Patient();
        patient.setId(1);
        patient.setAccount(currentAccount);
        patient.setFullName("Nguyen Van Patient");

        doctor = new Doctor();
        doctor.setId(2);
        doctor.setFullName("BS Nguyen Van Doctor");

        appointment = new Appointment();
        appointment.setId(100);
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setStatus("COMPLETED");
    }

    @Test
    void testDoctorRating_DuplicateAppointmentId_ThrowsException() {
        // Arrange
        Integer appointmentId = appointment.getId();
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        // Mock that a rating already exists for this appointment
        when(doctorRatingRepository.existsByAppointmentId(appointmentId)).thenReturn(true);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            ratingService.submitDoctorRating(appointmentId, 5, "Good service", currentAccount);
        });

        assertEquals("Lịch khám này đã được đánh giá trước đó.", exception.getMessage());
        verify(doctorRatingRepository, never()).save(any(DoctorRating.class));
    }

    @Test
    void testAppRating_LimitOnePerDay_ThrowsException() {
        // Arrange
        // Mock that user already rated today
        when(appRatingRepository.countByAccountIdAndCreatedAtAfter(eq(currentAccount.getId()), any(LocalDateTime.class)))
                .thenReturn(1L);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            ratingService.submitAppRating(4, "Nice app!", currentAccount);
        });

        assertEquals("Mỗi tài khoản chỉ được đánh giá ứng dụng tối đa 1 lần mỗi ngày.", exception.getMessage());
        verify(appRatingRepository, never()).save(any(AppRating.class));
    }

    @Test
    void testAppRating_HospitalAdminRole_ThrowsException() {
        // Arrange
        currentAccount.setRole("HOSPITAL_ADMIN");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ratingService.submitAppRating(4, "Nice app!", currentAccount);
        });

        assertEquals("Chức năng này không áp dụng cho tài khoản quản trị viên.", exception.getMessage());
        verify(appRatingRepository, never()).save(any(AppRating.class));
    }

    @Test
    void testDoctorRating_CalculateAverageCorrectly() {
        // Arrange
        // Setup doctor
        doctor.setAverageRating(null);
        doctor.setRatingCount(null);

        when(doctorRatingRepository.countByDoctorId(doctor.getId())).thenReturn(3L);
        when(doctorRatingRepository.getAverageRatingByDoctorId(doctor.getId())).thenReturn(4.5);

        // Act
        ratingService.populateDoctorRatings(doctor);

        // Assert
        assertEquals(3L, doctor.getRatingCount());
        assertEquals(4.5, doctor.getAverageRating());
        assertEquals("⭐⭐⭐⭐⭐ 4.5/5 (3 đánh giá)", doctor.getRatingDisplay());
    }

    @Test
    void testDoctorRating_OwnershipCheck_ThrowsException() {
        // Arrange
        // Set appointment patient to someone else
        Account otherAccount = new Account();
        otherAccount.setId(99);
        Patient otherPatient = new Patient();
        otherPatient.setId(22);
        otherPatient.setAccount(otherAccount);
        appointment.setPatient(otherPatient);

        when(appointmentRepository.findById(appointment.getId())).thenReturn(Optional.of(appointment));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ratingService.submitDoctorRating(appointment.getId(), 5, "Good service", currentAccount);
        });

        assertEquals("Bạn không có quyền đánh giá lịch khám của người khác.", exception.getMessage());
        verify(doctorRatingRepository, never()).save(any(DoctorRating.class));
    }

    @Test
    void testBannedWordsFilter_ThrowsException() {
        IllegalArgumentException exc1 = assertThrows(IllegalArgumentException.class, () -> {
            ratingService.submitDoctorRating(appointment.getId(), 5, "Mày ngu lắm", currentAccount);
        });
        assertEquals("Nội dung nhận xét chứa từ ngữ không phù hợp. Vui lòng chỉnh sửa lại.", exc1.getMessage());

        IllegalArgumentException exc2 = assertThrows(IllegalArgumentException.class, () -> {
            ratingService.submitAppRating(4, "Đm ứng dụng gì thế này", currentAccount);
        });
        assertEquals("Nội dung nhận xét chứa từ ngữ không phù hợp. Vui lòng chỉnh sửa lại.", exc2.getMessage());
    }

    @Test
    void testToggleHideDoctorRating() {
        DoctorRating docRating = new DoctorRating();
        docRating.setId(500);
        docRating.setIsHidden(false);
        when(doctorRatingRepository.findById(500)).thenReturn(Optional.of(docRating));

        ratingService.toggleHideDoctorRating(500);

        assertTrue(docRating.getIsHidden());
        verify(doctorRatingRepository).save(docRating);
    }
}
