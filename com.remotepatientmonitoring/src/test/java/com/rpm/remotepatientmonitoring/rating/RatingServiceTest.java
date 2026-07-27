package com.rpm.remotepatientmonitoring.rating;

import com.rpm.remotepatientmonitoring.model.*;
import com.rpm.remotepatientmonitoring.repository.*;
import com.rpm.remotepatientmonitoring.service.RatingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock
    private DoctorRatingRepository doctorRatingRepository;

    @Mock
    private AppRatingRepository appRatingRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private RatingService ratingService;

    private Account samplePatientAccount;
    private Account sampleAdminAccount;
    private Patient samplePatient;
    private Doctor sampleDoctor;
    private Appointment sampleAppointment;

    @BeforeEach
    void setUp() {
        ratingService.init();

        samplePatientAccount = Account.builder()
                .id(1)
                .email("patient@example.com")
                .role("PATIENT")
                .isEmailVerified(true)
                .build();

        sampleAdminAccount = Account.builder()
                .id(2)
                .email("admin@example.com")
                .role("HOSPITAL_ADMIN")
                .isEmailVerified(true)
                .build();

        samplePatient = Patient.builder()
                .id(10)
                .account(samplePatientAccount)
                .fullName("Nguyen Van A")
                .build();

        sampleDoctor = Doctor.builder()
                .id(20)
                .fullName("Dr. Tran Van B")
                .build();

        sampleAppointment = Appointment.builder()
                .id(100)
                .patient(samplePatient)
                .doctor(sampleDoctor)
                .status("COMPLETED")
                .build();
    }

    @Nested
    @DisplayName("Submit Doctor Rating")
    class SubmitDoctorRating {

        @Test
        @DisplayName("submitDoctorRating successfully saves rating for completed appointment")
        void submitDoctorRating_success() {
            when(appointmentRepository.findById(100)).thenReturn(Optional.of(sampleAppointment));
            when(doctorRatingRepository.existsByAppointmentId(100)).thenReturn(false);
            when(doctorRatingRepository.save(any(DoctorRating.class))).thenAnswer(i -> i.getArgument(0));

            DoctorRating rating = ratingService.submitDoctorRating(100, 5, "Dịch vụ rất tốt!", samplePatientAccount);

            assertNotNull(rating);
            assertEquals(5, rating.getRatingValue());
            assertEquals("Dịch vụ rất tốt!", rating.getComment());
            assertEquals(samplePatient, rating.getPatient());
            assertEquals(sampleDoctor, rating.getDoctor());
        }

        @Test
        @DisplayName("submitDoctorRating throws exception if comment contains banned words")
        void submitDoctorRating_bannedWords_throwsException() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> ratingService.submitDoctorRating(100, 5, "Bác sĩ vcl ngu", samplePatientAccount));

            assertTrue(ex.getMessage().contains("từ ngữ không phù hợp"));
        }

        @Test
        @DisplayName("submitDoctorRating throws exception if rating is out of range 1-5")
        void submitDoctorRating_invalidRatingValue_throwsException() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> ratingService.submitDoctorRating(100, 6, "Tốt", samplePatientAccount));

            assertTrue(ex.getMessage().contains("từ 1 đến 5 sao"));
        }

        @Test
        @DisplayName("submitDoctorRating throws exception if appointment is not COMPLETED")
        void submitDoctorRating_uncompletedAppointment_throwsException() {
            sampleAppointment.setStatus("PENDING");
            when(appointmentRepository.findById(100)).thenReturn(Optional.of(sampleAppointment));

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> ratingService.submitDoctorRating(100, 5, "Tốt", samplePatientAccount));

            assertTrue(ex.getMessage().contains("Chỉ có thể đánh giá lịch khám đã hoàn thành"));
        }

        @Test
        @DisplayName("submitDoctorRating throws exception if account does not own the appointment")
        void submitDoctorRating_unauthorizedUser_throwsException() {
            Account otherAccount = Account.builder().id(99).build();
            when(appointmentRepository.findById(100)).thenReturn(Optional.of(sampleAppointment));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> ratingService.submitDoctorRating(100, 5, "Tốt", otherAccount));

            assertTrue(ex.getMessage().contains("không có quyền đánh giá"));
        }

        @Test
        @DisplayName("submitDoctorRating throws exception on duplicate rating for same appointment")
        void submitDoctorRating_duplicateRating_throwsException() {
            when(appointmentRepository.findById(100)).thenReturn(Optional.of(sampleAppointment));
            when(doctorRatingRepository.existsByAppointmentId(100)).thenReturn(true);

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> ratingService.submitDoctorRating(100, 5, "Tốt", samplePatientAccount));

            assertTrue(ex.getMessage().contains("đã được đánh giá trước đó"));
        }
    }

    @Nested
    @DisplayName("Submit App Rating")
    class SubmitAppRating {

        @Test
        @DisplayName("submitAppRating saves app rating for valid patient account")
        void submitAppRating_success() {
            when(appRatingRepository.countByAccountIdAndCreatedAtAfter(eq(1), any())).thenReturn(0L);
            when(appRatingRepository.save(any(AppRating.class))).thenAnswer(i -> i.getArgument(0));

            AppRating rating = ratingService.submitAppRating(5, "Ứng dụng rất tiện lợi!", samplePatientAccount);

            assertNotNull(rating);
            assertEquals(5, rating.getRatingValue());
            assertEquals("Ứng dụng rất tiện lợi!", rating.getComment());
            assertEquals(samplePatientAccount, rating.getAccount());
        }

        @Test
        @DisplayName("submitAppRating with empty or null comment saves successfully without comment requirement")
        void submitAppRating_emptyOrNullComment_success() {
            when(appRatingRepository.countByAccountIdAndCreatedAtAfter(eq(1), any())).thenReturn(0L);
            when(appRatingRepository.save(any(AppRating.class))).thenAnswer(i -> i.getArgument(0));

            AppRating ratingNull = ratingService.submitAppRating(4, null, samplePatientAccount);
            assertNotNull(ratingNull);
            assertNull(ratingNull.getComment());
            assertEquals(4, ratingNull.getRatingValue());

            AppRating ratingEmpty = ratingService.submitAppRating(5, "   ", samplePatientAccount);
            assertNotNull(ratingEmpty);
            assertEquals("   ", ratingEmpty.getComment());
            assertEquals(5, ratingEmpty.getRatingValue());
        }

        @Test
        @DisplayName("submitAppRating blocks HOSPITAL_ADMIN role from rating app")
        void submitAppRating_adminAccount_throwsException() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> ratingService.submitAppRating(5, "Tốt", sampleAdminAccount));

            assertTrue(ex.getMessage().contains("không áp dụng cho tài khoản quản trị viên"));
        }

        @Test
        @DisplayName("submitAppRating enforces 1 rating per day limit")
        void submitAppRating_dailyLimitExceeded_throwsException() {
            when(appRatingRepository.countByAccountIdAndCreatedAtAfter(eq(1), any())).thenReturn(1L);

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> ratingService.submitAppRating(5, "Tốt", samplePatientAccount));

            assertTrue(ex.getMessage().contains("tối đa 1 lần mỗi ngày"));
        }
    }

    @Nested
    @DisplayName("Admin Rating Toggle Hide & Queries")
    class AdminRatingManagement {

        @Test
        @DisplayName("toggleHideDoctorRating toggles isHidden flag")
        void toggleHideDoctorRating_togglesFlag() {
            DoctorRating doctorRating = DoctorRating.builder().id(1).isHidden(false).build();
            when(doctorRatingRepository.findById(1)).thenReturn(Optional.of(doctorRating));

            ratingService.toggleHideDoctorRating(1);

            assertTrue(doctorRating.getIsHidden());
            verify(doctorRatingRepository).save(doctorRating);
        }

        @Test
        @DisplayName("toggleHideAppRating toggles isHidden flag")
        void toggleHideAppRating_togglesFlag() {
            AppRating appRating = AppRating.builder().id(2).isHidden(false).build();
            when(appRatingRepository.findById(2)).thenReturn(Optional.of(appRating));

            ratingService.toggleHideAppRating(2);

            assertTrue(appRating.getIsHidden());
            verify(appRatingRepository).save(appRating);
        }

        @Test
        @DisplayName("populateDoctorRatings updates doctor rating count and average rating")
        void populateDoctorRatings_updatesStats() {
            when(doctorRatingRepository.countByDoctorId(20)).thenReturn(10L);
            when(doctorRatingRepository.getAverageRatingByDoctorId(20)).thenReturn(4.8);

            ratingService.populateDoctorRatings(sampleDoctor);

            assertEquals(10L, sampleDoctor.getRatingCount());
            assertEquals(4.8, sampleDoctor.getAverageRating());
        }
    }
}
