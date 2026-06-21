package com.rpm.remotepatientmonitoring;

import com.rpm.remotepatientmonitoring.model.*;
import com.rpm.remotepatientmonitoring.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class DatabaseQAVerificationTest {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private HealthLogRepository healthLogRepository;

    @Autowired
    private PatientMealRepository patientMealRepository;

    @Autowired
    private PatientExerciseRepository patientExerciseRepository;

    @Test
    void runFullSystemDataValidation() {
        System.out.println("==========================================================================");
        System.out.println("                QA VERIFICATION REPORT - SYSTEM DATA AUDIT                ");
        System.out.println("==========================================================================");

        List<String> errorsList = new ArrayList<>();

        // 1. Validate Doctor Quota
        List<Doctor> doctors = doctorRepository.findAll();
        System.out.println("[QA CHECK 1] Checking Doctor Patient Quotas...");
        for (Doctor doc : doctors) {
            long actualPatientsCount = patientRepository.findAll().stream()
                    .filter(p -> p.getDoctor() != null && p.getDoctor().getId().equals(doc.getId()))
                    .count();
            if (actualPatientsCount > doc.getCapacityLimit()) {
                errorsList.add(String.format("Bác sĩ '%s' (Mã: %s): Vượt quá hạn ngạch phân phối bệnh nhân (%d/%d)",
                        doc.getFullName(), doc.getDoctorCode(), actualPatientsCount, doc.getCapacityLimit()));
            }
        }

        // 2. Validate Daily Health Logs (Disease profiles & logging times & OCR)
        List<DailyHealthLog> healthLogs = healthLogRepository.findAll();
        System.out.println("[QA CHECK 2] Checking Health Logs Validation Rules...");
        for (DailyHealthLog log : healthLogs) {
            Patient patient = log.getPatient();
            if (patient == null) {
                errorsList.add(String.format("HealthLog ID %d: Không liên kết với bệnh nhân nào.", log.getId()));
                continue;
            }

            DiseaseProfile profile = patient.getDiseaseProfile();
            String profileName = profile != null ? profile.getProfileName() : "Không có bệnh lý nền";

            boolean hasHypertension = profileName.contains("Huyết áp") || profileName.contains("Đồng mắc");
            boolean hasDiabetes = profileName.contains("Tiểu đường") || profileName.contains("Đồng mắc");

            // Check dynamic display authorization rules
            if (!hasHypertension && (log.getSystolicBp() != null || log.getDiastolicBp() != null)) {
                errorsList.add(String.format("Bệnh nhân '%s' (Bệnh lý: %s, Log ID: %d): Sai phân quyền bệnh lý — Ghi nhận chỉ số Huyết áp không thuộc gói chỉ định.",
                        patient.getFullName(), profileName, log.getId()));
            }

            if (!hasDiabetes && log.getGlucoseLevel() != null) {
                errorsList.add(String.format("Bệnh nhân '%s' (Bệnh lý: %s, Log ID: %d): Sai phân quyền bệnh lý — Ghi nhận chỉ số Đường huyết không thuộc gói chỉ định.",
                        patient.getFullName(), profileName, log.getId()));
            }

            // Check logging time compliance rules
            String logType = log.getLogType();
            if (hasHypertension && (log.getSystolicBp() != null || log.getDiastolicBp() != null)) {
                if (!"MORNING".equalsIgnoreCase(logType) && !"EVENING".equalsIgnoreCase(logType) && !"RANDOM".equalsIgnoreCase(logType)) {
                    errorsList.add(String.format("Bệnh nhân '%s' (Log ID: %d): Sai mốc giờ đo Huyết áp — Mốc '%s' không tuân thủ quy định (Chỉ chấp nhận MORNING/EVENING/RANDOM).",
                            patient.getFullName(), log.getId(), logType));
                }
            }

            if (hasDiabetes && log.getGlucoseLevel() != null) {
                if (!"GLUCOSE1".equalsIgnoreCase(logType) && !"GLUCOSE2".equalsIgnoreCase(logType) && !"RANDOM".equalsIgnoreCase(logType)) {
                    errorsList.add(String.format("Bệnh nhân '%s' (Log ID: %d): Sai mốc giờ đo Đường huyết — Mốc '%s' không tuân thủ quy định (Chỉ chấp nhận GLUCOSE1/GLUCOSE2/RANDOM).",
                            patient.getFullName(), log.getId(), logType));
                }
            }

            // Check OCR confirmation rules
            if ("OCR".equalsIgnoreCase(log.getInputMethod())) {
                if (log.getIsOcrValidated() == null || !log.getIsOcrValidated()) {
                    errorsList.add(String.format("Bệnh nhân '%s' (Log ID: %d): Thiếu xác nhận OCR — Dữ liệu nhận dạng từ ảnh chụp chưa được xác minh hoặc kiểm tra thủ công.",
                            patient.getFullName(), log.getId()));
                }
            }
        }

        // Summary report
        System.out.println("==========================================================================");
        if (errorsList.isEmpty()) {
            System.out.println("XÁC NHẬN: Dữ liệu hệ thống hợp lệ 100%. Không tìm thấy lỗi nghiệp vụ nào.");
        } else {
            System.out.println("DANH SÁCH LỖI DỮ LIỆU ĐẦU VÀO PHÁT HIỆN:");
            for (int i = 0; i < errorsList.size(); i++) {
                System.out.println(String.format("%d. %s", i + 1, errorsList.get(i)));
            }
            System.out.println("--------------------------------------------------------------------------");
            System.out.println("GỢI Ý SỬA CHUẨN XÁC:");
            System.out.println("- Đảm bảo cập nhật cờ isOcrValidated = true khi lưu dữ liệu chụp ảnh OCR.");
            System.out.println("- Lọc đúng logType (MORNING/EVENING cho huyết áp, GLUCOSE1/GLUCOSE2 cho đường huyết).");
            System.out.println("- Kiểm tra việc phân phối số lượng bệnh nhân để không vượt quá hạn ngạch (capacityLimit) của bác sĩ.");
        }
        System.out.println("==========================================================================");
    }
}
