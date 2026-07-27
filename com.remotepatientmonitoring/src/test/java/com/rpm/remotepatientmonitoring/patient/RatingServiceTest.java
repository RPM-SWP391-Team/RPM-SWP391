package com.rpm.remotepatientmonitoring.patient;

import com.rpm.remotepatientmonitoring.model.Account;
import com.rpm.remotepatientmonitoring.model.Appointment;
import com.rpm.remotepatientmonitoring.model.DoctorRating;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.repository.AppointmentRepository;
import com.rpm.remotepatientmonitoring.repository.DoctorRatingRepository;
import com.rpm.remotepatientmonitoring.service.RatingService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RatingServiceTest {

    @Mock
    private DoctorRatingRepository doctorRatingRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private RatingService ratingService;

    private Account account;
    private Patient patient;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        ratingService.init();

        account = new Account();
        account.setId(100);

        patient = new Patient();
        patient.setId(1);
        patient.setAccount(account);

        appointment = new Appointment();
        appointment.setId(50);
        appointment.setStatus("COMPLETED");
        appointment.setPatient(patient);
    }

    @Test
    void getEvaluatedAppointmentIdsByPatientId_UT01() {
        Integer patientId = 1;
        DoctorRating rating = new DoctorRating();
        rating.setAppointment(appointment);

        List<DoctorRating> ratingList = new ArrayList<>();
        ratingList.add(rating);

        when(doctorRatingRepository.findByPatientId(patientId)).thenReturn(ratingList);

        Set<Integer> actual = ratingService.getEvaluatedAppointmentIdsByPatientId(patientId);
        assertEquals(1, actual.size());
        assertTrue(actual.contains(50));
    }

    @Test
    void submitDoctorRating_UT02() {
        Integer appointmentId = 50;
        Integer ratingValue = 5;
        String comment = "Bác sĩ rất nhiệt tình";

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(doctorRatingRepository.existsByAppointmentId(appointmentId)).thenReturn(false);

        DoctorRating saved = new DoctorRating();
        saved.setRatingValue(5);
        when(doctorRatingRepository.save(any(DoctorRating.class))).thenReturn(saved);

        DoctorRating actual = ratingService.submitDoctorRating(appointmentId, ratingValue, comment, account);
        assertNotNull(actual);
        assertEquals(5, actual.getRatingValue());
    }

    @Test
    void submitDoctorRating_UT03() {
        Integer appointmentId = 50;
        Integer ratingValue = 6;
        String comment = "Good";

        try {
            ratingService.submitDoctorRating(appointmentId, ratingValue, comment, account);
            fail("Nên quăng IllegalArgumentException khi điểm đánh giá > 5");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    void submitDoctorRating_UT04() {
        Integer appointmentId = 50;
        Integer ratingValue = 5;
        String comment = "Good";

        appointment.setStatus("PENDING");
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        try {
            ratingService.submitDoctorRating(appointmentId, ratingValue, comment, account);
            fail("Nên quăng IllegalStateException khi lịch khám chưa hoàn thành");
        } catch (IllegalStateException e) {
            assertNotNull(e.getMessage());
        }
    }
}
