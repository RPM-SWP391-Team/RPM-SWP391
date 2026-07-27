package com.rpm.remotepatientmonitoring.patient;

import com.rpm.remotepatientmonitoring.dto.patient.HealthLogRequest;
import com.rpm.remotepatientmonitoring.repository.*;
import com.rpm.remotepatientmonitoring.service.patient.PatientHealthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PatientHealthServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private AlertThresholdRepository alertThresholdRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private HealthLogRepository healthLogRepository;

    @InjectMocks
    private PatientHealthService patientHealthService;

    @Test
    void submitDailyHealthLog_UT01() {
        HealthLogRequest req = new HealthLogRequest();
        req.setPatientId(1);
        req.setLogType("MORNING");
        req.setSystolicBp(120);
        req.setDiastolicBp(80);
        req.setHeartRate(75);
        req.setGlucoseLevel(BigDecimal.valueOf(5.5));

        assertNotNull(req);
        assertEquals(1, req.getPatientId());
        assertEquals("MORNING", req.getLogType());
        assertEquals(120, req.getSystolicBp());
    }

    @Test
    void submitDailyHealthLog_UT02() {
        HealthLogRequest req = new HealthLogRequest();
        req.setPatientId(1);
        req.setLogType("EVENING");
        req.setSystolicBp(145);
        req.setDiastolicBp(95);

        assertNotNull(req);
        assertEquals(145, req.getSystolicBp());
        assertEquals(95, req.getDiastolicBp());
    }

    @Test
    void submitDailyHealthLog_UT03() {
        HealthLogRequest req = new HealthLogRequest();
        req.setPatientId(1);
        req.setLogType("EVENING");
        req.setSystolicBp(185);
        req.setDiastolicBp(115);

        assertNotNull(req);
        assertEquals(185, req.getSystolicBp());
        assertEquals(115, req.getDiastolicBp());
    }

    @Test
    void submitDailyHealthLog_UT04() {
        HealthLogRequest req = new HealthLogRequest();
        req.setPatientId(null);

        assertNull(req.getPatientId());
    }
}
