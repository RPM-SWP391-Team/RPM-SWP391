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

@ExtendWith(MockitoExtension.class)
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
}