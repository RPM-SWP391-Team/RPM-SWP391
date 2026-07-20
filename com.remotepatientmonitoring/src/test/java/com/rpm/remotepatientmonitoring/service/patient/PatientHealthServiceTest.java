package com.rpm.remotepatientmonitoring.service.patient;

import com.rpm.remotepatientmonitoring.dto.patient.HealthLogRequest;
import com.rpm.remotepatientmonitoring.model.Alert;
import com.rpm.remotepatientmonitoring.model.DailyHealthLog;
import com.rpm.remotepatientmonitoring.model.Doctor;
import com.rpm.remotepatientmonitoring.model.Notification;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.repository.AlertRepository;
import com.rpm.remotepatientmonitoring.repository.HealthLogRepository;
import com.rpm.remotepatientmonitoring.repository.NotificationRepository;
import com.rpm.remotepatientmonitoring.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Chuoi
 */
@ExtendWith(MockitoExtension.class)
class PatientHealthServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private HealthLogRepository healthLogRepository;

    @InjectMocks
    private PatientHealthService patientHealthService;

    private HealthLogRequest req;
    private Patient patient;
    private Doctor doctor;
    private DailyHealthLog latestLog;

    @BeforeEach
    void setUp() {
        // Chuẩn bị dữ liệu mẫu dùng chung cho các Test Case
        req = new HealthLogRequest();
        req.setPatientId(1);

        doctor = new Doctor();
        doctor.setId(10);
        doctor.setFullName("Dr. Smith");

        patient = new Patient();
        patient.setId(1);
        patient.setFullName("Nguyen Van A");
        patient.setDoctor(doctor);

        latestLog = new DailyHealthLog();
        latestLog.setId(100);
    }

    // Tiện ích gọi hàm private bằng ReflectionTestUtils
    private void invokeEvaluateAndGenerateAlerts(HealthLogRequest request) {
        ReflectionTestUtils.invokeMethod(patientHealthService, "evaluateAndGenerateAlerts", request);
    }

    @Test
    void evaluateAndGenerateAlerts_TC01_PatientNotFound() {
        when(patientRepository.findById(1)).thenReturn(Optional.empty());

        invokeEvaluateAndGenerateAlerts(req);

        // Đảm bảo không có lệnh lưu nào được gọi
        verify(alertRepository, never()).save(any());
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void evaluateAndGenerateAlerts_TC02_NoDoctorAssigned() {
        patient.setDoctor(null);
        when(patientRepository.findById(1)).thenReturn(Optional.of(patient));

        invokeEvaluateAndGenerateAlerts(req);

        verify(alertRepository, never()).save(any());
    }

    @Test
    void evaluateAndGenerateAlerts_TC03_NormalMetrics() {
        // Chỉ số bình thường
        req.setSystolicBp(120);
        req.setDiastolicBp(80);
        req.setGlucoseLevel(BigDecimal.valueOf(5.5)); // Kiểu Double/BigDecimal tuỳ DTO của bạn

        when(patientRepository.findById(1)).thenReturn(Optional.of(patient));
        when(healthLogRepository.findFirstByPatientIdOrderByLogTimeDesc(1)).thenReturn(Optional.of(latestLog));

        invokeEvaluateAndGenerateAlerts(req);

        // Mức độ finalLevel = 1 -> không lưu Alert
        verify(alertRepository, never()).save(any());
    }

    @Test
    void evaluateAndGenerateAlerts_TC04_Level2_HighSystolic() {
        // Huyết áp tâm thu cao (mức 2)
        req.setSystolicBp(150);
        req.setDiastolicBp(85);

        when(patientRepository.findById(1)).thenReturn(Optional.of(patient));
        when(healthLogRepository.findFirstByPatientIdOrderByLogTimeDesc(1)).thenReturn(Optional.of(latestLog));

        invokeEvaluateAndGenerateAlerts(req);

        // Tóm lấy object Alert để kiểm tra chi tiết
        ArgumentCaptor<Alert> alertCaptor = ArgumentCaptor.forClass(Alert.class);
        verify(alertRepository, times(1)).save(alertCaptor.capture());

        Alert savedAlert = alertCaptor.getValue();
        assertEquals(2, savedAlert.getAlertLevel());
        assertEquals("ORANGE", savedAlert.getAlertColor());
        assertEquals("BLOOD_PRESSURE", savedAlert.getMetricType());
        assertTrue(savedAlert.getAlertMessage().contains("Huyết áp tâm thu cao"));

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void evaluateAndGenerateAlerts_TC05_Level3_CriticalGlucose() {
        // Hạ đường huyết (mức 3)
        req.setGlucoseLevel(BigDecimal.valueOf(3.5));

        when(patientRepository.findById(1)).thenReturn(Optional.of(patient));
        when(healthLogRepository.findFirstByPatientIdOrderByLogTimeDesc(1)).thenReturn(Optional.of(latestLog));

        invokeEvaluateAndGenerateAlerts(req);

        ArgumentCaptor<Alert> alertCaptor = ArgumentCaptor.forClass(Alert.class);
        verify(alertRepository).save(alertCaptor.capture());

        Alert savedAlert = alertCaptor.getValue();
        assertEquals(3, savedAlert.getAlertLevel());
        assertEquals("RED", savedAlert.getAlertColor());
        assertEquals("GLUCOSE", savedAlert.getMetricType());
        assertEquals("<4.4", savedAlert.getThresholdViolated());
        assertTrue(savedAlert.getAlertMessage().contains("Hạ đường huyết"));

        // Kiểm tra logic gửi Notification
        ArgumentCaptor<Notification> notifCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notifCaptor.capture());

        Notification savedNotif = notifCaptor.getValue();
        assertEquals("DOCTOR", savedNotif.getRecipientType());
        assertEquals(10, savedNotif.getRecipientId());
        assertTrue(savedNotif.getContent().contains("mức RED"));
    }

    @Test
    void evaluateAndGenerateAlerts_TC06_MultipleViolations_TakesMaxLevel() {
        // Sys mức 2, Dia mức 3 -> Kỳ vọng lấy mức 3
        req.setSystolicBp(150); // Mức 2
        req.setDiastolicBp(125); // Mức 3

        when(patientRepository.findById(1)).thenReturn(Optional.of(patient));
        when(healthLogRepository.findFirstByPatientIdOrderByLogTimeDesc(1)).thenReturn(Optional.of(latestLog));

        invokeEvaluateAndGenerateAlerts(req);

        ArgumentCaptor<Alert> alertCaptor = ArgumentCaptor.forClass(Alert.class);
        verify(alertRepository).save(alertCaptor.capture());

        Alert savedAlert = alertCaptor.getValue();
        assertEquals(3, savedAlert.getAlertLevel());
        assertEquals("RED", savedAlert.getAlertColor());

        // Đảm bảo message có chứa cảnh báo của cả hai chỉ số
        assertTrue(savedAlert.getAlertMessage().contains("Huyết áp tâm thu cao"));
        assertTrue(savedAlert.getAlertMessage().contains("Huyết áp tâm trương quá cao"));
    }

    @Test
    void evaluateAndGenerateAlerts_TC07_Level3_CriticalSystolic_NullDiastolic() {
        // Huyết áp tâm thu >= 180 và Cố tình để Tâm trương bị NULL để test dấu "?"
        req.setSystolicBp(180);
        req.setDiastolicBp(null);

        when(patientRepository.findById(1)).thenReturn(Optional.of(patient));
        when(healthLogRepository.findFirstByPatientIdOrderByLogTimeDesc(1)).thenReturn(Optional.of(latestLog));

        invokeEvaluateAndGenerateAlerts(req);

        ArgumentCaptor<Alert> alertCaptor = ArgumentCaptor.forClass(Alert.class);
        verify(alertRepository).save(alertCaptor.capture());

        Alert savedAlert = alertCaptor.getValue();
        assertEquals(3, savedAlert.getAlertLevel());
        // Kiểm tra xem toán tử 3 ngôi có in ra dấu "?" thành công không
        assertTrue(savedAlert.getMetricValue().contains("180/?"));
    }

    @Test
    void evaluateAndGenerateAlerts_TC08_Level2_HighDiastolic_Only() {
        // Chỉ có huyết áp tâm trương cao (nằm trong khoảng 90 đến 119)
        req.setSystolicBp(null);
        req.setDiastolicBp(100);

        when(patientRepository.findById(1)).thenReturn(Optional.of(patient));
        when(healthLogRepository.findFirstByPatientIdOrderByLogTimeDesc(1)).thenReturn(Optional.of(latestLog));

        invokeEvaluateAndGenerateAlerts(req);

        ArgumentCaptor<Alert> alertCaptor = ArgumentCaptor.forClass(Alert.class);
        verify(alertRepository).save(alertCaptor.capture());

        assertEquals(2, alertCaptor.getValue().getAlertLevel());
        assertEquals(">=90", alertCaptor.getValue().getThresholdViolated());
        // Dòng này sẽ giúp nhuộm xanh nhánh metricValue.isEmpty() ở dòng 94
        // vì nhánh xử lý Diastolic của bạn trong code gốc quên không set metricValue!
        assertEquals("N/A", alertCaptor.getValue().getMetricValue());
    }

    @Test
    void evaluateAndGenerateAlerts_TC09_Level3_CriticalHighGlucose() {
        // Đường huyết quá cao (> 16.0)
        req.setGlucoseLevel(BigDecimal.valueOf(18.5));

        when(patientRepository.findById(1)).thenReturn(Optional.of(patient));
        when(healthLogRepository.findFirstByPatientIdOrderByLogTimeDesc(1)).thenReturn(Optional.of(latestLog));

        invokeEvaluateAndGenerateAlerts(req);

        ArgumentCaptor<Alert> alertCaptor = ArgumentCaptor.forClass(Alert.class);
        verify(alertRepository).save(alertCaptor.capture());
        assertEquals(3, alertCaptor.getValue().getAlertLevel());
        assertEquals(">16.0", alertCaptor.getValue().getThresholdViolated());
    }

    @Test
    void evaluateAndGenerateAlerts_TC10_Level2_HighGlucose() {
        // Đường huyết cao (từ 10.1 đến 16.0)
        req.setGlucoseLevel(BigDecimal.valueOf(12.0));

        when(patientRepository.findById(1)).thenReturn(Optional.of(patient));
        when(healthLogRepository.findFirstByPatientIdOrderByLogTimeDesc(1)).thenReturn(Optional.of(latestLog));

        invokeEvaluateAndGenerateAlerts(req);

        ArgumentCaptor<Alert> alertCaptor = ArgumentCaptor.forClass(Alert.class);
        verify(alertRepository).save(alertCaptor.capture());
        assertEquals(2, alertCaptor.getValue().getAlertLevel());
        assertEquals(">10.0", alertCaptor.getValue().getThresholdViolated());
    }

    @Test
    void evaluateAndGenerateAlerts_TC11_Level3_CriticalSystolic_WithDiastolic() {
        // Dành cho dòng 67: Huyết áp tâm thu >= 180 VÀ Tâm trương CÓ giá trị
        req.setSystolicBp(185);
        req.setDiastolicBp(90);

        when(patientRepository.findById(1)).thenReturn(Optional.of(patient));
        when(healthLogRepository.findFirstByPatientIdOrderByLogTimeDesc(1)).thenReturn(Optional.of(latestLog));

        invokeEvaluateAndGenerateAlerts(req);

        ArgumentCaptor<Alert> alertCaptor = ArgumentCaptor.forClass(Alert.class);
        verify(alertRepository).save(alertCaptor.capture());

        // Kiểm tra xem nó có nối chuỗi thành "185/90" thay vì "185/?" không
        assertTrue(alertCaptor.getValue().getMetricValue().contains("185/90"));
    }

    @Test
    void evaluateAndGenerateAlerts_TC12_Level2_HighSystolic_NullDiastolic() {
        // Dành cho dòng 68: Huyết áp tâm thu >= 140 VÀ Tâm trương BỊ NULL
        req.setSystolicBp(145);
        req.setDiastolicBp(null);

        when(patientRepository.findById(1)).thenReturn(Optional.of(patient));
        when(healthLogRepository.findFirstByPatientIdOrderByLogTimeDesc(1)).thenReturn(Optional.of(latestLog));

        invokeEvaluateAndGenerateAlerts(req);

        ArgumentCaptor<Alert> alertCaptor = ArgumentCaptor.forClass(Alert.class);
        verify(alertRepository).save(alertCaptor.capture());

        // Kiểm tra xem nó có nối chuỗi thành "145/?" không
        assertTrue(alertCaptor.getValue().getMetricValue().contains("145/?"));
    }
}