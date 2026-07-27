package com.rpm.remotepatientmonitoring.service.doctor;

import com.rpm.remotepatientmonitoring.dto.doctor.PatientSearchResponseDTO;
import com.rpm.remotepatientmonitoring.model.Doctor;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.repository.DoctorRepository;
import com.rpm.remotepatientmonitoring.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DoctorPatientServiceTest {


    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private AuditTrailService auditTrailService;

    @InjectMocks
    private DoctorPatientService doctorPatientService;

    @Test
    void searchUnassignedPatients_TC01_ReturnsMappedList() {
        Patient p1 = new Patient(); p1.setId(1); p1.setFullName("Nguyen A"); p1.setStatus("WAITING");
        when(patientRepository.searchUnassignedPatients(1, "Ng")).thenReturn(Collections.singletonList(p1));

        List<PatientSearchResponseDTO> result = doctorPatientService.searchUnassignedPatients("Ng", 1);

        assertEquals(1, result.size());
        assertEquals("Nguyen A", result.get(0).getFullName());
    }

    @Test
    void searchUnassignedPatients_TC02_ReturnsEmptyList() {
        when(patientRepository.searchUnassignedPatients(1, "Null")).thenReturn(Collections.emptyList());
        List<PatientSearchResponseDTO> result = doctorPatientService.searchUnassignedPatients("Null", 1);
        assertTrue(result.isEmpty());
    }

    @Test
    void savePatientWithGeneratedCode_TC01_SuccessOnFirstTry() {
        Patient p = new Patient();
        // Giả lập ko trùng mã
        when(patientRepository.findAllByPatientCodeIsNotNull()).thenReturn(Collections.emptyList());
        when(patientRepository.saveAndFlush(p)).thenReturn(p);

        ReflectionTestUtils.invokeMethod(doctorPatientService, "savePatientWithGeneratedCode", p);

        assertEquals("PAT001", p.getPatientCode());
        verify(patientRepository, times(1)).saveAndFlush(p);
    }

    @Test
    void savePatientWithGeneratedCode_TC02_RetriesOnDataIntegrityViolation() {
        Patient p = new Patient();
        
        when(patientRepository.findAllByPatientCodeIsNotNull()).thenReturn(Collections.emptyList());
        // Lần đầu văng lỗi trùng lặp (DataIntegrityViolationException), Lần hai thành công
        when(patientRepository.saveAndFlush(p))
                .thenThrow(new DataIntegrityViolationException("Duplicated code"))
                .thenReturn(p);

        ReflectionTestUtils.invokeMethod(doctorPatientService, "savePatientWithGeneratedCode", p);

        // Vẫn tạo thành công sau khi lặp
        verify(patientRepository, times(2)).saveAndFlush(p);
    }

    @Test
    void savePatientWithGeneratedCode_TC03_ThrowsExceptionAfterMaxRetries() {
        Patient p = new Patient();
        
        when(patientRepository.findAllByPatientCodeIsNotNull()).thenReturn(Collections.emptyList());
        // Lỗi liên tục 5 lần
        when(patientRepository.saveAndFlush(p)).thenThrow(new DataIntegrityViolationException("Duplicated code"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            ReflectionTestUtils.invokeMethod(doctorPatientService, "savePatientWithGeneratedCode", p);
        });

        assertTrue(exception.getMessage().contains("Không thể sinh mã bệnh nhân"));
        verify(patientRepository, times(5)).saveAndFlush(p);
    }

    @Test
    void generateNextPatientCode_TC04_ValidFormatWithLegacyData() {
        Patient p1 = new Patient(); p1.setPatientCode("PAT005");
        Patient p2 = new Patient(); p2.setPatientCode("INVALID_CODE"); // Legacy data
        Patient p3 = new Patient(); p3.setPatientCode("PAT012");
        when(patientRepository.findAllByPatientCodeIsNotNull()).thenReturn(Arrays.asList(p1, p2, p3));
        
        String nextCode = ReflectionTestUtils.invokeMethod(doctorPatientService, "generateNextPatientCode");
        
        assertEquals("PAT013", nextCode);
    }

    @Test
    void assignPatient_TC01_DoctorCapacityLimitReached_ThrowsException() {
        Doctor doc = new Doctor();
        doc.setId(10);
        doc.setCurrentPatientCount(30);
        doc.setCapacityLimit(30);
        when(doctorRepository.findById(10)).thenReturn(Optional.of(doc));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            doctorPatientService.assignPatient(100, 10, 1);
        });

        assertTrue(ex.getMessage().contains("Bác sĩ đã đạt giới hạn tối đa"));
    }

    private void mockSimpleJdbcCall() throws Exception {
        mockSimpleJdbcCall("Thành công tiếp nhận bệnh nhân");
    }

    private void mockSimpleJdbcCall(String spResultMsg) throws Exception {
        javax.sql.DataSource dataSource = mock(javax.sql.DataSource.class);
        java.sql.Connection conn = mock(java.sql.Connection.class);
        java.sql.DatabaseMetaData metaData = mock(java.sql.DatabaseMetaData.class);

        when(jdbcTemplate.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.getMetaData()).thenReturn(metaData);
        when(metaData.getDatabaseProductName()).thenReturn("MySQL");
        when(metaData.getUserName()).thenReturn("root");

        java.sql.ResultSet procRs = mock(java.sql.ResultSet.class);
        when(procRs.next()).thenReturn(true, false);
        when(procRs.getString(3)).thenReturn("sp_assign_patient_to_doctor");
        when(procRs.getString("PROCEDURE_NAME")).thenReturn("sp_assign_patient_to_doctor");

        java.sql.ResultSet colRs = mock(java.sql.ResultSet.class);
        when(colRs.next()).thenReturn(true, true, true, true, true, false);
        when(colRs.getString(4)).thenReturn("patient_id", "doctor_id", "actor_id", "actor_type", "result_message");
        when(colRs.getString("COLUMN_NAME")).thenReturn("patient_id", "doctor_id", "actor_id", "actor_type", "result_message");
        when(colRs.getInt(5)).thenReturn(1, 1, 1, 1, 4);
        when(colRs.getInt("COLUMN_TYPE")).thenReturn(1, 1, 1, 1, 4);
        when(colRs.getInt(6)).thenReturn(java.sql.Types.INTEGER, java.sql.Types.INTEGER, java.sql.Types.INTEGER, java.sql.Types.VARCHAR, java.sql.Types.VARCHAR);
        when(colRs.getInt("DATA_TYPE")).thenReturn(java.sql.Types.INTEGER, java.sql.Types.INTEGER, java.sql.Types.INTEGER, java.sql.Types.VARCHAR, java.sql.Types.VARCHAR);

        java.sql.ResultSet dummyRs = mock(java.sql.ResultSet.class);

        when(metaData.getProcedures(any(), any(), any())).thenReturn(procRs);
        when(metaData.getProcedureColumns(any(), any(), any(), any())).thenReturn(colRs);
        when(metaData.getFunctions(any(), any(), any())).thenReturn(dummyRs);
        when(metaData.getFunctionColumns(any(), any(), any(), any())).thenReturn(dummyRs);

        Map<String, Object> spResultMap = new HashMap<>();
        if (spResultMsg != null) {
            spResultMap.put("result_message", spResultMsg);
        }
        when(jdbcTemplate.call(any(), any())).thenReturn(spResultMap);
    }








    @Test
    void assignPatient_TC02_Success_WithExistingPatientCode() throws Exception {
        Doctor doc = new Doctor();
        doc.setId(10);
        doc.setFullName("Dr. Tran");
        doc.setCurrentPatientCount(5);
        doc.setCapacityLimit(30);
        when(doctorRepository.findById(10)).thenReturn(Optional.of(doc));

        mockSimpleJdbcCall();

        Patient p = new Patient();
        p.setId(100);
        p.setFullName("Patient B");
        p.setPatientCode("PAT005");
        when(patientRepository.findById(100)).thenReturn(Optional.of(p));

        String result = doctorPatientService.assignPatient(100, 10, 2);

        assertNotNull(result);
        assertEquals(2, p.getDiseaseProfile().getId());
        verify(patientRepository, times(1)).save(p);
        verify(auditTrailService, times(1)).logAction(eq("DOCTOR"), eq(10), eq("ACCEPT_PATIENT"), eq("patients"), eq(100), any(), any(), any());
    }

    @Test
    void assignPatient_TC03_Success_WithNewPatientCode_DiseaseProfile3() throws Exception {
        Doctor doc = new Doctor();
        doc.setId(10);
        doc.setFullName("Dr. Tran");
        doc.setCurrentPatientCount(5);
        doc.setCapacityLimit(30);
        when(doctorRepository.findById(10)).thenReturn(Optional.of(doc));

        mockSimpleJdbcCall();

        Patient p = new Patient();
        p.setId(100);
        p.setFullName("Patient C");
        p.setPatientCode(null); // Code null -> sinh mã mới
        when(patientRepository.findById(100)).thenReturn(Optional.of(p));
        when(patientRepository.findAllByPatientCodeIsNotNull()).thenReturn(Collections.emptyList());
        when(patientRepository.saveAndFlush(p)).thenReturn(p);

        String result = doctorPatientService.assignPatient(100, 10, 3);

        assertNotNull(result);
        assertEquals(3, p.getDiseaseProfile().getId());
        assertEquals("PAT001", p.getPatientCode());
        verify(patientRepository, times(1)).saveAndFlush(p);
    }

    @Test
    void assignPatient_TC04_Success_DiseaseProfile1_AuditTrailExceptionHandled() throws Exception {
        Doctor doc = new Doctor();
        doc.setId(10);
        doc.setCurrentPatientCount(5);
        doc.setCapacityLimit(30);
        when(doctorRepository.findById(10)).thenReturn(Optional.of(doc)).thenReturn(Optional.empty()); // Lần 2 return empty cho audit log

        mockSimpleJdbcCall();

        Patient p = new Patient();
        p.setId(100);
        p.setFullName("Patient D");
        p.setPatientCode("PAT010");
        when(patientRepository.findById(100)).thenReturn(Optional.of(p));

        doThrow(new RuntimeException("Audit log failed")).when(auditTrailService).logAction(any(), any(), any(), any(), any(), any(), any(), any());

        String result = doctorPatientService.assignPatient(100, 10, 1);

        assertNotNull(result);
        assertEquals(1, p.getDiseaseProfile().getId());
    }

    @Test
    void assignPatient_TC05_PatientNotFound_ThrowsException() throws Exception {
        Doctor doc = new Doctor();
        doc.setId(10);
        doc.setCurrentPatientCount(5);
        doc.setCapacityLimit(30);
        when(doctorRepository.findById(10)).thenReturn(Optional.of(doc));

        mockSimpleJdbcCall();

        when(patientRepository.findById(100)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            doctorPatientService.assignPatient(100, 10, 1);
        });

        assertEquals("Không tìm thấy bệnh nhân", ex.getMessage());
    }

    @Test
    void assignPatient_TC06_DoctorNotFound_AllowsAssignment() throws Exception {
        when(doctorRepository.findById(10)).thenReturn(Optional.empty());

        mockSimpleJdbcCall();

        Patient p = new Patient();
        p.setId(100);
        p.setPatientCode("PAT001");
        when(patientRepository.findById(100)).thenReturn(Optional.of(p));

        String result = doctorPatientService.assignPatient(100, 10, 4);

        assertNotNull(result);
        verify(patientRepository, times(1)).save(p);
    }

    @Test
    void assignPatient_TC07_NullSPResult_NullAuditTrailService() throws Exception {
        Doctor doc = new Doctor();
        doc.setId(10);
        doc.setCurrentPatientCount(5);
        doc.setCapacityLimit(30);
        when(doctorRepository.findById(10)).thenReturn(Optional.of(doc));

        mockSimpleJdbcCall(null);

        Patient p = new Patient();
        p.setId(100);
        p.setPatientCode("PAT001");
        when(patientRepository.findById(100)).thenReturn(Optional.of(p));

        ReflectionTestUtils.setField(doctorPatientService, "auditTrailService", null);

        String result = doctorPatientService.assignPatient(100, 10, 1);

        assertEquals("Thành công tiếp nhận bệnh nhân", result);
    }

    @Test
    void assignPatient_TC08_BlankPatientCode() throws Exception {
        Doctor doc = new Doctor();
        doc.setId(10);
        doc.setCurrentPatientCount(5);
        doc.setCapacityLimit(30);
        when(doctorRepository.findById(10)).thenReturn(Optional.of(doc));

        mockSimpleJdbcCall();

        Patient p = new Patient();
        p.setId(100);
        p.setPatientCode("   ");
        when(patientRepository.findById(100)).thenReturn(Optional.of(p));
        when(patientRepository.findAllByPatientCodeIsNotNull()).thenReturn(Collections.emptyList());
        when(patientRepository.saveAndFlush(p)).thenReturn(p);

        String successResult = doctorPatientService.assignPatient(100, 10, 1);
        assertNotNull(successResult);
        assertEquals("PAT001", p.getPatientCode());
    }

    @Test
    void assignPatient_TC09_UnsuccessfulSPResult() throws Exception {
        Doctor doc = new Doctor();
        doc.setId(10);
        doc.setCurrentPatientCount(5);
        doc.setCapacityLimit(30);
        when(doctorRepository.findById(10)).thenReturn(Optional.of(doc));

        mockSimpleJdbcCall("Lỗi: Đã thuộc bác sĩ khác");

        String errorResult = doctorPatientService.assignPatient(100, 10, 1);
        assertEquals("Lỗi: Đã thuộc bác sĩ khác", errorResult);
    }


    @Test
    void generateNextPatientCode_TC05_NullCodeAndSmallerNumber() {
        Patient p1 = new Patient(); p1.setPatientCode(null);
        Patient p2 = new Patient(); p2.setPatientCode("PAT010");
        Patient p3 = new Patient(); p3.setPatientCode("PAT003");
        when(patientRepository.findAllByPatientCodeIsNotNull()).thenReturn(Arrays.asList(p1, p2, p3));

        String nextCode = ReflectionTestUtils.invokeMethod(doctorPatientService, "generateNextPatientCode");

        assertEquals("PAT011", nextCode);
    }
}