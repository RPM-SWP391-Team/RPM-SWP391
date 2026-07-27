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

        Map<String, Object> hospitalObj = new HashMap<>();
        hospitalObj.put("id", 5);

        Map<String, Object> doctorObj = new HashMap<>();
        doctorObj.put("id", 20);
        doctorObj.put("fullName", "Dr. Smith");

        Map<String, Object> nutritionRuleObj = new HashMap<>();
        nutritionRuleObj.put("id", 99);

        Map<String, Object> complexObj = new HashMap<>();
        complexObj.put("username", "admin");
        complexObj.put("passwordHash", "secret123456");
        complexObj.put("token", "my-jwt-token");
        complexObj.put("patient", patientObj);
        complexObj.put("hospital", hospitalObj);
        complexObj.put("doctor", doctorObj);
        complexObj.put("nutritionRule", nutritionRuleObj);
        
        // Nested array
        List<Map<String, Object>> array = new ArrayList<>();
        Map<String, Object> nested = new HashMap<>();
        nested.put("account", "AccountInfo");
        array.add(nested);
        complexObj.put("list", array);

        // Act
        auditTrailService.logAction("DOCTOR", 1, "CREATE", "users", 10, "{\"password\":\"123456\"}", complexObj, "Create User");

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
        assertTrue(json.contains("\"hospitalId\":5"));
        assertTrue(json.contains("\"doctorId\":20"));
        assertTrue(json.contains("\"doctorName\":\"Dr. Smith\""));
        assertTrue(json.contains("\"nutritionRuleId\":99"));
        assertFalse(json.contains("\"account\":\"AccountInfo\""));
        assertTrue(savedAudit.getOldValue().contains("***PROTECTED***"));
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

    @Test
    void logAction_TC05_NormalDeviceInfo_CoversLengthConditionFalse() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (Windows NT 10.0)");

        auditTrailService.logAction("DOCTOR", 1, "UPDATE", "patients", 100, null, null, "Notes");

        ArgumentCaptor<AuditTrail> captor = ArgumentCaptor.forClass(AuditTrail.class);
        verify(auditTrailRepository, times(1)).save(captor.capture());
        assertEquals("Mozilla/5.0 (Windows NT 10.0)", captor.getValue().getDeviceInfo());
    }

    @Test
    void logAction_TC06_EntitiesWithoutIdOrEmpty_CoversFalseBranches() {
        Map<String, Object> emptyMap = new HashMap<>();

        Map<String, Object> patientObjNoName = new HashMap<>();
        patientObjNoName.put("id", 10); // has id but no fullName

        Map<String, Object> doctorObjNoName = new HashMap<>();
        doctorObjNoName.put("id", 20); // has id but no fullName

        Map<String, Object> complexObj = new HashMap<>();
        complexObj.put("hospital", emptyMap); // hospNode.has("id") -> false
        complexObj.put("patient", patientObjNoName); // patNode.has("fullName") -> false
        complexObj.put("doctor", doctorObjNoName); // docNode.has("fullName") -> false
        complexObj.put("nutritionRule", emptyMap); // nutNode.has("id") -> false

        auditTrailService.logAction("DOCTOR", 1, "UPDATE", "patients", 100, null, complexObj, "Notes");

        ArgumentCaptor<AuditTrail> captor = ArgumentCaptor.forClass(AuditTrail.class);
        verify(auditTrailRepository, times(1)).save(captor.capture());
        assertNotNull(captor.getValue().getNewValue());
    }

    @Test
    void logAction_TC07_NullStringAndNullObjects_CoversNullBranch() {
        Object result = ReflectionTestUtils.invokeMethod(auditTrailService, "stripPasswords", (String) null);
        assertNull(result);
    }

    @Test
    void logAction_TC08_AllBranchCombinations_Covers100PercentBranches() {
        // 1. Cover line 88: cleanSecurityAndEntitySubtrees(null)
        ReflectionTestUtils.invokeMethod(auditTrailService, "cleanSecurityAndEntitySubtrees", (com.fasterxml.jackson.databind.JsonNode) null);

        // 2. Cover line 99 registrationdetails and null entity nodes (hospNode == null, patNode == null, docNode == null, nutNode == null)
        com.fasterxml.jackson.databind.node.ObjectNode mockNode = mock(com.fasterxml.jackson.databind.node.ObjectNode.class);
        when(mockNode.isObject()).thenReturn(true);
        when(mockNode.fieldNames()).thenReturn(Arrays.asList("registrationdetails", "hospital", "patient", "doctor", "nutritionrule").iterator());
        when(mockNode.get("hospital")).thenReturn(null);
        when(mockNode.get("patient")).thenReturn(null);
        when(mockNode.get("doctor")).thenReturn(null);
        when(mockNode.get("nutritionrule")).thenReturn(null);

        ReflectionTestUtils.invokeMethod(auditTrailService, "cleanSecurityAndEntitySubtrees", mockNode);

        verify(mockNode, times(1)).put("registrationdetails", "***PROTECTED***");
    }

    @Test
    void logAction_TC09_ExhaustiveBranchCoverage() {
        ObjectMapper mapper = new ObjectMapper();

        // 1. Line 99: Test each OR branch individually
        com.fasterxml.jackson.databind.node.ObjectNode root99_1 = mapper.createObjectNode();
        root99_1.put("myPassword", "val1");
        ReflectionTestUtils.invokeMethod(auditTrailService, "cleanSecurityAndEntitySubtrees", root99_1);

        com.fasterxml.jackson.databind.node.ObjectNode root99_2 = mapper.createObjectNode();
        root99_2.put("mySecret", "val2");
        ReflectionTestUtils.invokeMethod(auditTrailService, "cleanSecurityAndEntitySubtrees", root99_2);

        com.fasterxml.jackson.databind.node.ObjectNode root99_3 = mapper.createObjectNode();
        root99_3.put("myToken", "val3");
        ReflectionTestUtils.invokeMethod(auditTrailService, "cleanSecurityAndEntitySubtrees", root99_3);

        com.fasterxml.jackson.databind.node.ObjectNode root99_4 = mapper.createObjectNode();
        root99_4.put("myRegistrationDetails", "val4");
        ReflectionTestUtils.invokeMethod(auditTrailService, "cleanSecurityAndEntitySubtrees", root99_4);

        // 2. Line 113 (patient): Test all 4 branch combinations
        com.fasterxml.jackson.databind.node.ObjectNode patNullNode = mock(com.fasterxml.jackson.databind.node.ObjectNode.class);
        when(patNullNode.isObject()).thenReturn(true);
        when(patNullNode.fieldNames()).thenReturn(Collections.singletonList("patient").iterator());
        when(patNullNode.get("patient")).thenReturn(null);
        ReflectionTestUtils.invokeMethod(auditTrailService, "cleanSecurityAndEntitySubtrees", patNullNode);

        com.fasterxml.jackson.databind.node.ObjectNode patNoIdNode = mapper.createObjectNode();
        patNoIdNode.set("patient", mapper.createObjectNode());
        ReflectionTestUtils.invokeMethod(auditTrailService, "cleanSecurityAndEntitySubtrees", patNoIdNode);

        com.fasterxml.jackson.databind.node.ObjectNode patNoNameNode = mapper.createObjectNode();
        com.fasterxml.jackson.databind.node.ObjectNode pat10 = mapper.createObjectNode();
        pat10.put("id", 10);
        patNoNameNode.set("patient", pat10);
        ReflectionTestUtils.invokeMethod(auditTrailService, "cleanSecurityAndEntitySubtrees", patNoNameNode);

        com.fasterxml.jackson.databind.node.ObjectNode patFullNode = mapper.createObjectNode();
        com.fasterxml.jackson.databind.node.ObjectNode pat10Full = mapper.createObjectNode();
        pat10Full.put("id", 10);
        pat10Full.put("fullName", "Test Patient");
        patFullNode.set("patient", pat10Full);
        ReflectionTestUtils.invokeMethod(auditTrailService, "cleanSecurityAndEntitySubtrees", patFullNode);

        // 3. Line 122 (doctor): Test all 4 branch combinations
        com.fasterxml.jackson.databind.node.ObjectNode docNullNode = mock(com.fasterxml.jackson.databind.node.ObjectNode.class);
        when(docNullNode.isObject()).thenReturn(true);
        when(docNullNode.fieldNames()).thenReturn(Collections.singletonList("doctor").iterator());
        when(docNullNode.get("doctor")).thenReturn(null);
        ReflectionTestUtils.invokeMethod(auditTrailService, "cleanSecurityAndEntitySubtrees", docNullNode);

        com.fasterxml.jackson.databind.node.ObjectNode docNoIdNode = mapper.createObjectNode();
        docNoIdNode.set("doctor", mapper.createObjectNode());
        ReflectionTestUtils.invokeMethod(auditTrailService, "cleanSecurityAndEntitySubtrees", docNoIdNode);

        com.fasterxml.jackson.databind.node.ObjectNode docNoNameNode = mapper.createObjectNode();
        com.fasterxml.jackson.databind.node.ObjectNode doc20 = mapper.createObjectNode();
        doc20.put("id", 20);
        docNoNameNode.set("doctor", doc20);
        ReflectionTestUtils.invokeMethod(auditTrailService, "cleanSecurityAndEntitySubtrees", docNoNameNode);

        com.fasterxml.jackson.databind.node.ObjectNode docFullNode = mapper.createObjectNode();
        com.fasterxml.jackson.databind.node.ObjectNode doc20Full = mapper.createObjectNode();
        doc20Full.put("id", 20);
        doc20Full.put("fullName", "Dr. House");
        docFullNode.set("doctor", doc20Full);
        ReflectionTestUtils.invokeMethod(auditTrailService, "cleanSecurityAndEntitySubtrees", docFullNode);
    }
}



