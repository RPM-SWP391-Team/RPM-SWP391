package com.rpm.remotepatientmonitoring.service.doctor;

import com.rpm.remotepatientmonitoring.dto.doctor.PatientSearchResponseDTO;
import com.rpm.remotepatientmonitoring.model.DiseaseProfile;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DoctorPatientService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 1. Chức năng tìm kiếm bệnh nhân chờ tiếp nhận (lọc theo bệnh viện của bác sĩ)
    public List<PatientSearchResponseDTO> searchUnassignedPatients(String keyword, Integer hospitalId) {
        List<Patient> patients = patientRepository.searchUnassignedPatients(hospitalId, keyword);
        List<PatientSearchResponseDTO> resultList = new ArrayList<>();

        for (Patient p : patients) {
            PatientSearchResponseDTO dto = new PatientSearchResponseDTO();
            dto.setId(p.getId());
            dto.setFullName(p.getFullName());
            dto.setPhone(p.getPhone());
            dto.setStatus(p.getStatus());
            resultList.add(dto);
        }
        return resultList;
    }

    // 2. Chức năng tiếp nhận bệnh nhân
    public String assignPatient(Integer patientId, Integer doctorId, Integer diseaseProfileId) {
        // Gọi Stored Procedure để gán bác sĩ và kiểm tra số lượng tải bệnh nhân
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_assign_patient_to_doctor");

        MapSqlParameterSource inParams = new MapSqlParameterSource();
        inParams.addValue("patient_id", patientId);
        inParams.addValue("doctor_id", doctorId);
        inParams.addValue("actor_id", doctorId);
        inParams.addValue("actor_type", "DOCTOR");

        Map<String, Object> out = jdbcCall.execute(inParams);
        String resultMessage = (String) out.get("result_message");

        // Nếu Procedure thông báo thành công, ta cập nhật thêm loại bệnh lý vào hồ sơ
        if (resultMessage != null && resultMessage.startsWith("Thành công")) {
            Patient patient = patientRepository.findById(patientId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bệnh nhân"));

            DiseaseProfile dp = new DiseaseProfile();
            dp.setId(diseaseProfileId);
            patient.setDiseaseProfile(dp);

            patientRepository.save(patient);
        }

        return resultMessage;
    }
}
