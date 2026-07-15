package com.rpm.remotepatientmonitoring.service.doctor;

import com.rpm.remotepatientmonitoring.dto.doctor.PatientSearchResponseDTO;
import com.rpm.remotepatientmonitoring.model.DiseaseProfile;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DoctorPatientService {

    private static final int MAX_CODE_GENERATION_RETRIES = 5;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 1. Tìm kiếm bệnh nhân chờ tiếp nhận (lọc theo bệnh viện của bác sĩ)
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

    // 2. Tiếp nhận bệnh nhân qua Stored Procedure + sinh mã bệnh nhân nếu chưa có
    @Transactional
    public String assignPatient(Integer patientId, Integer doctorId, Integer diseaseProfileId) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_assign_patient_to_doctor");

        MapSqlParameterSource inParams = new MapSqlParameterSource();
        inParams.addValue("patient_id", patientId);
        inParams.addValue("doctor_id", doctorId);
        inParams.addValue("actor_id", doctorId);
        inParams.addValue("actor_type", "DOCTOR");

        Map<String, Object> out = jdbcCall.execute(inParams);
        String resultMessage = (String) out.get("result_message");

        if (resultMessage != null && resultMessage.startsWith("Thành công")) {
            Patient patient = patientRepository.findById(patientId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bệnh nhân"));

            DiseaseProfile dp = new DiseaseProfile();
            dp.setId(diseaseProfileId);
            patient.setDiseaseProfile(dp);

            if (patient.getPatientCode() == null || patient.getPatientCode().trim().isEmpty()) {
                savePatientWithGeneratedCode(patient);
            } else {
                patientRepository.save(patient);
            }
        }

        return resultMessage;
    }

    // Sinh mã bệnh nhân mới và lưu, tự retry nếu trùng do race condition hiếm gặp
    private void savePatientWithGeneratedCode(Patient patient) {
        for (int attempt = 0; attempt < MAX_CODE_GENERATION_RETRIES; attempt++) {
            patient.setPatientCode(generateNextPatientCode());
            try {
                patientRepository.saveAndFlush(patient);
                return;
            } catch (DataIntegrityViolationException e) {
                // Mã bị trùng do có bác sĩ khác assign gần như cùng lúc -> thử lại với số kế tiếp
            }
        }
        throw new RuntimeException("Không thể sinh mã bệnh nhân duy nhất sau " + MAX_CODE_GENERATION_RETRIES + " lần thử. Vui lòng thử lại.");
    }

    private String generateNextPatientCode() {
        List<Patient> patientsWithCode = patientRepository.findAllByPatientCodeIsNotNull();
        int maxNumber = 0;
        for (Patient p : patientsWithCode) {
            String code = p.getPatientCode();
            if (code != null && code.matches("^PAT\\d+$")) {
                try {
                    int num = Integer.parseInt(code.substring(3));
                    if (num > maxNumber) {
                        maxNumber = num;
                    }
                } catch (NumberFormatException ignored) {
                    // Mã không đúng định dạng (dữ liệu legacy) -> bỏ qua, không tính vào max
                }
            }
        }
        return String.format("PAT%03d", maxNumber + 1);
    }
}
