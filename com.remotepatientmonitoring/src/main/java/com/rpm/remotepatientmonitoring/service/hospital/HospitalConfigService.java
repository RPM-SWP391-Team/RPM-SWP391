package com.rpm.remotepatientmonitoring.service.hospital;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rpm.remotepatientmonitoring.model.*;
import com.rpm.remotepatientmonitoring.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class HospitalConfigService {
    @Autowired
    private AuditTrailRepository auditTrailRepository;

    @Autowired
    private AlertThresholdRepository alertThresholdRepository;

    @Autowired
    private EmergencyGuideRepository emergencyGuideRepository;

    @Autowired
    private EmergencyProtocolRepository emergencyProtocolRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private ExerciseGuidelineRepository exerciseGuidelineRepository;

    @Autowired
    private FoodDictionaryRepository foodDictionaryRepository;

    @Autowired
    private PatientMealRepository patientMealRepository;

    @Autowired
    private DiseaseProfileRepository diseaseProfileRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // --- CÁC HÀM HELPER KIỂM TRA THAY ĐỔI (Đặt ở đầu hoặc cuối service tùy bạn) ---
    private void checkAndLogString(Map<String, Object> newLog, String field, String oldVal, String newVal) {
        if (!Objects.equals(oldVal, newVal)) {
            newLog.put(field, newVal);
        }
    }

    private void checkAndLogInt(Map<String, Object> newLog, String field, Integer oldVal, Integer newVal) {
        if (!Objects.equals(oldVal, newVal)) {
            newLog.put(field, newVal);
        }
    }

    private void checkAndLogBigDecimal(Map<String, Object> newLog, String field, BigDecimal oldVal, BigDecimal newVal) {
        if (oldVal == null || newVal == null || oldVal.compareTo(newVal) != 0) {
            newLog.put(field, newVal);
        }
    }

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

        // 1. Đưa TOÀN BỘ dữ liệu cũ vào oldLog để làm sạch cột giá trị cũ
        Map<String, Object> oldLog = new HashMap<>();
        oldLog.put("glucoseHypo", existing.getGlucoseHypoThreshold());
        oldLog.put("glucoseNormalMax", existing.getGlucoseNormalMax());
        oldLog.put("glucoseHighMax", existing.getGlucoseHighMax());

        oldLog.put("sysNormal", existing.getSystolicNormalMax());
        oldLog.put("sysWarningMin", existing.getSystolicWarningMin());
        oldLog.put("sysWarningMax", existing.getSystolicWarningMax());
        oldLog.put("sysDangerMin", existing.getSystolicDangerMin());
        oldLog.put("sysDangerMax", existing.getSystolicDangerMax());
        oldLog.put("sysEmergency", existing.getSystolicEmergencyThreshold());

        oldLog.put("diaNormal", existing.getDiastolicNormalMax());
        oldLog.put("diaWarningMin", existing.getDiastolicWarningMin());
        oldLog.put("diaWarningMax", existing.getDiastolicWarningMax());
        oldLog.put("diaDangerMin", existing.getDiastolicDangerMin());
        oldLog.put("diaDangerMax", existing.getDiastolicDangerMax());
        oldLog.put("diaEmergency", existing.getDiastolicEmergencyThreshold());

        // 2. Chỉ nhặt các trường CÓ THAY ĐỔI thực sự cho cột giá trị mới (newLog)
        Map<String, Object> newLog = new HashMap<>();
        checkAndLogBigDecimal(newLog, "glucoseHypo", existing.getGlucoseHypoThreshold(), updated.getGlucoseHypoThreshold());
        checkAndLogBigDecimal(newLog, "glucoseNormalMax", existing.getGlucoseNormalMax(), updated.getGlucoseNormalMax());
        checkAndLogBigDecimal(newLog, "glucoseHighMax", existing.getGlucoseHighMax(), updated.getGlucoseHighMax());

        checkAndLogInt(newLog, "sysNormal", existing.getSystolicNormalMax(), updated.getSystolicNormalMax());
        checkAndLogInt(newLog, "sysWarningMin", existing.getSystolicWarningMin(), updated.getSystolicWarningMin());
        checkAndLogInt(newLog, "sysWarningMax", existing.getSystolicWarningMax(), updated.getSystolicWarningMax());
        checkAndLogInt(newLog, "sysDangerMin", existing.getSystolicDangerMin(), updated.getSystolicDangerMin());
        checkAndLogInt(newLog, "sysDangerMax", existing.getSystolicDangerMax(), updated.getSystolicDangerMax());
        checkAndLogInt(newLog, "sysEmergency", existing.getSystolicEmergencyThreshold(), updated.getSystolicEmergencyThreshold());

        checkAndLogInt(newLog, "diaNormal", existing.getDiastolicNormalMax(), updated.getDiastolicNormalMax());
        checkAndLogInt(newLog, "diaWarningMin", existing.getDiastolicWarningMin(), updated.getDiastolicWarningMin());
        checkAndLogInt(newLog, "diaWarningMax", existing.getDiastolicWarningMax(), updated.getDiastolicWarningMax());
        checkAndLogInt(newLog, "diaDangerMin", existing.getDiastolicDangerMin(), updated.getDiastolicDangerMin());
        checkAndLogInt(newLog, "diaDangerMax", existing.getDiastolicDangerMax(), updated.getDiastolicDangerMax());
        checkAndLogInt(newLog, "diaEmergency", existing.getDiastolicEmergencyThreshold(), updated.getDiastolicEmergencyThreshold());

        // 3. Thực hiện gán giá trị mới và lưu DB (Luồng gốc giữ nguyên)
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
        AlertThreshold savedThreshold = alertThresholdRepository.save(existing);

        // 4. Parse dữ liệu ra JSON
        String oldValueJson;
        String newValueJson;
        try {
            oldValueJson = objectMapper.writeValueAsString(oldLog);
            newValueJson = objectMapper.writeValueAsString(newLog); // Trả về dạng "{}" nếu không đổi gì, giao diện của bạn xử lý hiển thị rất mượt
        } catch (Exception e) {
            oldValueJson = "{\"error\":\"Lỗi parse dữ liệu cũ\"}";
            newValueJson = "{\"error\":\"Lỗi parse dữ liệu mới\"}";
        }

        saveAuditLog("UPDATE_THRESHOLD", "alert_thresholds", savedThreshold.getId(), oldValueJson, newValueJson, "Cập nhật cấu hình ngưỡng cảnh báo hệ thống");

        return savedThreshold;
    }

    private void validateThresholds(AlertThreshold t) {
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

        if (t.getGlucoseHypoThreshold().compareTo(t.getGlucoseNormalMax()) >= 0) {
            throw new IllegalArgumentException("Mốc hạ đường huyết phải nhỏ hơn mốc bình thường tối đa.");
        }
        if (t.getGlucoseNormalMax().compareTo(t.getGlucoseHighMax()) >= 0) {
            throw new IllegalArgumentException("Mốc bình thường tối đa phải nhỏ hơn mốc cao tối đa.");
        }

        if (t.getGlucoseHypoThreshold().compareTo(new BigDecimal("4.4")) > 0) {
            throw new IllegalArgumentException("Ngưỡng hạ đường huyết (Level 1) không được lớn hơn 4.4 mmol/L.");
        }
        if (t.getGlucoseNormalMax().compareTo(new BigDecimal("10.0")) > 0) {
            throw new IllegalArgumentException("Ngưỡng bình thường tối đa (Level 2) không được lớn hơn 10.0 mmol/L.");
        }
        if (t.getGlucoseHighMax().compareTo(new BigDecimal("16.0")) > 0) {
            throw new IllegalArgumentException("Ngưỡng cao tối đa (Level 3) không được lớn hơn 16.0 mmol/L.");
        }

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

        if (t.getSystolicNormalMax() > 120) throw new IllegalArgumentException("Tâm thu đạt mục tiêu (Xanh) không được vượt quá 120 mmHg.");
        if (t.getSystolicWarningMin() < 130 || t.getSystolicWarningMax() > 139) throw new IllegalArgumentException("Ngưỡng Vàng tâm thu bắt buộc phải nằm trong khung 130 - 139 mmHg.");
        if (t.getSystolicDangerMin() < 140 || t.getSystolicDangerMax() > 179) throw new IllegalArgumentException("Ngưỡng Cam tâm thu bắt buộc phải nằm trong khung 140 - 179 mmHg.");
        if (t.getSystolicEmergencyThreshold() < 180) throw new IllegalArgumentException("Ngưỡng Đỏ tâm thu bắt buộc phải từ 180 mmHg trở lên.");

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

        EmergencyGuide savedGuide = emergencyGuideRepository.save(guide);

        String newValueJson = "-";
        try {
            Map<String, Object> logMap = new HashMap<>();
            logMap.put("alertLevel", alertLevel);
            logMap.put("metricType", metricType);
            logMap.put("title", title.trim());
            logMap.put("instructionContent", instructionContent.trim());
            newValueJson = objectMapper.writeValueAsString(logMap);
        } catch (Exception e) {
            newValueJson = "{\"error\":\"Lỗi định dạng cấu trúc dữ liệu\"}";
        }
        saveAuditLog("CREATE_GUIDE", "emergency_guides", savedGuide.getId(), "-", newValueJson, "Thêm mới Hướng dẫn xử lý khẩn cấp");

        return savedGuide;
    }

    @Transactional
    public EmergencyGuide editEmergencyGuideContent(Integer guideId, String title, String instructionContent) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Thất bại: Tiêu đề không được để trống.");
        }
        if(title.trim().length()<10 || title.trim().length()>50) {
            throw new IllegalArgumentException("Thất bại: Dấu hiệu nhận biết phải từ 10 đến 50 ký tự.");
        }
        if (instructionContent == null || instructionContent.trim().isEmpty()) {
            throw new IllegalArgumentException("Thất bại: Nội dung chỉ dẫn không được để trống.");
        }
        if (instructionContent.trim().length() < 20 || instructionContent.trim().length() > 2000) {
            throw new IllegalArgumentException("Thất bại: Nội dung chỉ dẫn khẩn cấp phải từ 20 đến 2000 ký tự.");
        }
        EmergencyGuide guide = emergencyGuideRepository.findById(guideId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hướng dẫn với ID: " + guideId));

        // Lấy toàn bộ thông tin cũ
        Map<String, Object> oldLog = new HashMap<>();
        oldLog.put("title", guide.getTitle());
        oldLog.put("alertLevel", guide.getAlertLevel());
        oldLog.put("metricType", guide.getMetricType());
        oldLog.put("instructionContent", guide.getInstructionContent());

        // Kiểm tra thay đổi cho newLog
        Map<String, Object> newLog = new HashMap<>();
        String newTitle = title.trim();
        String newInstruction = instructionContent.trim();

        checkAndLogString(newLog, "title", guide.getTitle(), newTitle);
        checkAndLogString(newLog, "instructionContent", guide.getInstructionContent(), newInstruction);

        guide.setTitle(newTitle);
        guide.setInstructionContent(newInstruction);
        guide.setUpdatedAt(LocalDateTime.now());
        EmergencyGuide savedGuide = emergencyGuideRepository.save(guide);

        String oldValueJson;
        String newValueJson;
        try {
            oldValueJson = objectMapper.writeValueAsString(oldLog);
            newValueJson = objectMapper.writeValueAsString(newLog);
        } catch (Exception e) {
            oldValueJson = "{\"error\":\"Lỗi định dạng dữ liệu cũ\"}";
            newValueJson = "{\"error\":\"Lỗi định dạng dữ liệu mới\"}";
        }

        saveAuditLog("UPDATE_GUIDE", "emergency_guides", savedGuide.getId(), oldValueJson, newValueJson, "Cập nhật Tiêu đề và Nội dung Hướng dẫn xử lý khẩn cấp");

        return savedGuide;
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
        if (emergencyProtocolRepository.existsByHospitalIdAndConditionTypeAndIsActiveTrue(hospitalId, conditionType)) {
            throw new IllegalArgumentException("Nhóm bệnh lý '" + conditionType + "' đã có cẩm nang đang hoạt động. Vui lòng chỉnh sửa bài cũ hoặc vô hiệu hóa nó trước khi tạo mới.");
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

        EmergencyProtocol savedProtocol = emergencyProtocolRepository.save(protocol);

        String newValueJson = "-";
        try {
            Map<String, Object> logMap = new HashMap<>();
            logMap.put("conditionType", conditionType);
            logMap.put("title", title.trim());
            logMap.put("warningSigns", warningSigns.trim());
            logMap.put("instructionContent", instructionContent.trim());
            newValueJson = objectMapper.writeValueAsString(logMap);
        } catch (Exception e) {
            newValueJson = "{\"error\":\"Lỗi định dạng cấu trúc dữ liệu\"}";
        }
        saveAuditLog("CREATE_PROTOCOL", "emergency_protocols", savedProtocol.getId(), "-", newValueJson, "Thêm mới Cẩm nang nhận biết bệnh lý");

        return savedProtocol;
    }

    @Transactional
    public EmergencyProtocol editEmergencyProtocolContent(Integer protocolId, String title, String warningSigns, String instructionContent) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Thất bại: Tiêu đề không được để trống.");
        }
        if(title.trim().length()<10 || title.trim().length()>50) {
            throw new IllegalArgumentException("Thất bại: Dấu hiệu nhận biết phải từ 10 đến 50 ký tự.");
        }
        if (warningSigns == null || warningSigns.trim().isEmpty()) {
            throw new IllegalArgumentException("Thất bại: Dấu hiệu nhận biết không được để trống.");
        }
        if (warningSigns.trim().length() < 10 || warningSigns.trim().length() > 500) {
            throw new IllegalArgumentException("Thất bại: Dấu hiệu nhận biết phải từ 10 đến 500 ký tự.");
        }
        if (instructionContent == null || instructionContent.trim().isEmpty()) {
            throw new IllegalArgumentException("Thất bại: Nội dung cẩm nang không được để trống.");
        }
        if (instructionContent.trim().length() < 20 || instructionContent.trim().length() > 2000) {
            throw new IllegalArgumentException("Thất bại: Nội dung cẩm nang xử lý phải từ 20 đến 2000 ký tự.");
        }

        EmergencyProtocol protocol = emergencyProtocolRepository.findById(protocolId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy cẩm nang với ID: " + protocolId));

        // Lấy toàn bộ thông tin cũ
        Map<String, Object> oldLog = new HashMap<>();
        oldLog.put("title", protocol.getTitle());
        oldLog.put("conditionType", protocol.getConditionType());
        oldLog.put("warningSigns", protocol.getWarningSigns());
        oldLog.put("instructionContent", protocol.getInstructionContent());

        // Kiểm tra thay đổi cho newLog
        Map<String, Object> newLog = new HashMap<>();
        String newTitle = title.trim();
        String newWarning = warningSigns.trim();
        String newInstruction = instructionContent.trim();

        checkAndLogString(newLog, "title", protocol.getTitle(), newTitle);
        checkAndLogString(newLog, "warningSigns", protocol.getWarningSigns(), newWarning);
        checkAndLogString(newLog, "instructionContent", protocol.getInstructionContent(), newInstruction);

        protocol.setTitle(newTitle);
        protocol.setWarningSigns(newWarning);
        protocol.setInstructionContent(newInstruction);
        protocol.setUpdatedAt(LocalDateTime.now());

        EmergencyProtocol savedProtocol = emergencyProtocolRepository.save(protocol);

        String oldValueJson;
        String newValueJson;
        try {
            oldValueJson = objectMapper.writeValueAsString(oldLog);
            newValueJson = objectMapper.writeValueAsString(newLog);
        } catch (Exception e) {
            oldValueJson = "{\"error\":\"Lỗi định dạng dữ liệu cũ\"}";
            newValueJson = "{\"error\":\"Lỗi định dạng dữ liệu mới\"}";
        }

        saveAuditLog("UPDATE_PROTOCOL", "emergency_protocols", savedProtocol.getId(), oldValueJson, newValueJson, "Cập nhật Tiêu đề và Nội dung Cẩm nang nhận biết bệnh lý");

        return savedProtocol;
    }
    @Transactional
    public void deleteEmergencyGuide(Integer guideId) {
        // 1. Tìm bản ghi hiện tại
        EmergencyGuide existing = emergencyGuideRepository.findById(guideId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Hướng dẫn xử lý khẩn cấp với ID: " + guideId));

        // 2. Thực hiện vô hiệu hóa (Soft Delete)
        existing.setIsActive(false);
        emergencyGuideRepository.save(existing);

        // 3. Ghi log hệ thống với cấu trúc JSON đồng bộ
        saveAuditLog(
                "DELETE_GUIDE",
                "emergency_guides",
                guideId,
                "{\"isActive\":true}",
                "{\"isActive\":false}",
                "Vô hiệu hóa Hướng dẫn xử lý khẩn cấp"
        );
    }

    @Transactional
    public void deleteEmergencyProtocol(Integer protocolId) {
        // 1. Tìm bản ghi hiện tại
        EmergencyProtocol existing = emergencyProtocolRepository.findById(protocolId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Cẩm nang nhận biết bệnh lý với ID: " + protocolId));

        // 2. Thực hiện vô hiệu hóa (Soft Delete)
        existing.setIsActive(false);
        emergencyProtocolRepository.save(existing);

        // 3. Ghi log hệ thống với cấu trúc JSON đồng bộ
        saveAuditLog(
                "DELETE_PROTOCOL",
                "emergency_protocols",
                protocolId,
                "{\"isActive\":true}",
                "{\"isActive\":false}",
                "Vô hiệu hóa Cẩm nang nhận biết bệnh lý"
        );
    }

    @Transactional
    public EmergencyGuide restoreEmergencyGuide(Integer guideId) {
        EmergencyGuide existing = emergencyGuideRepository.findById(guideId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Hướng dẫn xử lý khẩn cấp với ID: " + guideId));

        // Kiểm tra xem vị trí này đã bị bản ghi mới nào chiếm chỗ chưa
        if (emergencyGuideRepository.existsByHospitalIdAndAlertLevelAndMetricTypeAndIsActiveTrue(
                existing.getHospital().getId(), existing.getAlertLevel(), existing.getMetricType())) {
            throw new IllegalArgumentException("Không thể khôi phục! Đã có một chỉ dẫn khác đang hoạt động cho cấp độ này. Vui lòng vô hiệu hóa chỉ dẫn đang chạy trước.");
        }

        existing.setIsActive(true);
        existing.setUpdatedAt(LocalDateTime.now());
        EmergencyGuide restoredGuide = emergencyGuideRepository.save(existing);

        saveAuditLog("RESTORE_GUIDE", "emergency_guides", guideId, "{\"isActive\":false}", "{\"isActive\":true}", "Khôi phục Hướng dẫn xử lý khẩn cấp");
        return restoredGuide;
    }

    @Transactional
    public EmergencyProtocol restoreEmergencyProtocol(Integer protocolId) {
        EmergencyProtocol existing = emergencyProtocolRepository.findById(protocolId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Cẩm nang với ID: " + protocolId));

        // Kiểm tra xem vị trí này đã bị bản ghi mới nào chiếm chỗ chưa
        if (emergencyProtocolRepository.existsByHospitalIdAndConditionTypeAndIsActiveTrue(
                existing.getHospital().getId(), existing.getConditionType())) {
            throw new IllegalArgumentException("Không thể khôi phục! Đã có một cẩm nang khác đang hoạt động cho bệnh lý này. Vui lòng vô hiệu hóa cẩm nang đang chạy trước.");
        }

        existing.setIsActive(true);
        existing.setUpdatedAt(LocalDateTime.now());
        EmergencyProtocol restoredProtocol = emergencyProtocolRepository.save(existing);

        saveAuditLog("RESTORE_PROTOCOL", "emergency_protocols", protocolId, "{\"isActive\":false}", "{\"isActive\":true}", "Khôi phục Cẩm nang nhận biết bệnh lý");
        return restoredProtocol;
    }

    private void saveAuditLog(String action, String targetTable, Integer targetRecordId, String oldValue, String newValue, String notes) {
        try {
            Integer adminId = 1;
            String adminEmail = "HOSPITAL_ADMIN";

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                Object principal = auth.getPrincipal();
                if (principal instanceof com.rpm.remotepatientmonitoring.config.CustomUserDetails) {
                    Account acc = ((com.rpm.remotepatientmonitoring.config.CustomUserDetails) principal).getAccount();
                    adminId = acc.getId();
                    adminEmail = acc.getEmail();
                }
            }

            String ipAddress = "Unknown";
            String deviceInfo = "Unknown";
            try {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();

                    deviceInfo = request.getHeader("User-Agent");
                    if (deviceInfo != null && deviceInfo.length() > 250) {
                        deviceInfo = deviceInfo.substring(0, 250);
                    }

                    ipAddress = request.getHeader("X-Forwarded-For");
                    if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                        ipAddress = request.getRemoteAddr();
                    }
                    if (ipAddress != null && ipAddress.contains(",")) {
                        ipAddress = ipAddress.split(",")[0].trim();
                    }
                }
            } catch (Exception e) {}

            String finalNotes = notes + " (Thực hiện bởi: " + adminEmail + ")";

            AuditTrail log = AuditTrail.builder()
                    .actorType("HOSPITAL_ADMIN")
                    .actorId(adminId)
                    .action(action)
                    .targetTable(targetTable)
                    .targetRecordId(targetRecordId)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .ipAddress(ipAddress)
                    .deviceInfo(deviceInfo)
                    .notes(finalNotes)
                    .build();

            auditTrailRepository.save(log);
        } catch (Exception e) {
            System.err.println("Cảnh báo: Lỗi hệ thống khi ghi Audit Log cấu hình (" + action + "): " + e.getMessage());
        }
    }

    // ==========================================
    // KHUYẾN NGHỊ TẬP LUYỆN
    // ==========================================
    public List<ExerciseGuideline> getExerciseGuidelines(Integer hospitalId) {
        return exerciseGuidelineRepository.findByHospitalIdAndIsActiveTrue(hospitalId);
    }

    private void validateGuideline(Integer diseaseProfileId, String title, String recommendedContent, String avoidContent) {
        if (diseaseProfileId == null) {
            throw new IllegalArgumentException("diseaseProfileId:Vui lòng chọn nhóm bệnh.");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("title:Tiêu đề không được để trống.");
        }
        if (title.trim().length() > 255) {
            throw new IllegalArgumentException("title:Tiêu đề tối đa 255 ký tự.");
        }
        if (recommendedContent == null || recommendedContent.trim().isEmpty()) {
            throw new IllegalArgumentException("recommendedContent:Nội dung nên làm không được để trống.");
        }
        if (recommendedContent.trim().length() < 10) {
            throw new IllegalArgumentException("recommendedContent:Nội dung nên làm phải có tối thiểu 10 ký tự.");
        }
        if (avoidContent == null || avoidContent.trim().isEmpty()) {
            throw new IllegalArgumentException("avoidContent:Nội dung cần tránh không được để trống.");
        }
        if (avoidContent.trim().length() < 10) {
            throw new IllegalArgumentException("avoidContent:Nội dung cần tránh phải có tối thiểu 10 ký tự.");
        }
    }

    @Transactional
    public ExerciseGuideline addExerciseGuideline(Integer hospitalId, Integer diseaseProfileId, String title, String recommendedContent, String avoidContent) {
        validateGuideline(diseaseProfileId, title, recommendedContent, avoidContent);

        exerciseGuidelineRepository.findByDiseaseProfileIdAndHospitalIdAndIsActiveTrue(diseaseProfileId, hospitalId)
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("diseaseProfileId:Nhóm bệnh này đã có khuyến nghị đang áp dụng. Vui lòng sửa bản ghi hiện có thay vì tạo mới.");
                });

        DiseaseProfile profile = diseaseProfileRepository.findById(diseaseProfileId)
                .orElseThrow(() -> new IllegalArgumentException("diseaseProfileId:Không tìm thấy nhóm bệnh lý."));

        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bệnh viện."));

        ExerciseGuideline guideline = new ExerciseGuideline();
        guideline.setHospital(hospital);
        guideline.setDiseaseProfile(profile);
        guideline.setTitle(title != null ? title.trim() : "");
        guideline.setRecommendedContent(recommendedContent != null ? recommendedContent.trim() : "");
        guideline.setAvoidContent(avoidContent != null ? avoidContent.trim() : "");
        guideline.setIsActive(true);
        guideline.setCreatedAt(LocalDateTime.now());

        ExerciseGuideline saved = exerciseGuidelineRepository.save(guideline);

        String newValueJson = "-";
        try {
            Map<String, Object> logMap = new HashMap<>();
            logMap.put("diseaseProfile", profile.getProfileName() != null ? profile.getProfileName() : String.valueOf(diseaseProfileId));
            logMap.put("title", saved.getTitle());
            logMap.put("recommendedContent", saved.getRecommendedContent());
            logMap.put("avoidContent", saved.getAvoidContent());
            newValueJson = objectMapper.writeValueAsString(logMap);
        } catch (Exception ignored) {}

        saveAuditLog("CREATE_EXERCISE_GUIDELINE", "exercise_guidelines", saved.getId(), "-", newValueJson, "Thêm mới khuyến nghị tập luyện");

        return saved;
    }

    @Transactional
    public ExerciseGuideline editExerciseGuideline(Integer id, Integer diseaseProfileId, String title, String recommendedContent, String avoidContent) {
        ExerciseGuideline existing = exerciseGuidelineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khuyến nghị tập luyện với ID: " + id));

        validateGuideline(diseaseProfileId, title, recommendedContent, avoidContent);

        if (!existing.getDiseaseProfile().getId().equals(diseaseProfileId)) {
            exerciseGuidelineRepository.findByDiseaseProfileIdAndHospitalIdAndIsActiveTrue(diseaseProfileId, existing.getHospital().getId())
                    .ifPresent(other -> {
                        if (!other.getId().equals(id)) {
                            throw new IllegalArgumentException("diseaseProfileId:Nhóm bệnh này đã có khuyến nghị đang áp dụng. Vui lòng sửa bản ghi hiện có thay vì tạo mới.");
                        }
                    });
        }

        // Snapshot dữ liệu cũ
        String oldProfileName = existing.getDiseaseProfile().getProfileName();
        String oldTitle = existing.getTitle();
        String oldRec = existing.getRecommendedContent();
        String oldAvoid = existing.getAvoidContent();

        DiseaseProfile profile = diseaseProfileRepository.findById(diseaseProfileId)
                .orElseThrow(() -> new IllegalArgumentException("diseaseProfileId:Không tìm thấy nhóm bệnh lý."));

        existing.setDiseaseProfile(profile);
        existing.setTitle(title != null ? title.trim() : "");
        existing.setRecommendedContent(recommendedContent != null ? recommendedContent.trim() : "");
        existing.setAvoidContent(avoidContent != null ? avoidContent.trim() : "");

        ExerciseGuideline saved = exerciseGuidelineRepository.save(existing);

        String oldValueJson = "-";
        String newValueJson = "-";
        try {
            Map<String, Object> oldLog = new HashMap<>();
            oldLog.put("diseaseProfile", oldProfileName != null ? oldProfileName : String.valueOf(existing.getDiseaseProfile().getId()));
            oldLog.put("title", oldTitle);
            oldLog.put("recommendedContent", oldRec);
            oldLog.put("avoidContent", oldAvoid);

            Map<String, Object> newLog = new HashMap<>();
            newLog.put("diseaseProfile", profile.getProfileName() != null ? profile.getProfileName() : String.valueOf(diseaseProfileId));
            newLog.put("title", saved.getTitle());
            newLog.put("recommendedContent", saved.getRecommendedContent());
            newLog.put("avoidContent", saved.getAvoidContent());

            oldValueJson = objectMapper.writeValueAsString(oldLog);
            newValueJson = objectMapper.writeValueAsString(newLog);
        } catch (Exception ignored) {}

        saveAuditLog("UPDATE_EXERCISE_GUIDELINE", "exercise_guidelines", saved.getId(), oldValueJson, newValueJson, "Cập nhật khuyến nghị tập luyện");

        return saved;
    }

    @Transactional
    public void deleteExerciseGuideline(Integer id) {
        ExerciseGuideline existing = exerciseGuidelineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khuyến nghị tập luyện với ID: " + id));

        existing.setIsActive(false);
        exerciseGuidelineRepository.save(existing);

        saveAuditLog("DELETE_EXERCISE_GUIDELINE", "exercise_guidelines", id, "{\"isActive\":true}", "{\"isActive\":false}", "Vô hiệu hóa khuyến nghị tập luyện");
    }

    // ==========================================
    // DANH MỤC THỰC PHẨM
    // ==========================================
    public Page<FoodDictionary> searchFoods(String search, Pageable pageable) {
        if (search == null || search.trim().isEmpty()) {
            return foodDictionaryRepository.findByIsActiveTrue(pageable);
        }
        return foodDictionaryRepository.findByIsActiveTrueAndFoodNameContainingIgnoreCaseOrIsActiveTrueAndEnglishNameContainingIgnoreCase(search, search, pageable);
    }

    public long getDietLogsCountByFoodId(Integer foodId) {
        return patientMealRepository.countByFoodId(foodId);
    }

    private void validateFood(FoodDictionary food) {
        if (food.getFoodCode() == null || food.getFoodCode().trim().isEmpty()) {
            throw new IllegalArgumentException("foodCode:Mã thực phẩm không được để trống.");
        }
        if (food.getFoodCode().trim().length() > 50) {
            throw new IllegalArgumentException("foodCode:Mã thực phẩm tối đa 50 ký tự.");
        }
        if (food.getFoodName() == null || food.getFoodName().trim().isEmpty()) {
            throw new IllegalArgumentException("foodName:Tên món ăn không được để trống.");
        }
        if (food.getFoodName().trim().length() > 255) {
            throw new IllegalArgumentException("foodName:Tên món ăn tối đa 255 ký tự.");
        }
        if (food.getEnglishName() != null) {
            String trimmedEng = food.getEnglishName().trim();
            if (trimmedEng.length() > 255) {
                throw new IllegalArgumentException("englishName:Tên tiếng Anh tối đa 255 ký tự.");
            }
            food.setEnglishName(trimmedEng.isEmpty() ? null : trimmedEng);
        }
        validateFoodNutrients(food);
    }

    private void validateFoodNutrients(FoodDictionary food) {
        if (food.getWaterG() != null && food.getWaterG().doubleValue() < 0) throw new IllegalArgumentException("waterG:Nước (g) phải >= 0");
        if (food.getEnergyKcal() != null && food.getEnergyKcal() < 0) throw new IllegalArgumentException("energyKcal:Calo (kcal) phải >= 0");
        if (food.getProteinG() != null && food.getProteinG().doubleValue() < 0) throw new IllegalArgumentException("proteinG:Đạm (g) phải >= 0");
        if (food.getLipidG() != null && food.getLipidG().doubleValue() < 0) throw new IllegalArgumentException("lipidG:Béo (g) phải >= 0");
        if (food.getGlucidG() != null && food.getGlucidG().doubleValue() < 0) throw new IllegalArgumentException("glucidG:Tinh bột (g) phải >= 0");
        if (food.getCellulozaG() != null && food.getCellulozaG().doubleValue() < 0) throw new IllegalArgumentException("cellulozaG:Xơ (g) phải >= 0");
        if (food.getAshG() != null && food.getAshG().doubleValue() < 0) throw new IllegalArgumentException("ashG:Tro (g) phải >= 0");
    }

    @Transactional
    public FoodDictionary addFood(FoodDictionary food) {
        validateFood(food);
        food.setFoodCode(food.getFoodCode().trim());
        food.setFoodName(food.getFoodName().trim());

        if (foodDictionaryRepository.existsByFoodCode(food.getFoodCode())) {
            throw new IllegalArgumentException("foodCode:Mã thực phẩm đã tồn tại.");
        }

        food.setIsActive(true);
        food.setCreatedAt(LocalDateTime.now());
        FoodDictionary saved = foodDictionaryRepository.save(food);

        String newValueJson = "-";
        try {
            newValueJson = objectMapper.writeValueAsString(buildFoodLogMap(saved));
        } catch (Exception ignored) {}

        saveAuditLog("CREATE_FOOD", "foods_dictionary", saved.getId(), "-", newValueJson, "Thêm mới món ăn vào danh mục");
        return saved;
    }

    @Transactional
    public FoodDictionary editFood(Integer id, FoodDictionary updated) {
        FoodDictionary existing = foodDictionaryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy món ăn với ID: " + id));

        validateFood(updated);
        updated.setFoodCode(updated.getFoodCode().trim());
        updated.setFoodName(updated.getFoodName().trim());

        if (!existing.getFoodCode().equals(updated.getFoodCode())) {
            if (foodDictionaryRepository.existsByFoodCode(updated.getFoodCode())) {
                throw new IllegalArgumentException("foodCode:Mã thực phẩm đã tồn tại.");
            }
        }

        validateFoodNutrients(updated);

        Map<String, Object> oldLogMap = buildFoodLogMap(existing);

        existing.setFoodCode(updated.getFoodCode());
        existing.setFoodName(updated.getFoodName());
        existing.setEnglishName(updated.getEnglishName());
        existing.setWaterG(updated.getWaterG());
        existing.setEnergyKcal(updated.getEnergyKcal());
        existing.setProteinG(updated.getProteinG());
        existing.setLipidG(updated.getLipidG());
        existing.setGlucidG(updated.getGlucidG());
        existing.setCellulozaG(updated.getCellulozaG());
        existing.setAshG(updated.getAshG());
        existing.setIsActive(updated.getIsActive());

        FoodDictionary saved = foodDictionaryRepository.save(existing);

        String oldValueJson = "-";
        String newValueJson = "-";
        try {
            oldValueJson = objectMapper.writeValueAsString(oldLogMap);
            newValueJson = objectMapper.writeValueAsString(buildFoodLogMap(saved));
        } catch (Exception ignored) {}

        saveAuditLog("UPDATE_FOOD", "foods_dictionary", saved.getId(), oldValueJson, newValueJson, "Cập nhật thông tin món ăn");
        return saved;
    }

    @Transactional
    public FoodDictionary toggleFoodActive(Integer id) {
        FoodDictionary existing = foodDictionaryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy món ăn với ID: " + id));

        boolean oldStatus = existing.getIsActive();
        existing.setIsActive(!existing.getIsActive());
        FoodDictionary saved = foodDictionaryRepository.save(existing);

        String oldValueJson = "{\"isActive\":" + oldStatus + "}";
        String newValueJson = "{\"isActive\":" + saved.getIsActive() + "}";

        saveAuditLog("TOGGLE_FOOD_ACTIVE", "foods_dictionary", saved.getId(), oldValueJson, newValueJson,
                "Thay đổi trạng thái hoạt động món ăn thành: " + (saved.getIsActive() ? "Hoạt động" : "Vô hiệu hóa"));
        return saved;
    }

    private Map<String, Object> buildFoodLogMap(FoodDictionary food) {
        Map<String, Object> logMap = new HashMap<>();
        logMap.put("foodCode", food.getFoodCode());
        logMap.put("foodName", food.getFoodName());
        logMap.put("englishName", food.getEnglishName());
        logMap.put("waterG", food.getWaterG());
        logMap.put("energyKcal", food.getEnergyKcal());
        logMap.put("proteinG", food.getProteinG());
        logMap.put("lipidG", food.getLipidG());
        logMap.put("glucidG", food.getGlucidG());
        logMap.put("cellulozaG", food.getCellulozaG());
        logMap.put("ashG", food.getAshG());
        return logMap;
    }
}