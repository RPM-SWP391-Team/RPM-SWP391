package com.rpm.remotepatientmonitoring.service.doctor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rpm.remotepatientmonitoring.model.AuditTrail;
import com.rpm.remotepatientmonitoring.repository.AuditTrailRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditTrailServiceTest {

    @Mock
    private AuditTrailRepository auditTrailRepository;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AuditTrailService auditTrailService;

    @Test
    void logAction_TC01_NullRequest_SavesLogSuccessfully() {
        // Arrange
        ReflectionTestUtils.setField(auditTrailService, "request", null);

        // Act
        auditTrailService.logAction("DOCTOR", 1, "UPDATE", "patients", 100, "OldValue", "NewValue", "Notes");

        // Assert
        ArgumentCaptor<AuditTrail> captor = ArgumentCaptor.forClass(AuditTrail.class);
        verify(auditTrailRepository, times(1)).save(captor.capture());
        
        AuditTrail savedAudit = captor.getValue();
        assertNull(savedAudit.getIpAddress());
        assertNull(savedAudit.getDeviceInfo());
        assertEquals("OldValue", savedAudit.getOldValue());
    }

    @Test
    void logAction_TC02_WithRequest_LongDeviceInfo_SavesLogSuccessfully() {
        // Arrange
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("10.0.0.1");
        
        String longAgent = "A".repeat(600);
        when(request.getHeader("User-Agent")).thenReturn(longAgent);

        // Act
        auditTrailService.logAction("DOCTOR", 1, "UPDATE", "patients", 100, null, null, "Notes");

        // Assert
        ArgumentCaptor<AuditTrail> captor = ArgumentCaptor.forClass(AuditTrail.class);
        verify(auditTrailRepository, times(1)).save(captor.capture());
        
        AuditTrail savedAudit = captor.getValue();
        assertEquals("10.0.0.1", savedAudit.getIpAddress());
        assertEquals(500, savedAudit.getDeviceInfo().length());
        assertTrue(savedAudit.getDeviceInfo().endsWith("..."));
    }

    @Test
    void logAction_TC03_ComplexObject_StripsPasswords() {
        // Arrange
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1");
        
        Map<String, Object> patientObj = new HashMap<>();
        patientObj.put("id", 10);
        patientObj.put("fullName", "Nguyen Van A");

        Map<String, Object> complexObj = new HashMap<>();
        complexObj.put("username", "admin");
        complexObj.put("passwordHash", "secret123456");
        complexObj.put("token", "my-jwt-token");
        complexObj.put("patient", patientObj);
        
        // Nested array
        List<Map<String, Object>> array = new ArrayList<>();
        Map<String, Object> nested = new HashMap<>();
        nested.put("account", "AccountInfo");
        array.add(nested);
        complexObj.put("list", array);

        // Act
        auditTrailService.logAction("DOCTOR", 1, "CREATE", "users", 10, null, complexObj, "Create User");

        // Assert
        ArgumentCaptor<AuditTrail> captor = ArgumentCaptor.forClass(AuditTrail.class);
        verify(auditTrailRepository, times(1)).save(captor.capture());
        
        AuditTrail savedAudit = captor.getValue();
        String json = savedAudit.getNewValue();
        
        // Verify stripped fields
        assertTrue(json.contains("***PROTECTED***"));
        assertFalse(json.contains("secret123456"));
        assertFalse(json.contains("my-jwt-token"));
        
        // Verify JPA stripped fields
        assertTrue(json.contains("\"patientId\":10"));
        assertTrue(json.contains("\"patientName\":\"Nguyen Van A\""));
        assertFalse(json.contains("\"account\":\"AccountInfo\""));
    }
    
    @Test
    void logAction_TC04_ObjectThrowsException_UsesFallback() {
        // Arrange
        // To 1 object gAy li kA-p JsonProcessingException cho ObjectMapper
        Object invalidObj = new Object() {
            @Override
            public String toString() {
                return "MockInvalidObject";
            }
        };

        // Act
        auditTrailService.logAction("DOCTOR", 1, "UPDATE", "patients", 100, null, invalidObj, "Notes");

        // Assert
        ArgumentCaptor<AuditTrail> captor = ArgumentCaptor.forClass(AuditTrail.class);
        verify(auditTrailRepository, times(1)).save(captor.capture());
        
        AuditTrail savedAudit = captor.getValue();
        assertTrue(savedAudit.getNewValue().contains("MockInvalidObject"));
    }
}
