package com.rpm.remotepatientmonitoring.exercise;

import com.rpm.remotepatientmonitoring.model.ExerciseLog;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.model.TreatmentPlan;
import com.rpm.remotepatientmonitoring.repository.ExerciseLogRepository;
import com.rpm.remotepatientmonitoring.repository.NotificationRepository;
import com.rpm.remotepatientmonitoring.repository.PatientRepository;
import com.rpm.remotepatientmonitoring.repository.TreatmentPlanRepository;
import com.rpm.remotepatientmonitoring.service.patient.ExerciseLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExerciseLogServiceTest {

    @Mock
    private ExerciseLogRepository exerciseLogRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private TreatmentPlanRepository treatmentPlanRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private ExerciseLogService exerciseLogService;

    private Patient samplePatient;

    @BeforeEach
    void setUp() {
        samplePatient = Patient.builder()
                .id(10)
                .fullName("Nguyen Van Patient")
                .build();
    }

    @Nested
    @DisplayName("Target & Today Summary Calculation")
    class TargetAndSummary {

        @Test
        @DisplayName("getTargetMinutesForPatient returns default 30 when no active treatment plan")
        void getTargetMinutes_noPlan_returnsDefault() {
            when(treatmentPlanRepository.findByPatientIdAndIsCurrent(10, true)).thenReturn(Optional.empty());

            int target = exerciseLogService.getTargetMinutesForPatient(10);

            assertEquals(30, target);
        }

        @Test
        @DisplayName("getTargetMinutesForPatient converts target steps to minutes")
        void getTargetMinutes_withSteps_convertsToMinutes() {
            TreatmentPlan plan = TreatmentPlan.builder().targetSteps(4000).build();
            when(treatmentPlanRepository.findByPatientIdAndIsCurrent(10, true)).thenReturn(Optional.of(plan));

            int target = exerciseLogService.getTargetMinutesForPatient(10);

            assertEquals(40, target); // 4000 / 100 = 40
        }

        @Test
        @DisplayName("getTodaySummary calculates total duration, calories burned and progress percentage")
        void getTodaySummary_calculatesCorrectly() {
            ExerciseLog log1 = new ExerciseLog(samplePatient, LocalDate.now(), "Đi bộ", 20, 2000, 100.0);
            ExerciseLog log2 = new ExerciseLog(samplePatient, LocalDate.now(), "Chạy bộ", 10, 1000, 50.0);

            when(exerciseLogRepository.findByPatientIdAndLogDate(eq(10), any(LocalDate.class)))
                    .thenReturn(List.of(log1, log2));
            when(treatmentPlanRepository.findByPatientIdAndIsCurrent(10, true)).thenReturn(Optional.empty());

            Map<String, Object> summary = exerciseLogService.getTodaySummary(10);

            assertEquals(30, summary.get("totalMinutes"));
            assertEquals(150.0, summary.get("totalCaloriesBurned"));
            assertEquals(30, summary.get("targetMinutes"));
            assertEquals(100, summary.get("exerciseProgress"));
        }
    }

    @Nested
    @DisplayName("Save & Update Exercise Log")
    class SaveAndUpdateLog {

        @Test
        @DisplayName("saveExerciseLog successfully saves valid exercise log")
        void saveExerciseLog_success() {
            when(patientRepository.findById(10)).thenReturn(Optional.of(samplePatient));
            when(exerciseLogRepository.save(any(ExerciseLog.class))).thenAnswer(i -> i.getArgument(0));

            ExerciseLog saved = exerciseLogService.saveExerciseLog(10, "Đi bộ", 3000, 30, 120.0);

            assertNotNull(saved);
            assertEquals("Đi bộ", saved.getExerciseType());
            assertEquals(30, saved.getDurationMinutes());
            assertEquals(3000, saved.getStepsCount());
            assertEquals(120.0, saved.getCaloriesBurned());
        }

        @Test
        @DisplayName("saveExerciseLog throws exception on invalid duration (<= 0 or > 1440)")
        void saveExerciseLog_invalidDuration_throwsException() {
            IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () ->
                    exerciseLogService.saveExerciseLog(10, "Chạy bộ", 1000, 0, 50.0));
            assertTrue(ex1.getMessage().contains("lớn hơn 0 phút"));

            IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () ->
                    exerciseLogService.saveExerciseLog(10, "Chạy bộ", 1000, 1500, 50.0));
            assertTrue(ex2.getMessage().contains("không được vượt quá 1440 phút"));
        }

        @Test
        @DisplayName("saveExerciseLog throws exception on negative steps or > 100,000")
        void saveExerciseLog_invalidSteps_throwsException() {
            IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () ->
                    exerciseLogService.saveExerciseLog(10, "Đi bộ", -10, 20, 50.0));
            assertTrue(ex1.getMessage().contains("không được nhỏ hơn 0"));

            IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () ->
                    exerciseLogService.saveExerciseLog(10, "Đi bộ", 200000, 20, 50.0));
            assertTrue(ex2.getMessage().contains("không được vượt quá 100,000"));
        }

        @Test
        @DisplayName("updateExerciseLog checks ownership and updates fields")
        void updateExerciseLog_success() {
            ExerciseLog existingLog = new ExerciseLog(samplePatient, LocalDate.now(), "Chạy bộ", 15, 1500, 80.0);
            existingLog.setId(1);

            when(exerciseLogRepository.findById(1)).thenReturn(Optional.of(existingLog));
            when(exerciseLogRepository.save(any(ExerciseLog.class))).thenAnswer(i -> i.getArgument(0));

            ExerciseLog updated = exerciseLogService.updateExerciseLog(1, 10, "Đạp xe", 2000, 45, 180.0);

            assertEquals("Đạp xe", updated.getExerciseType());
            assertEquals(45, updated.getDurationMinutes());
            assertEquals(2000, updated.getStepsCount());
        }

        @Test
        @DisplayName("deleteExerciseLog checks ownership and deletes log")
        void deleteExerciseLog_success() {
            ExerciseLog existingLog = new ExerciseLog(samplePatient, LocalDate.now(), "Chạy bộ", 15, 1500, 80.0);
            existingLog.setId(1);

            when(exerciseLogRepository.findById(1)).thenReturn(Optional.of(existingLog));

            assertDoesNotThrow(() -> exerciseLogService.deleteExerciseLog(1, 10));
            verify(exerciseLogRepository).delete(existingLog);
        }
    }
}
