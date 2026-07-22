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
    private com.rpm.remotepatientmonitoring.repository.DoctorRepository doctorRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AuditTrailService auditTrailService;

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

            if (auditTrailService != null) {
                try {
                    com.rpm.remotepatientmonitoring.model.Doctor doc = doctorRepository.findById(doctorId).orElse(null);
                    String docName = (doc != null) ? doc.getFullName() : ("Bác sĩ ID " + doctorId);

                    String profileName = "Tăng Huyết Áp";
                    if (Integer.valueOf(2).equals(diseaseProfileId)) profileName = "Đái Tháo Đường Type 2";
                    if (Integer.valueOf(3).equals(diseaseProfileId)) profileName = "Đồng Mắc (Tăng HA & ĐTĐ T2)";

                    java.util.Map<String, Object> pLog = new java.util.HashMap<>();
                    pLog.put("doctorId", doctorId);
                    pLog.put("doctorName", docName);
                    pLog.put("patientId", patient.getId());
                    pLog.put("patientCode", patient.getPatientCode());
                    pLog.put("patientName", patient.getFullName());
                    pLog.put("diseaseProfileId", diseaseProfileId);
                    pLog.put("diseaseProfileName", profileName);
                    pLog.put("status", patient.getStatus());

                    String notes = "Bác sĩ " + docName + " tiếp nhận quản lý bệnh nhân " + patient.getFullName() + " (Gói bệnh lý: " + profileName + ")";

                    auditTrailService.logAction(
                            "DOCTOR",
                            doctorId,
                            "ACCEPT_PATIENT",
                            "patients",
                            patient.getId(),
                            null,
                            pLog,
                            notes
                    );
                } catch (Exception e) {
                    // Log fail silent
                }
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
