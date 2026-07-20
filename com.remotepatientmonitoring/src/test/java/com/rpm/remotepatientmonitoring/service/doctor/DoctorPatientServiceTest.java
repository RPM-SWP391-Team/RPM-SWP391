package com.rpm.remotepatientmonitoring.service.doctor;

import com.rpm.remotepatientmonitoring.dto.doctor.PatientSearchResponseDTO;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Chuoi
 */
@ExtendWith(MockitoExtension.class)
class DoctorPatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private DoctorPatientService doctorPatientService;

    @Test
    public void searchUnassignedPatients_TC01_ReturnsMappedList() {
        String keyword = "Nguyen";
        Integer hospitalId = 1;

        Patient p1 = new Patient();
        p1.setId(100);
        p1.setFullName("Nguyen Van A");
        p1.setPhone("0901234567");
        p1.setStatus("WAITING");

        Patient p2 = new Patient();
        p2.setId(101);
        p2.setFullName("Nguyen Thi B");
        p2.setPhone("0987654321");
        p2.setStatus("WAITING");

        when(patientRepository.searchUnassignedPatients(hospitalId, keyword))
                .thenReturn(Arrays.asList(p1, p2));

        List<PatientSearchResponseDTO> actualList = doctorPatientService.searchUnassignedPatients(keyword, hospitalId);

        assertNotNull(actualList);
        assertEquals(2, actualList.size(), "Danh sách trả về phải có 2 phần tử");
        assertEquals(100, actualList.get(0).getId());
        assertEquals("Nguyen Van A", actualList.get(0).getFullName());

        verify(patientRepository, times(1)).searchUnassignedPatients(hospitalId, keyword);
    }

    @Test
    public void searchUnassignedPatients_TC02_ReturnsEmptyList() {
        String keyword = "KhongTonTai";
        Integer hospitalId = 1;

        when(patientRepository.searchUnassignedPatients(hospitalId, keyword))
                .thenReturn(Collections.emptyList());

        List<PatientSearchResponseDTO> actualList = doctorPatientService.searchUnassignedPatients(keyword, hospitalId);

        assertTrue(actualList.isEmpty(), "Danh sách trả về phải là danh sách rỗng");
    }

    @Test
    public void searchUnassignedPatients_TC03_NullKeyword() {
        Integer hospitalId = 1;
        Patient p1 = new Patient();
        p1.setId(50);
        p1.setFullName("Le Van C");

        when(patientRepository.searchUnassignedPatients(hospitalId, null))
                .thenReturn(Collections.singletonList(p1));

        List<PatientSearchResponseDTO> actualList = doctorPatientService.searchUnassignedPatients(null, hospitalId);

        assertEquals(1, actualList.size());
        assertEquals("Le Van C", actualList.get(0).getFullName());
    }

    @Test
    public void searchUnassignedPatients_TC05_PatientWithNullFields() {
        String keyword = "LoiData";
        Integer hospitalId = 1;

        Patient p1 = new Patient();
        p1.setId(99);
        p1.setFullName(null);
        p1.setPhone(null);

        when(patientRepository.searchUnassignedPatients(hospitalId, keyword))
                .thenReturn(Collections.singletonList(p1));

        List<PatientSearchResponseDTO> actualList = doctorPatientService.searchUnassignedPatients(keyword, hospitalId);

        assertEquals(1, actualList.size());
        assertEquals(99, actualList.get(0).getId());
        assertNull(actualList.get(0).getFullName());
        assertNull(actualList.get(0).getPhone());
    }
}