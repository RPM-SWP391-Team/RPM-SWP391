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
                    // Create default thresholds if not found (Null Pointer security)
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

        // Apply strict hospital business validation rules
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
        // 1. Glucose validations
        if (t.getGlucoseNormalMin() == null || t.getGlucoseNormalMax() == null ||
            t.getGlucoseNormalMin().compareTo(BigDecimal.ZERO) <= 0 ||
            t.getGlucoseNormalMax().compareTo(t.getGlucoseNormalMin()) <= 0) {
            throw new IllegalArgumentException("Ngưỡng đường huyết bình thường không hợp lệ.");
        }
        if (t.getGlucoseWarningMin() == null || t.getGlucoseWarningMax() == null ||
            t.getGlucoseWarningMin().compareTo(t.getGlucoseNormalMax()) < 0 ||
            t.getGlucoseWarningMax().compareTo(t.getGlucoseWarningMin()) <= 0) {
            throw new IllegalArgumentException("Ngưỡng tiền tiểu đường không hợp lệ.");
        }
        if (t.getGlucoseTreatingMin() == null || t.getGlucoseTreatingMax() == null ||
            t.getGlucoseTreatingMax().compareTo(t.getGlucoseTreatingMin()) <= 0) {
            throw new IllegalArgumentException("Ngưỡng kiểm soát điều trị không hợp lệ.");
        }
        if (t.getGlucoseDangerThreshold() == null || t.getGlucoseDangerThreshold().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Ngưỡng nguy hiểm đường huyết phải lớn hơn 0.");
        }

        // 2. Systolic Blood Pressure validations
        if (t.getSystolicNormalMax() == null || t.getSystolicNormalMax() <= 0) {
            throw new IllegalArgumentException("Huyết áp tâm thu bình thường không hợp lệ.");
        }
        if (t.getSystolicPrehypertensionMin() == null || t.getSystolicPrehypertensionMax() == null ||
            t.getSystolicPrehypertensionMin() < t.getSystolicNormalMax() ||
            t.getSystolicPrehypertensionMax() < t.getSystolicPrehypertensionMin()) {
            throw new IllegalArgumentException("Ngưỡng tiền tăng huyết áp tâm thu không hợp lệ.");
        }
        if (t.getSystolicHypertensionMin() == null || t.getSystolicHypertensionMax() == null ||
            t.getSystolicHypertensionMin() < t.getSystolicPrehypertensionMax() ||
            t.getSystolicHypertensionMax() < t.getSystolicHypertensionMin()) {
            throw new IllegalArgumentException("Ngưỡng tăng huyết áp tâm thu không hợp lệ.");
        }
        if (t.getSystolicDangerThreshold() == null || t.getSystolicEmergencyThreshold() == null ||
            t.getSystolicDangerThreshold() < t.getSystolicHypertensionMax() ||
            t.getSystolicEmergencyThreshold() < t.getSystolicDangerThreshold()) {
            throw new IllegalArgumentException("Ngưỡng nguy kịch/cấp cứu huyết áp tâm thu không hợp lệ.");
        }

        // 3. Diastolic Blood Pressure validations
        if (t.getDiastolicNormalMax() == null || t.getDiastolicNormalMax() <= 0) {
            throw new IllegalArgumentException("Huyết áp tâm trương bình thường không hợp lệ.");
        }
        if (t.getDiastolicHypertensionMin() == null || t.getDiastolicHypertensionMax() == null ||
            t.getDiastolicHypertensionMin() < t.getDiastolicNormalMax() ||
            t.getDiastolicHypertensionMax() < t.getDiastolicHypertensionMin()) {
            throw new IllegalArgumentException("Ngưỡng tăng huyết áp tâm trương không hợp lệ.");
        }
        if (t.getDiastolicDangerThreshold() == null || t.getDiastolicEmergencyThreshold() == null ||
            t.getDiastolicDangerThreshold() < t.getDiastolicHypertensionMax() ||
            t.getDiastolicEmergencyThreshold() < t.getDiastolicDangerThreshold()) {
            throw new IllegalArgumentException("Ngưỡng nguy kịch/cấp cứu huyết áp tâm trương không hợp lệ.");
        }
    }

    // Emergency Guide Management
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

        EmergencyGuide guide = EmergencyGuide.builder()
                .hospital(hospital)
                .alertLevel(alertLevel)
                .metricType(metricType)
                .title(title)
                .instructionContent(instructionContent)
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

        guide.setInstructionContent(instructionContent);
        guide.setUpdatedAt(LocalDateTime.now());
        return emergencyGuideRepository.save(guide);
    }
}
