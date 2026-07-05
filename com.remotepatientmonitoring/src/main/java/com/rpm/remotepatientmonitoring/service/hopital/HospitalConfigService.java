package com.rpm.remotepatientmonitoring.service.hopital;

import com.rpm.remotepatientmonitoring.model.AlertThreshold;
import com.rpm.remotepatientmonitoring.model.EmergencyGuide;
import com.rpm.remotepatientmonitoring.model.EmergencyProtocol;
import com.rpm.remotepatientmonitoring.model.Hospital;
import com.rpm.remotepatientmonitoring.repository.AlertThresholdRepository;
import com.rpm.remotepatientmonitoring.repository.EmergencyGuideRepository;
import com.rpm.remotepatientmonitoring.repository.EmergencyProtocolRepository;
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
    private EmergencyProtocolRepository emergencyProtocolRepository;

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
        // --- 1. KIỂM TRA NULL (Chống sập hệ thống) ---
        if (t.getGlucoseHypoThreshold() == null || t.getGlucoseNormalMax() == null || t.getGlucoseHighMax() == null) {
            throw new IllegalArgumentException("Các chỉ số đường huyết bắt buộc phải có.");
        }
        if (t.getSystolicNormalMax() == null || t.getSystolicWarningMin() == null || t.getSystolicWarningMax() == null ||
                t.getSystolicDangerMin() == null || t.getSystolicDangerMax() == null || t.getSystolicEmergencyThreshold() == null) {
            throw new IllegalArgumentException("Các chỉ số huyết áp tâm thu bắt buộc phải có.");
        }
        if (t.getDiastolicNormalMax() == null || t.getDiastolicWarningMin() == null || t.getDiastolicWarningMax() == null ||
                t.getDiastolicDangerMin() == null || t.getDiastolicDangerMax() == null || t.getDiastolicEmergencyThreshold() == null) {
            throw new IllegalArgumentException("Các chỉ số huyết áp tâm trương bắt buộc phải có.");
        }

        // --- 2. KIỂM TRA LOGIC BẮC CẦU (A < B < C) ---
        if (t.getGlucoseHypoThreshold().compareTo(t.getGlucoseNormalMax()) >= 0) {
            throw new IllegalArgumentException("Mốc hạ đường huyết phải nhỏ hơn mốc bình thường tối đa.");
        }
        if (t.getGlucoseNormalMax().compareTo(t.getGlucoseHighMax()) >= 0) {
            throw new IllegalArgumentException("Mốc bình thường tối đa phải nhỏ hơn mốc cao tối đa.");
        }

        // --- 3. ÉP CỨNG BIÊN ĐỘ ĐƯỜNG HUYẾT THEO TÀI LIỆU ---
        // Cho phép nhập đúng số chuẩn, nhưng không được vượt quá
        if (t.getGlucoseHypoThreshold().compareTo(new BigDecimal("4.4")) > 0) {
            throw new IllegalArgumentException("Ngưỡng hạ đường huyết (Level 1) không được lớn hơn 4.4 mmol/L.");
        }
        if (t.getGlucoseNormalMax().compareTo(new BigDecimal("10.0")) > 0) {
            throw new IllegalArgumentException("Ngưỡng bình thường tối đa (Level 2) không được lớn hơn 10.0 mmol/L.");
        }
        if (t.getGlucoseHighMax().compareTo(new BigDecimal("16.0")) > 0) {
            throw new IllegalArgumentException("Ngưỡng cao tối đa (Level 3) không được lớn hơn 16.0 mmol/L.");
        }

        // --- 4. ÉP CỨNG BIÊN ĐỘ HUYẾT ÁP TÂM THU (SYSTOLIC) ---
        if (t.getSystolicNormalMax() >= t.getSystolicWarningMin()) {
            throw new IllegalArgumentException("Huyết áp tâm thu bình thường phải nhỏ hơn ngưỡng cảnh báo tối thiểu.");
        }
        if (t.getSystolicWarningMin() > t.getSystolicWarningMax()) {
            throw new IllegalArgumentException("Cảnh báo tâm thu tối thiểu không được lớn hơn cảnh báo tối đa.");
        }
        if (t.getSystolicWarningMax() >= t.getSystolicDangerMin()) {
            throw new IllegalArgumentException("Cảnh báo tâm thu tối đa phải nhỏ hơn ngưỡng nguy hiểm tối thiểu.");
        }
        if (t.getSystolicDangerMin() > t.getSystolicDangerMax()) {
            throw new IllegalArgumentException("Nguy hiểm tâm thu tối thiểu không được lớn hơn nguy hiểm tối đa.");
        }
        if (t.getSystolicDangerMax() >= t.getSystolicEmergencyThreshold()) {
            throw new IllegalArgumentException("Nguy hiểm tâm thu tối đa phải nhỏ hơn ngưỡng cấp cứu.");
        }

        // Chặn theo mốc Nghị định
        if (t.getSystolicNormalMax() > 120) throw new IllegalArgumentException("Tâm thu đạt mục tiêu (Xanh) không được vượt quá 120 mmHg.");
        if (t.getSystolicWarningMin() < 130 || t.getSystolicWarningMax() > 139) throw new IllegalArgumentException("Ngưỡng Vàng tâm thu bắt buộc phải nằm trong khung 130 - 139 mmHg.");
        if (t.getSystolicDangerMin() < 140 || t.getSystolicDangerMax() > 179) throw new IllegalArgumentException("Ngưỡng Cam tâm thu bắt buộc phải nằm trong khung 140 - 179 mmHg.");
        if (t.getSystolicEmergencyThreshold() < 180) throw new IllegalArgumentException("Ngưỡng Đỏ tâm thu bắt buộc phải từ 180 mmHg trở lên.");

        // --- 5. ÉP CỨNG BIÊN ĐỘ HUYẾT ÁP TÂM TRƯƠNG (DIASTOLIC) ---
        if (t.getDiastolicNormalMax() >= t.getDiastolicWarningMin()) {
            throw new IllegalArgumentException("Huyết áp tâm trương bình thường phải nhỏ hơn ngưỡng cảnh báo tối thiểu.");
        }
        if (t.getDiastolicWarningMin() > t.getDiastolicWarningMax()) {
            throw new IllegalArgumentException("Cảnh báo tâm trương tối thiểu không được lớn hơn cảnh báo tối đa.");
        }
        if (t.getDiastolicWarningMax() >= t.getDiastolicDangerMin()) {
            throw new IllegalArgumentException("Cảnh báo tâm trương tối đa phải nhỏ hơn ngưỡng nguy hiểm tối thiểu.");
        }
        if (t.getDiastolicDangerMin() > t.getDiastolicDangerMax()) {
            throw new IllegalArgumentException("Nguy hiểm tâm trương tối thiểu không được lớn hơn nguy hiểm tối đa.");
        }
        if (t.getDiastolicDangerMax() >= t.getDiastolicEmergencyThreshold()) {
            throw new IllegalArgumentException("Nguy hiểm tâm trương tối đa phải nhỏ hơn ngưỡng cấp cứu.");
        }

        // Chặn theo mốc Nghị định
        if (t.getDiastolicNormalMax() > 80) throw new IllegalArgumentException("Tâm trương đạt mục tiêu (Xanh) không được vượt quá 80 mmHg.");
        if (t.getDiastolicWarningMin() < 85 || t.getDiastolicWarningMax() > 89) throw new IllegalArgumentException("Ngưỡng Vàng tâm trương bắt buộc phải nằm trong khung 85 - 89 mmHg.");
        if (t.getDiastolicDangerMin() < 90 || t.getDiastolicDangerMax() > 109) throw new IllegalArgumentException("Ngưỡng Cam tâm trương bắt buộc phải nằm trong khung 90 - 109 mmHg.");
        if (t.getDiastolicEmergencyThreshold() < 110) throw new IllegalArgumentException("Ngưỡng Đỏ tâm trương bắt buộc phải từ 110 mmHg trở lên.");
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

    public List<EmergencyProtocol> getEmergencyProtocols(Integer hospitalId) {
        return emergencyProtocolRepository.findByHospitalId(hospitalId);
    }

    @Transactional
    public EmergencyProtocol addEmergencyProtocol(Integer hospitalId, String conditionType, String title, String warningSigns, String instructionContent) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bệnh viện với ID: " + hospitalId));

        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Tiêu đề cẩm nang không được để trống.");
        }
        if (warningSigns == null || warningSigns.trim().isEmpty()) {
            throw new IllegalArgumentException("Dấu hiệu nhận biết không được để trống.");
        }
        if (instructionContent == null || instructionContent.trim().isEmpty()) {
            throw new IllegalArgumentException("Nội dung chỉ dẫn không được để trống.");
        }

        EmergencyProtocol protocol = EmergencyProtocol.builder()
                .hospital(hospital)
                .conditionType(conditionType)
                .title(title.trim())
                .warningSigns(warningSigns.trim())
                .instructionContent(instructionContent.trim())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return emergencyProtocolRepository.save(protocol);
    }

    @Transactional
    public EmergencyProtocol editEmergencyProtocolContent(Integer protocolId, String warningSigns, String instructionContent) {
        EmergencyProtocol protocol = emergencyProtocolRepository.findById(protocolId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy cẩm nang với ID: " + protocolId));

        if (warningSigns == null || warningSigns.trim().isEmpty()) {
            throw new IllegalArgumentException("Dấu hiệu nhận biết không được để trống.");
        }
        if (instructionContent == null || instructionContent.trim().isEmpty()) {
            throw new IllegalArgumentException("Nội dung chỉ dẫn không được để trống.");
        }

        protocol.setWarningSigns(warningSigns.trim());
        protocol.setInstructionContent(instructionContent.trim());
        protocol.setUpdatedAt(LocalDateTime.now());
        return emergencyProtocolRepository.save(protocol);
    }

    @Transactional
    public void deleteEmergencyGuide(Integer guideId) {
        emergencyGuideRepository.deleteById(guideId);
    }

    @Transactional
    public void deleteEmergencyProtocol(Integer protocolId) {
        emergencyProtocolRepository.deleteById(protocolId);
    }
}