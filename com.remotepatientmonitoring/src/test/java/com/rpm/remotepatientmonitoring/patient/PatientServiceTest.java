package com.rpm.remotepatientmonitoring.patient;

import com.rpm.remotepatientmonitoring.model.*;
import com.rpm.remotepatientmonitoring.repository.*;
import com.rpm.remotepatientmonitoring.service.patient.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private WaterLogRepository waterLogRepository;

    @Mock
    private PatientMealRepository patientMealRepository;

    @Mock
    private FoodDictionaryRepository foodDictionaryRepository;

    @Mock
    private PatientExerciseRepository patientExerciseRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PatientService patientService;

    private Account sampleAccount;
    private Patient samplePatient;

    @BeforeEach
    void setUp() {
        sampleAccount = Account.builder()
                .id(1)
                .email("patient@example.com")
                .passwordHash("oldHash")
                .role("PATIENT")
                .build();

        samplePatient = Patient.builder()
                .id(10)
                .account(sampleAccount)
                .fullName("Nguyen Van A")
                .phone("0912345678")
                .address("123 Main St")
                .build();
    }

    @Nested
    @DisplayName("Patient Profile & Account Updates")
    class ProfileManagement {

        @Test
        @DisplayName("findByAccountId returns patient when present")
        void findByAccountId_returnsPatient() {
            when(patientRepository.findByAccountId(1)).thenReturn(Optional.of(samplePatient));

            Optional<Patient> result = patientService.findByAccountId(1);

            assertTrue(result.isPresent());
            assertEquals("Nguyen Van A", result.get().getFullName());
        }

        @Test
        @DisplayName("updateProfile updates patient info and encodes password if provided")
        void updateProfile_updatesPatientAndAccount() {
            when(passwordEncoder.encode("newPassword123")).thenReturn("newHash123");

            patientService.updateProfile(
                    samplePatient,
                    "0987654321",
                    "456 New St",
                    "Nguyen Van B",
                    "0911223344",
                    "newPassword123"
            );

            assertEquals("0987654321", samplePatient.getPhone());
            assertEquals("456 New St", samplePatient.getAddress());
            assertEquals("Nguyen Van B", samplePatient.getEmergencyContactName());
            assertEquals("0911223344", samplePatient.getEmergencyContactPhone());

            verify(patientRepository).save(samplePatient);
            verify(passwordEncoder).encode("newPassword123");
            assertEquals("newHash123", sampleAccount.getPasswordHash());
            verify(accountRepository).save(sampleAccount);
        }

        @Test
        @DisplayName("updateProfile does not modify or overwrite account email field")
        void updateProfile_doesNotModifyAccountEmail() {
            String originalEmail = sampleAccount.getEmail();

            patientService.updateProfile(
                    samplePatient,
                    "0987654321",
                    "456 New St",
                    "Nguyen Van B",
                    "0911223344",
                    null
            );

            assertEquals(originalEmail, sampleAccount.getEmail());
            assertEquals("patient@example.com", sampleAccount.getEmail());
        }
    }

    @Nested
    @DisplayName("Water Intake Tracking")
    class WaterTracking {

        @Test
        @DisplayName("addWater creates new WaterLog if none exists for today")
        void addWater_createsNewLog() {
            when(waterLogRepository.findAllByPatientIdAndLogDate(eq(10), any(LocalDate.class)))
                    .thenReturn(Collections.emptyList());

            patientService.addWater(samplePatient, 250);

            verify(waterLogRepository).save(any(WaterLog.class));
        }

        @Test
        @DisplayName("resetWater deletes all water logs for today")
        void resetWater_deletesLogs() {
            WaterLog log = WaterLog.builder().id(1).amountMl(500).build();
            when(waterLogRepository.findAllByPatientIdAndLogDate(eq(10), any(LocalDate.class)))
                    .thenReturn(List.of(log));

            int total = patientService.resetWater(samplePatient);

            assertEquals(0, total);
            verify(waterLogRepository).deleteAll(List.of(log));
        }
    }

    @Nested
    @DisplayName("Patient Meal Management")
    class MealManagement {

        @Test
        @DisplayName("addMeal throws exception if food dictionary item is missing")
        void addMeal_foodNotFound_throwsException() {
            when(foodDictionaryRepository.findById(99)).thenReturn(Optional.empty());

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    patientService.addMeal(samplePatient, "BREAKFAST", 99, 150.0));

            assertTrue(ex.getMessage().contains("Không tìm thấy món ăn"));
        }

        @Test
        @DisplayName("addMeal saves new meal record successfully")
        void addMeal_success() {
            FoodDictionary food = FoodDictionary.builder().id(5).foodName("Cơm trắng").build();
            when(foodDictionaryRepository.findById(5)).thenReturn(Optional.of(food));
            when(patientMealRepository.save(any(PatientMeal.class))).thenAnswer(i -> i.getArgument(0));

            PatientMeal meal = patientService.addMeal(samplePatient, "BREAKFAST", 5, 200.0);

            assertNotNull(meal);
            assertEquals("BREAKFAST", meal.getMealType());
            assertEquals(200.0, meal.getQuantityG());
            assertEquals(food, meal.getFood());
        }
    }
}
