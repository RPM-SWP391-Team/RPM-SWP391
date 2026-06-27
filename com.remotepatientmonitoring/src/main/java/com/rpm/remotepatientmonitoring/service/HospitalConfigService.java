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
                            .glucoseHypoThreshold(BigDecimal.valueOf(4.4))
                            .glucoseNormalMax(BigDecimal.valueOf(10.0))
                            .glucoseHighMax(BigDecimal.valueOf(16.0))
                            .systolicNormalMax(120)
                            .systolicWarningMin(130)
                            .systolicWarningMax(139)
                            .systolicDangerMin(140)
                            .systolicDangerMax(179)
                            .systolicEmergencyThreshold(180)
                            .diastolicNormalMax(80)
                            .diastolicWarningMin(85)
                            .diastolicWarningMax(89)
                            .diastolicDangerMin(90)
                            .diastolicDangerMax(109)
                            .diastolicEmergencyThreshold(110)
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

        existing.setGlucoseHypoThreshold(updated.getGlucoseHypoThreshold());
        existing.setGlucoseNormalMax(updated.getGlucoseNormalMax());
        existing.setGlucoseHighMax(updated.getGlucoseHighMax());

        existing.setSystolicNormalMax(updated.getSystolicNormalMax());
        existing.setSystolicWarningMin(updated.getSystolicWarningMin());
        existing.setSystolicWarningMax(updated.getSystolicWarningMax());
        existing.setSystolicDangerMin(updated.getSystolicDangerMin());
        existing.setSystolicDangerMax(updated.getSystolicDangerMax());
        existing.setSystolicEmergencyThreshold(updated.getSystolicEmergencyThreshold());

        existing.setDiastolicNormalMax(updated.getDiastolicNormalMax());
        existing.setDiastolicWarningMin(updated.getDiastolicWarningMin());
        existing.setDiastolicWarningMax(updated.getDiastolicWarningMax());
        existing.setDiastolicDangerMin(updated.getDiastolicDangerMin());
        existing.setDiastolicDangerMax(updated.getDiastolicDangerMax());
        existing.setDiastolicEmergencyThreshold(updated.getDiastolicEmergencyThreshold());

        existing.setUpdatedAt(LocalDateTime.now());

        return alertThresholdRepository.save(existing);
    }

    private void validateThresholds(AlertThreshold t) {
        if (t.getGlucoseHypoThreshold() == null || t.getGlucoseNormalMax() == null || t.getGlucoseHighMax() == null) {
            throw new IllegalArgumentException("Các chỉ số đường huyết bắt buộc phải có.");
        }
        if (t.getSystolicNormalMax() == null || t.getSystolicWarningMin() == null || t.getSystolicWarningMax() == null) {
            throw new IllegalArgumentException("Các chỉ số huyết áp tâm thu bắt buộc phải có.");
        }
        if (t.getDiastolicNormalMax() == null || t.getDiastolicWarningMin() == null || t.getDiastolicWarningMax() == null) {
            throw new IllegalArgumentException("Các chỉ số huyết áp tâm trương bắt buộc phải có.");
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