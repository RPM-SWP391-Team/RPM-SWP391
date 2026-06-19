package com.rpm.remotepatientmonitoring.service;

import com.rpm.remotepatientmonitoring.model.AlertThreshold;
import com.rpm.remotepatientmonitoring.model.EmergencyGuide;
import com.rpm.remotepatientmonitoring.model.Hospital;
import com.rpm.remotepatientmonitoring.repository.AlertThresholdRepository;
import com.rpm.remotepatientmonitoring.repository.EmergencyGuideRepository;
import com.rpm.remotepatientmonitoring.repository.HospitalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class HospitalConfigService {

    @Autowired
    private AlertThresholdRepository alertThresholdRepository;

    @Autowired
    private EmergencyGuideRepository emergencyGuideRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    public AlertThreshold getGlobalThreshold(Integer hospitalId) {
        return alertThresholdRepository.findByHospitalIdAndScope(hospitalId, "HOSPITAL")
                .orElseGet(() -> {
                    Hospital hospital = hospitalRepository.findById(hospitalId)
                            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bệnh viện với ID: " + hospitalId));

                    AlertThreshold defaultThreshold = AlertThreshold.builder()
                            .hospital(hospital)
                            .scope("HOSPITAL")
                            .metricType("COMBINED")
                            .glucoseNormalMin(BigDecimal.valueOf(70))
                            .glucoseNormalMax(BigDecimal.valueOf(99))
                            .glucoseWarningMin(BigDecimal.valueOf(100))
                            .glucoseWarningMax(BigDecimal.valueOf(125))
                            .glucoseTreatingMin(BigDecimal.valueOf(80))
                            .glucoseTreatingMax(BigDecimal.valueOf(130))
                            .glucoseDangerThreshold(BigDecimal.valueOf(130))
                            .systolicNormalMax(120)
                            .systolicPrehypertensionMin(120)
                            .systolicPrehypertensionMax(129)
                            .systolicHypertensionMin(130)
                            .systolicHypertensionMax(139)
                            .systolicDangerThreshold(140)
                            .systolicEmergencyThreshold(180)
                            .diastolicNormalMax(80)
                            .diastolicHypertensionMin(80)
                            .diastolicHypertensionMax(89)
                            .diastolicDangerThreshold(90)
                            .diastolicEmergencyThreshold(120)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();

                    return alertThresholdRepository.save(defaultThreshold);
                });
    }

    @Transactional
    public AlertThreshold updateGlobalThreshold(Integer hospitalId, AlertThreshold updated) {
        AlertThreshold existing = getGlobalThreshold(hospitalId);

        validateThresholds(updated);

        existing.setGlucoseNormalMin(updated.getGlucoseNormalMin());
        existing.setGlucoseNormalMax(updated.getGlucoseNormalMax());
        existing.setGlucoseWarningMin(updated.getGlucoseWarningMin());
        existing.setGlucoseWarningMax(updated.getGlucoseWarningMax());
        existing.setGlucoseTreatingMin(updated.getGlucoseTreatingMin());
        existing.setGlucoseTreatingMax(updated.getGlucoseTreatingMax());
        existing.setGlucoseDangerThreshold(updated.getGlucoseDangerThreshold());

        existing.setSystolicNormalMax(updated.getSystolicNormalMax());
        existing.setSystolicPrehypertensionMin(updated.getSystolicPrehypertensionMin());
        existing.setSystolicPrehypertensionMax(updated.getSystolicPrehypertensionMax());
        existing.setSystolicHypertensionMin(updated.getSystolicHypertensionMin());
        existing.setSystolicHypertensionMax(updated.getSystolicHypertensionMax());
        existing.setSystolicDangerThreshold(updated.getSystolicDangerThreshold());
        existing.setSystolicEmergencyThreshold(updated.getSystolicEmergencyThreshold());

        existing.setDiastolicNormalMax(updated.getDiastolicNormalMax());
        existing.setDiastolicHypertensionMin(updated.getDiastolicHypertensionMin());
        existing.setDiastolicHypertensionMax(updated.getDiastolicHypertensionMax());
        existing.setDiastolicDangerThreshold(updated.getDiastolicDangerThreshold());
        existing.setDiastolicEmergencyThreshold(updated.getDiastolicEmergencyThreshold());

        existing.setUpdatedAt(LocalDateTime.now());

        return alertThresholdRepository.save(existing);
    }

    private void validateThresholds(AlertThreshold t) {
        if (t.getGlucoseNormalMin() == null || t.getGlucoseNormalMax() == null ||
                t.getGlucoseNormalMin().compareTo(BigDecimal.ZERO) <= 0 ||
                t.getGlucoseNormalMax().compareTo(t.getGlucoseNormalMin()) <= 0) {
            throw new IllegalArgumentException("Ngưỡng đường huyết bình thường không hợp lệ (Cận trên phải lớn hơn cận dưới).");
        }

        if (t.getGlucoseWarningMin() == null || t.getGlucoseWarningMax() == null ||
                t.getGlucoseWarningMax().compareTo(t.getGlucoseWarningMin()) <= 0) {
            throw new IllegalArgumentException("Ngưỡng tiền tiểu đường không hợp lệ.");
        }

        if (t.getGlucoseTreatingMin() == null || t.getGlucoseTreatingMax() == null ||
                t.getGlucoseTreatingMax().compareTo(t.getGlucoseTreatingMin()) <= 0) {
            throw new IllegalArgumentException("Ngưỡng kiểm soát điều trị tiểu đường không hợp lệ.");
        }

        if (t.getGlucoseDangerThreshold() == null ||
                t.getGlucoseDangerThreshold().compareTo(t.getGlucoseTreatingMax()) <= 0) {
            throw new IllegalArgumentException("Ngưỡng nguy hiểm đường huyết bắt buộc phải lớn hơn mức điều trị tối đa (> 130 mg/dL).");
        }

        if (t.getSystolicNormalMax() == null || t.getSystolicNormalMax() <= 0) {
            throw new IllegalArgumentException("Huyết áp tâm thu bình thường phải là số nguyên dương.");
        }

        if (t.getSystolicPrehypertensionMin() == null || t.getSystolicPrehypertensionMax() == null ||
                !t.getSystolicPrehypertensionMin().equals(t.getSystolicNormalMax()) ||
                t.getSystolicPrehypertensionMax() <= t.getSystolicPrehypertensionMin()) {
            throw new IllegalArgumentException("Cận dưới tiền tăng huyết áp tâm thu phải trùng khít với mức tối đa bình thường (120 mmHg).");
        }

        if (t.getSystolicHypertensionMin() == null || t.getSystolicHypertensionMax() == null ||
                !t.getSystolicHypertensionMin().equals(t.getSystolicPrehypertensionMax() + 1) ||
                t.getSystolicHypertensionMax() <= t.getSystolicHypertensionMin()) {
            throw new IllegalArgumentException("Ngưỡng tăng huyết áp tâm thu phải tiếp nối liên tục từ mức tiền tăng huyết áp.");
        }

        if (t.getSystolicDangerThreshold() == null || t.getSystolicEmergencyThreshold() == null ||
                t.getSystolicDangerThreshold() <= t.getSystolicHypertensionMax() ||
                t.getSystolicEmergencyThreshold() <= t.getSystolicDangerThreshold()) {
            throw new IllegalArgumentException("Ngưỡng nguy hiểm và cấp cứu huyết áp tâm thu vi phạm tính tịnh tiến.");
        }

        if (t.getDiastolicNormalMax() == null || t.getDiastolicNormalMax() <= 0) {
            throw new IllegalArgumentException("Huyết áp tâm trương bình thường phải là số nguyên dương.");
        }

        if (t.getDiastolicHypertensionMin() == null || t.getDiastolicHypertensionMax() == null ||
                !t.getDiastolicHypertensionMin().equals(t.getDiastolicNormalMax()) ||
                t.getDiastolicHypertensionMax() <= t.getDiastolicHypertensionMin()) {
            throw new IllegalArgumentException("Ngưỡng tăng huyết áp tâm trương phải bắt đầu từ mức tối đa bình thường (80 mmHg).");
        }

        if (t.getDiastolicDangerThreshold() == null || t.getDiastolicEmergencyThreshold() == null ||
                t.getDiastolicDangerThreshold() <= t.getDiastolicHypertensionMax() ||
                t.getDiastolicEmergencyThreshold() <= t.getDiastolicDangerThreshold()) {
            throw new IllegalArgumentException("Ngưỡng nguy hiểm và cấp cứu huyết áp tâm trương không hợp lệ.");
        }
    }

    public List<EmergencyGuide> getEmergencyGuides(Integer hospitalId) {
        return emergencyGuideRepository.findByHospitalId(hospitalId);
    }

    @Transactional
    public EmergencyGuide addEmergencyGuide(Integer hospitalId, String alertLevel, String metricType, String title, String instructionContent) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bệnh viện với ID: " + hospitalId));

        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Tiêu đề hướng dẫn không được để trống.");
        }
        if (instructionContent == null || instructionContent.trim().isEmpty()) {
            throw new IllegalArgumentException("Nội dung hướng dẫn không được để trống.");
        }

        if (emergencyGuideRepository.existsByHospitalIdAndAlertLevelAndMetricTypeAndIsActiveTrue(hospitalId, alertLevel, metricType)) {
            throw new IllegalArgumentException("Mức cảnh báo '" + alertLevel + "' của chỉ số '" + metricType + "' đã có hướng dẫn xử lý khẩn cấp. Vui lòng chỉnh sửa bài cũ thay vì tạo mới.");
        }

        EmergencyGuide guide = EmergencyGuide.builder()
                .hospital(hospital)
                .alertLevel(alertLevel)
                .metricType(metricType)
                .title(title.trim())
                .instructionContent(instructionContent.trim())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return emergencyGuideRepository.save(guide);
    }

    @Transactional
    public EmergencyGuide editEmergencyGuideContent(Integer guideId, String instructionContent) {
        EmergencyGuide guide = emergencyGuideRepository.findById(guideId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hướng dẫn với ID: " + guideId));

        if (instructionContent == null || instructionContent.trim().isEmpty()) {
            throw new IllegalArgumentException("Nội dung hướng dẫn không được để trống.");
        }

        guide.setInstructionContent(instructionContent.trim());
        guide.setUpdatedAt(LocalDateTime.now());
        return emergencyGuideRepository.save(guide);
    }
}