package com.rpm.remotepatientmonitoring.controller.doctor;

import com.rpm.remotepatientmonitoring.dto.doctor.AssignPatientRequestDTO;
import com.rpm.remotepatientmonitoring.dto.doctor.PatientSearchResponseDTO;
import com.rpm.remotepatientmonitoring.service.doctor.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctor")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    // API 1: Tìm kiếm bệnh nhân
    // Ví dụ gọi: GET http://localhost:8080/api/doctor/patients/search?keyword=09
    @GetMapping("/patients/search")
    public ResponseEntity<List<PatientSearchResponseDTO>> searchPatients(@RequestParam String keyword) {
        List<PatientSearchResponseDTO> patients = doctorService.searchUnassignedPatients(keyword);
        return ResponseEntity.ok(patients);
    }

    // API 2: Gán bệnh nhân vào bác sĩ
    // Ví dụ gọi: POST http://localhost:8080/api/doctor/patients/assign
    @PostMapping("/patients/assign")
    public ResponseEntity<String> assignPatient(@RequestBody AssignPatientRequestDTO request) {
        String message = doctorService.assignPatient(
                request.getPatientId(),
                request.getDoctorId(),
                request.getDiseaseProfileId()
        );
        return ResponseEntity.ok(message);
    }
}