package com.rpm.remotepatientmonitoring.service;

import com.rpm.remotepatientmonitoring.model.ExerciseLog;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.model.TreatmentPlan;
import com.rpm.remotepatientmonitoring.repository.ExerciseLogRepository;
import com.rpm.remotepatientmonitoring.repository.PatientRepository;
import com.rpm.remotepatientmonitoring.repository.TreatmentPlanRepository;
import com.rpm.remotepatientmonitoring.service.patient.ExerciseLogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExerciseLogServiceTest {

    @Mock
    private ExerciseLogRepository exerciseLogRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private TreatmentPlanRepository treatmentPlanRepository;

    @InjectMocks
    private ExerciseLogService exerciseLogService;

    @Mock
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Mock
    private com.rpm.remotepatientmonitoring.repository.NotificationRepository notificationRepository;

    @Test
    public void testGetTodaySummary_WithLogs() {
        Integer patientId = 1;
        LocalDate today = LocalDate.now();

        Patient patient = new Patient();
        patient.setId(patientId);

        // Logs registered today
        ExerciseLog log1 = new ExerciseLog(patient, today, "Đi bộ", 20, 2000, 80.0);
        ExerciseLog log2 = new ExerciseLog(patient, today, "Đạp xe", 15, null, 90.0);

        List<ExerciseLog> todayLogs = Arrays.asList(log1, log2);

        when(treatmentPlanRepository.findByPatientIdAndIsCurrent(patientId, true)).thenReturn(java.util.Optional.empty());
        when(exerciseLogRepository.findByPatientIdAndLogDate(patientId, today)).thenReturn(todayLogs);

        Map<String, Object> summary = exerciseLogService.getTodaySummary(patientId);

        assertNotNull(summary);
        assertEquals(35, summary.get("totalMinutes"));
        assertEquals(170.0, (Double) summary.get("totalCaloriesBurned"), 0.001);
        assertEquals(30, summary.get("targetMinutes"));
        assertEquals(100, summary.get("exerciseProgress")); // Capped at 100% since 35 > 30

        verify(exerciseLogRepository, times(1)).findByPatientIdAndLogDate(patientId, today);
    }

    @Test
    public void testGetTodaySummary_NoLogs() {
        Integer patientId = 1;
        LocalDate today = LocalDate.now();

        when(treatmentPlanRepository.findByPatientIdAndIsCurrent(patientId, true)).thenReturn(java.util.Optional.empty());
        when(exerciseLogRepository.findByPatientIdAndLogDate(patientId, today)).thenReturn(Collections.emptyList());

        Map<String, Object> summary = exerciseLogService.getTodaySummary(patientId);

        assertNotNull(summary);
        assertEquals(0, summary.get("totalMinutes"));
        assertEquals(0.0, (Double) summary.get("totalCaloriesBurned"), 0.001);
        assertEquals(30, summary.get("targetMinutes"));
        assertEquals(0, summary.get("exerciseProgress"));

        verify(exerciseLogRepository, times(1)).findByPatientIdAndLogDate(patientId, today);
    }

    @Test
    public void testGetHistorySummary_VerifyGroupingAndPadding() {
        Integer patientId = 1;
        int days = 7;
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        Patient patient = new Patient();
        patient.setId(patientId);

        // Giả lập 2 log trên cùng 1 ngày (hôm nay) và 1 log ở 2 ngày trước
        ExerciseLog logToday1 = new ExerciseLog(patient, endDate, "Đi bộ", 10, 1000, 40.0);
        ExerciseLog logToday2 = new ExerciseLog(patient, endDate, "Yoga", 20, null, 60.0);
        ExerciseLog logTwoDaysAgo = new ExerciseLog(patient, endDate.minusDays(2), "Đạp xe", 30, null, 180.0);

        List<ExerciseLog> dbLogs = Arrays.asList(logToday1, logToday2, logTwoDaysAgo);

        when(exerciseLogRepository.findByPatientIdAndLogDateBetween(patientId, startDate, endDate)).thenReturn(dbLogs);

        List<Map<String, Object>> history = exerciseLogService.getHistorySummary(patientId, days);

        assertNotNull(history);
        assertEquals(7, history.size()); // Đủ 7 ngày

        // Hôm nay (vị trí cuối cùng index = 6)
        Map<String, Object> todaySummary = history.get(6);
        assertEquals(endDate, todaySummary.get("logDate"));
        assertEquals(30, todaySummary.get("totalMinutes")); // 10 + 20
        assertEquals(100.0, (Double) todaySummary.get("totalCaloriesBurned"), 0.001); // 40 + 60

        // Ngày hôm qua (index = 5, không có dữ liệu -> 0)
        Map<String, Object> yesterdaySummary = history.get(5);
        assertEquals(endDate.minusDays(1), yesterdaySummary.get("logDate"));
        assertEquals(0, yesterdaySummary.get("totalMinutes"));
        assertEquals(0.0, (Double) yesterdaySummary.get("totalCaloriesBurned"), 0.001);

        // 2 ngày trước (index = 4)
        Map<String, Object> twoDaysAgoSummary = history.get(4);
        assertEquals(endDate.minusDays(2), twoDaysAgoSummary.get("logDate"));
        assertEquals(30, twoDaysAgoSummary.get("totalMinutes"));
        assertEquals(180.0, (Double) twoDaysAgoSummary.get("totalCaloriesBurned"), 0.001);

        verify(exerciseLogRepository, times(1)).findByPatientIdAndLogDateBetween(patientId, startDate, endDate);
    }

    @Test
    public void testGetTodaySummary_WithPersonalizedGoalFromSteps() {
        Integer patientId = 1;
        LocalDate today = LocalDate.now();

        Patient patient = new Patient();
        patient.setId(patientId);

        TreatmentPlan plan = new TreatmentPlan();
        plan.setPatient(patient);
        plan.setIsCurrent(true);
        plan.setTargetSteps(4000); // 4000 steps / 100 = 40 minutes target

        when(treatmentPlanRepository.findByPatientIdAndIsCurrent(patientId, true)).thenReturn(java.util.Optional.of(plan));
        when(exerciseLogRepository.findByPatientIdAndLogDate(patientId, today)).thenReturn(Collections.emptyList());

        Map<String, Object> summary = exerciseLogService.getTodaySummary(patientId);

        assertNotNull(summary);
        assertEquals(0, summary.get("totalMinutes"));
        assertEquals(40, summary.get("targetMinutes")); // 4000 / 100
        assertEquals(0, summary.get("exerciseProgress"));
    }

    @Test
    public void testGetTodaySummary_WithPersonalizedGoalFromExerciseGoal() {
        Integer patientId = 1;
        LocalDate today = LocalDate.now();

        Patient patient = new Patient();
        patient.setId(patientId);

        TreatmentPlan plan = new TreatmentPlan();
        plan.setPatient(patient);
        plan.setIsCurrent(true);
        plan.setExerciseGoal("Tập luyện 45 phút mỗi ngày"); // should parse 45 minutes

        when(treatmentPlanRepository.findByPatientIdAndIsCurrent(patientId, true)).thenReturn(java.util.Optional.of(plan));
        when(exerciseLogRepository.findByPatientIdAndLogDate(patientId, today)).thenReturn(Collections.emptyList());

        Map<String, Object> summary = exerciseLogService.getTodaySummary(patientId);

        assertNotNull(summary);
        assertEquals(0, summary.get("totalMinutes"));
        assertEquals(45, summary.get("targetMinutes")); // parsed 45
        assertEquals(0, summary.get("exerciseProgress"));
    }

    @Test
    public void testGetCurrentStreak_ContinuousStreak() {
        Integer patientId = 1;
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(3650);

        Patient patient = new Patient();
        patient.setId(patientId);

        // Giả lập phác đồ trống -> targetMinutes = 30
        when(treatmentPlanRepository.findByPatientIdAndIsCurrent(patientId, true)).thenReturn(java.util.Optional.empty());

        // Gom nhóm: Hôm nay đạt, Hôm qua đạt, 2 ngày trước đạt, 3 ngày trước không đạt
        ExerciseLog logToday = new ExerciseLog(patient, today, "Đi bộ", 35, null, 140.0);
        ExerciseLog logYesterday = new ExerciseLog(patient, today.minusDays(1), "Chạy bộ", 40, null, 240.0);
        ExerciseLog logTwoDaysAgo = new ExerciseLog(patient, today.minusDays(2), "Yoga", 30, null, 90.0);
        ExerciseLog logThreeDaysAgo = new ExerciseLog(patient, today.minusDays(3), "Đi bộ", 10, null, 40.0);

        List<ExerciseLog> dbLogs = Arrays.asList(logToday, logYesterday, logTwoDaysAgo, logThreeDaysAgo);

        when(exerciseLogRepository.findByPatientIdAndLogDateBetween(patientId, startDate, today)).thenReturn(dbLogs);

        int streak = exerciseLogService.getCurrentStreak(patientId);

        assertEquals(3, streak); // 3 ngày liên tiếp đạt (Hôm nay, hôm qua, 2 ngày trước)
        verify(exerciseLogRepository, times(1)).findByPatientIdAndLogDateBetween(patientId, startDate, today);
    }

    @Test
    public void testGetCurrentStreak_BrokenStreakToday() {
        Integer patientId = 1;
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(3650);

        Patient patient = new Patient();
        patient.setId(patientId);

        when(treatmentPlanRepository.findByPatientIdAndIsCurrent(patientId, true)).thenReturn(java.util.Optional.empty());

        // Hôm nay không đạt (10 phút), hôm qua đạt (30 phút), 2 ngày trước đạt (45 phút), 3 ngày trước không đạt (15 phút)
        ExerciseLog logToday = new ExerciseLog(patient, today, "Đi bộ", 10, null, 40.0);
        ExerciseLog logYesterday = new ExerciseLog(patient, today.minusDays(1), "Chạy bộ", 30, null, 180.0);
        ExerciseLog logTwoDaysAgo = new ExerciseLog(patient, today.minusDays(2), "Yoga", 45, null, 135.0);
        ExerciseLog logThreeDaysAgo = new ExerciseLog(patient, today.minusDays(3), "Đi bộ", 15, null, 60.0);

        List<ExerciseLog> dbLogs = Arrays.asList(logToday, logYesterday, logTwoDaysAgo, logThreeDaysAgo);

        when(exerciseLogRepository.findByPatientIdAndLogDateBetween(patientId, startDate, today)).thenReturn(dbLogs);

        int streak = exerciseLogService.getCurrentStreak(patientId);

        assertEquals(2, streak); // Hôm nay chưa đạt nhưng vẫn giữ streak 2 ngày trước đó (hôm qua và 2 ngày trước)
    }

    @Test
    public void testGetCurrentStreak_NoLogs() {
        Integer patientId = 1;
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(3650);

        when(treatmentPlanRepository.findByPatientIdAndIsCurrent(patientId, true)).thenReturn(java.util.Optional.empty());
        when(exerciseLogRepository.findByPatientIdAndLogDateBetween(patientId, startDate, today)).thenReturn(Collections.emptyList());

        int streak = exerciseLogService.getCurrentStreak(patientId);

        assertEquals(0, streak);
    }

    @Test
    public void testGetLatestBmi_Success() {
        Integer patientId = 1;
        Map<String, Object> mockRow = new HashMap<>();
        mockRow.put("weight_kg", new java.math.BigDecimal("65.5"));
        mockRow.put("height_cm", new java.math.BigDecimal("170.0"));
        mockRow.put("bmi", null); // triggers calculation
        mockRow.put("examination_date", java.sql.Timestamp.valueOf(LocalDateTime.of(2026, 7, 12, 10, 0)));

        when(jdbcTemplate.queryForList(anyString(), eq(patientId))).thenReturn(Collections.singletonList(mockRow));

        Map<String, Object> result = exerciseLogService.getLatestBmi(patientId);

        assertNotNull(result);
        assertEquals(65.5, result.get("weightKg"));
        assertEquals(170.0, result.get("heightCm"));
        assertEquals(22.7, result.get("bmiValue")); // 65.5 / 1.7^2 = 22.66 -> 22.7
        assertEquals("Bình thường", result.get("bmiCategory"));
        assertEquals("12/07/2026", result.get("examinationDate"));
    }

    @Test
    public void testGetLatestBmi_Empty() {
        Integer patientId = 1;
        when(jdbcTemplate.queryForList(anyString(), eq(patientId))).thenReturn(Collections.emptyList());

        Map<String, Object> result = exerciseLogService.getLatestBmi(patientId);

        assertNull(result);
    }

    @Test
    public void testGetUnreadExerciseNotificationsToday() {
        Integer patientId = 1;
        List<com.rpm.remotepatientmonitoring.model.Notification> mockNotifs = Collections.singletonList(new com.rpm.remotepatientmonitoring.model.Notification());
        
        when(notificationRepository.findUnreadExerciseNotifications(eq(patientId)))
                .thenReturn(mockNotifs);

        List<com.rpm.remotepatientmonitoring.model.Notification> result = exerciseLogService.getUnreadExerciseNotificationsToday(patientId);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testIsStreakAtRiskToday_True() {
        Integer patientId = 1;
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(3650);

        Patient patient = new Patient();
        patient.setId(patientId);

        // Target: 30 minutes
        when(treatmentPlanRepository.findByPatientIdAndIsCurrent(patientId, true)).thenReturn(java.util.Optional.empty());

        // Logs: yesterday achieved (30 min), today logged only 10 min
        ExerciseLog logYesterday = new ExerciseLog(patient, today.minusDays(1), "Đi bộ", 30, null, 120.0);
        ExerciseLog logToday = new ExerciseLog(patient, today, "Chạy bộ", 10, null, 60.0);
        List<ExerciseLog> dbLogs = Arrays.asList(logYesterday, logToday);

        when(exerciseLogRepository.findByPatientIdAndLogDateBetween(patientId, startDate, today)).thenReturn(dbLogs);
        when(exerciseLogRepository.findByPatientIdAndLogDate(patientId, today)).thenReturn(Collections.singletonList(logToday));

        boolean atRisk = exerciseLogService.isStreakAtRiskToday(patientId);

        assertTrue(atRisk);
    }

    @Test
    public void testIsStreakAtRiskToday_False_MetTarget() {
        Integer patientId = 1;
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(3650);

        Patient patient = new Patient();
        patient.setId(patientId);

        when(treatmentPlanRepository.findByPatientIdAndIsCurrent(patientId, true)).thenReturn(java.util.Optional.empty());

        // Logs: yesterday achieved (30 min), today achieved (30 min)
        ExerciseLog logYesterday = new ExerciseLog(patient, today.minusDays(1), "Đi bộ", 30, null, 120.0);
        ExerciseLog logToday = new ExerciseLog(patient, today, "Chạy bộ", 30, null, 180.0);
        List<ExerciseLog> dbLogs = Arrays.asList(logYesterday, logToday);

        when(exerciseLogRepository.findByPatientIdAndLogDateBetween(patientId, startDate, today)).thenReturn(dbLogs);
        when(exerciseLogRepository.findByPatientIdAndLogDate(patientId, today)).thenReturn(Collections.singletonList(logToday));

        boolean atRisk = exerciseLogService.isStreakAtRiskToday(patientId);

        assertFalse(atRisk);
    }

    @Test
    public void testIsStreakAtRiskToday_False_NoStreak() {
        Integer patientId = 1;
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(3650);

        Patient patient = new Patient();
        patient.setId(patientId);

        when(treatmentPlanRepository.findByPatientIdAndIsCurrent(patientId, true)).thenReturn(java.util.Optional.empty());

        // Logs: yesterday achieved 0 min (no logs), today logged 10 min
        ExerciseLog logToday = new ExerciseLog(patient, today, "Chạy bộ", 10, null, 60.0);
        List<ExerciseLog> dbLogs = Collections.singletonList(logToday);

        when(exerciseLogRepository.findByPatientIdAndLogDateBetween(patientId, startDate, today)).thenReturn(dbLogs);

        boolean atRisk = exerciseLogService.isStreakAtRiskToday(patientId);

        assertFalse(atRisk);
    }

    @Test
    public void testIsShortBurstOverexertion_True() {
        Integer patientId = 1;
        Patient patient = new Patient();
        patient.setId(patientId);

        // Logs in the last 60 mins: total 310 kcal (> 300)
        ExerciseLog log1 = new ExerciseLog(patient, LocalDate.now(), "Đạp xe", 30, null, 180.0);
        ExerciseLog log2 = new ExerciseLog(patient, LocalDate.now(), "Bơi lội", 20, null, 130.0);
        List<ExerciseLog> dbLogs = Arrays.asList(log1, log2);

        when(exerciseLogRepository.findByPatientIdAndLoggedAtGreaterThanEqual(eq(patientId), any(LocalDateTime.class)))
                .thenReturn(dbLogs);

        boolean result = exerciseLogService.isShortBurstOverexertion(patientId);

        assertTrue(result);
    }

    @Test
    public void testIsShortBurstOverexertion_False() {
        Integer patientId = 1;
        Patient patient = new Patient();
        patient.setId(patientId);

        // Logs in the last 60 mins: total 200 kcal (<= 300)
        ExerciseLog log1 = new ExerciseLog(patient, LocalDate.now(), "Yoga", 30, null, 90.0);
        ExerciseLog log2 = new ExerciseLog(patient, LocalDate.now(), "Đi bộ", 30, 2500, 110.0);
        List<ExerciseLog> dbLogs = Arrays.asList(log1, log2);

        when(exerciseLogRepository.findByPatientIdAndLoggedAtGreaterThanEqual(eq(patientId), any(LocalDateTime.class)))
                .thenReturn(dbLogs);

        boolean result = exerciseLogService.isShortBurstOverexertion(patientId);

        assertFalse(result);
    }

    @Test
    public void testIsShortBurstOverexertion_False_NoLogs() {
        Integer patientId = 1;

        when(exerciseLogRepository.findByPatientIdAndLoggedAtGreaterThanEqual(eq(patientId), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        boolean result = exerciseLogService.isShortBurstOverexertion(patientId);

        assertFalse(result);
    }
}
