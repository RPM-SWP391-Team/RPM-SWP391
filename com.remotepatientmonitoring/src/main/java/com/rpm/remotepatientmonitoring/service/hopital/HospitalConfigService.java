package com.rpm.remotepatientmonitoring.service.hopital;

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
    private DiseaseProfileRepository diseaseProfileRepository;

    @Autowired
    private ObjectMapper objectMapper;

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

        // --- ĐỒNG BỘ CÁCH LẤY LOG BẰNG MAP VÀ OBJECTMAPPER ---
        String oldValueJson = "-";
        try {
            Map<String, Object> oldLog = new HashMap<>();
            oldLog.put("glucoseHypo", existing.getGlucoseHypoThreshold());
            oldLog.put("glucoseNormalMax", existing.getGlucoseNormalMax());
            oldLog.put("glucoseHighMax", existing.getGlucoseHighMax());
            oldLog.put("sysNormal", existing.getSystolicNormalMax());
            oldLog.put("sysDanger", existing.getSystolicDangerMin());
            oldLog.put("diaNormal", existing.getDiastolicNormalMax());
            oldLog.put("diaDanger", existing.getDiastolicDangerMin());
            oldValueJson = objectMapper.writeValueAsString(oldLog);
        } catch (Exception e) {
            oldValueJson = "{\"error\":\"Lỗi parse dữ liệu cũ\"}";
        }

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

        String newValueJson = "-";
        try {
            Map<String, Object> newLog = new HashMap<>();
            newLog.put("glucoseHypo", savedThreshold.getGlucoseHypoThreshold());
            newLog.put("glucoseNormalMax", savedThreshold.getGlucoseNormalMax());
            newLog.put("glucoseHighMax", savedThreshold.getGlucoseHighMax());
            newLog.put("sysNormal", savedThreshold.getSystolicNormalMax());
            newLog.put("sysDanger", savedThreshold.getSystolicDangerMin());
            newLog.put("diaNormal", savedThreshold.getDiastolicNormalMax());
            newLog.put("diaDanger", savedThreshold.getDiastolicDangerMin());
            newValueJson = objectMapper.writeValueAsString(newLog);
        } catch (Exception e) {
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
    public EmergencyGuide editEmergencyGuideContent(Integer guideId, String instructionContent) {
        EmergencyGuide guide = emergencyGuideRepository.findById(guideId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hướng dẫn với ID: " + guideId));

        if (instructionContent == null || instructionContent.trim().isEmpty()) {
            throw new IllegalArgumentException("Nội dung hướng dẫn không được để trống.");
        }

        String oldValueJson = "-";
        try {
            Map<String, Object> oldLog = new HashMap<>();
            oldLog.put("title", guide.getTitle());
            oldLog.put("alertLevel", guide.getAlertLevel());
            oldLog.put("metricType", guide.getMetricType());
            oldLog.put("instructionContent", guide.getInstructionContent());
            oldValueJson = objectMapper.writeValueAsString(oldLog);
        } catch (Exception e) {
            oldValueJson = "{\"error\":\"Lỗi định dạng dữ liệu cũ\"}";
        }

        guide.setInstructionContent(instructionContent.trim());
        guide.setUpdatedAt(LocalDateTime.now());
        EmergencyGuide savedGuide = emergencyGuideRepository.save(guide);

        String newValueJson = "-";
        try {
            Map<String, Object> newLog = new HashMap<>();
            newLog.put("title", savedGuide.getTitle());
            newLog.put("alertLevel", savedGuide.getAlertLevel());
            newLog.put("metricType", savedGuide.getMetricType());
            newLog.put("instructionContent", savedGuide.getInstructionContent());
            newValueJson = objectMapper.writeValueAsString(newLog);
        } catch (Exception e) {
            newValueJson = "{\"error\":\"Lỗi định dạng dữ liệu mới\"}";
        }

        saveAuditLog("UPDATE_GUIDE", "emergency_guides", savedGuide.getId(), oldValueJson, newValueJson, "Cập nhật nội dung Hướng dẫn xử lý khẩn cấp");

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
    public EmergencyProtocol editEmergencyProtocolContent(Integer protocolId, String warningSigns, String instructionContent) {
        EmergencyProtocol protocol = emergencyProtocolRepository.findById(protocolId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy cẩm nang với ID: " + protocolId));

        if (warningSigns == null || warningSigns.trim().isEmpty()) {
            throw new IllegalArgumentException("Dấu hiệu nhận biết không được để trống.");
        }
        if (instructionContent == null || instructionContent.trim().isEmpty()) {
            throw new IllegalArgumentException("Nội dung chỉ dẫn không được để trống.");
        }

        String oldValueJson = "-";
        try {
            Map<String, Object> oldLog = new HashMap<>();
            oldLog.put("title", protocol.getTitle());
            oldLog.put("conditionType", protocol.getConditionType());
            oldLog.put("warningSigns", protocol.getWarningSigns());
            oldLog.put("instructionContent", protocol.getInstructionContent());
            oldValueJson = objectMapper.writeValueAsString(oldLog);
        } catch (Exception e) {
            oldValueJson = "{\"error\":\"Lỗi định dạng dữ liệu cũ\"}";
        }

        protocol.setWarningSigns(warningSigns.trim());
        protocol.setInstructionContent(instructionContent.trim());
        protocol.setUpdatedAt(LocalDateTime.now());

        EmergencyProtocol savedProtocol = emergencyProtocolRepository.save(protocol);

        String newValueJson = "-";
        try {
            Map<String, Object> newLog = new HashMap<>();
            newLog.put("title", savedProtocol.getTitle());
            newLog.put("conditionType", savedProtocol.getConditionType());
            newLog.put("warningSigns", savedProtocol.getWarningSigns());
            newLog.put("instructionContent", savedProtocol.getInstructionContent());
            newValueJson = objectMapper.writeValueAsString(newLog);
        } catch (Exception e) {
            newValueJson = "{\"error\":\"Lỗi định dạng dữ liệu mới\"}";
        }

        saveAuditLog("UPDATE_PROTOCOL", "emergency_protocols", savedProtocol.getId(), oldValueJson, newValueJson, "Cập nhật nội dung Cẩm nang nhận biết bệnh lý");

        return savedProtocol;
    }

    @Transactional
    public void deleteEmergencyGuide(Integer guideId) {
        saveAuditLog("DELETE_GUIDE", "emergency_guides", guideId, "{\"status\":\"ACTIVE\"}", "{\"status\":\"DELETED\"}", "Xóa Hướng dẫn xử lý khẩn cấp");
        emergencyGuideRepository.deleteById(guideId);
    }

    @Transactional
    public void deleteEmergencyProtocol(Integer protocolId) {
        saveAuditLog("DELETE_PROTOCOL", "emergency_protocols", protocolId, "{\"status\":\"ACTIVE\"}", "{\"status\":\"DELETED\"}", "Xóa Cẩm nang nhận biết bệnh lý");
        emergencyProtocolRepository.deleteById(protocolId);
    }

    private void saveAuditLog(String action, String targetTable, Integer targetRecordId, String oldValue, String newValue, String notes) {
        try {
            Integer adminId = 1;
            String adminEmail = "HOSPITAL_ADMIN";

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                Object principal = auth.getPrincipal();
                // LƯU Ý: Đảm bảo class CustomUserDetails nằm ĐÚNG đường dẫn import dưới đây trong project của bạn
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
                        deviceInfo = deviceInfo.substring(0, 250); // Ép cứng độ dài chống vỡ CSDL
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

    @Transactional
    public ExerciseGuideline addExerciseGuideline(Integer hospitalId, Integer diseaseProfileId, String title, String recommendedContent, String avoidContent) {
        exerciseGuidelineRepository.findByDiseaseProfileIdAndHospitalIdAndIsActiveTrue(diseaseProfileId, hospitalId)
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Đã tồn tại khuyến nghị tập luyện đang hoạt động cho nhóm bệnh này.");
                });

        DiseaseProfile profile = diseaseProfileRepository.findById(diseaseProfileId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhóm bệnh lý."));

        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bệnh viện."));

        ExerciseGuideline guideline = new ExerciseGuideline();
        guideline.setHospital(hospital);
        guideline.setDiseaseProfile(profile);
        guideline.setTitle(title);
        guideline.setRecommendedContent(recommendedContent);
        guideline.setAvoidContent(avoidContent);
        guideline.setIsActive(true);
        guideline.setCreatedAt(LocalDateTime.now());

        ExerciseGuideline saved = exerciseGuidelineRepository.save(guideline);

        String newValueJson = "-";
        try {
            newValueJson = objectMapper.writeValueAsString(saved);
        } catch (Exception ignored) {}

        saveAuditLog("CREATE_EXERCISE_GUIDELINE", "exercise_guidelines", saved.getId(), null, newValueJson, "Thêm mới khuyến nghị tập luyện");

        return saved;
    }

    @Transactional
    public ExerciseGuideline editExerciseGuideline(Integer id, Integer diseaseProfileId, String title, String recommendedContent, String avoidContent) {
        ExerciseGuideline existing = exerciseGuidelineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khuyến nghị tập luyện với ID: " + id));

        if (!existing.getDiseaseProfile().getId().equals(diseaseProfileId)) {
            exerciseGuidelineRepository.findByDiseaseProfileIdAndHospitalIdAndIsActiveTrue(diseaseProfileId, existing.getHospital().getId())
                    .ifPresent(other -> {
                        if (!other.getId().equals(id)) {
                            throw new IllegalArgumentException("Đã tồn tại khuyến nghị tập luyện đang hoạt động cho nhóm bệnh này.");
                        }
                    });
        }

        String oldValueJson = "-";
        try {
            oldValueJson = objectMapper.writeValueAsString(existing);
        } catch (Exception ignored) {}

        DiseaseProfile profile = diseaseProfileRepository.findById(diseaseProfileId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhóm bệnh lý."));

        existing.setDiseaseProfile(profile);
        existing.setTitle(title);
        existing.setRecommendedContent(recommendedContent);
        existing.setAvoidContent(avoidContent);

        ExerciseGuideline saved = exerciseGuidelineRepository.save(existing);

        String newValueJson = "-";
        try {
            newValueJson = objectMapper.writeValueAsString(saved);
        } catch (Exception ignored) {}

        saveAuditLog("UPDATE_EXERCISE_GUIDELINE", "exercise_guidelines", saved.getId(), oldValueJson, newValueJson, "Cập nhật khuyến nghị tập luyện");

        return saved;
    }

    @Transactional
    public void deleteExerciseGuideline(Integer id) {
        ExerciseGuideline existing = exerciseGuidelineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khuyến nghị tập luyện với ID: " + id));

        String oldValueJson = "-";
        try {
            oldValueJson = objectMapper.writeValueAsString(existing);
        } catch (Exception ignored) {}

        existing.setIsActive(false);
        exerciseGuidelineRepository.save(existing);

        String newValueJson = "-";
        try {
            newValueJson = objectMapper.writeValueAsString(existing);
        } catch (Exception ignored) {}

        saveAuditLog("DELETE_EXERCISE_GUIDELINE", "exercise_guidelines", id, oldValueJson, newValueJson, "Vô hiệu hóa khuyến nghị tập luyện");
    }

    // ==========================================
    // DANH MỤC THỰC PHẨM
    // ==========================================
    public Page<FoodDictionary> searchFoods(String search, Pageable pageable) {
        if (search == null || search.trim().isEmpty()) {
            return foodDictionaryRepository.findAll(pageable);
        }
        return foodDictionaryRepository.findByFoodNameContainingIgnoreCaseOrEnglishNameContainingIgnoreCase(search, search, pageable);
    }

    @Transactional
    public FoodDictionary addFood(FoodDictionary food) {
        if (foodDictionaryRepository.existsByFoodCode(food.getFoodCode())) {
            throw new IllegalArgumentException("Mã món ăn (food_code) đã tồn tại.");
        }
        validateFoodNutrients(food);

        food.setIsActive(true);
        food.setCreatedAt(LocalDateTime.now());
        FoodDictionary saved = foodDictionaryRepository.save(food);

        String newValueJson = "-";
        try {
            newValueJson = objectMapper.writeValueAsString(saved);
        } catch (Exception ignored) {}

        saveAuditLog("CREATE_FOOD", "foods_dictionary", saved.getId(), null, newValueJson, "Thêm mới món ăn vào danh mục");
        return saved;
    }

    @Transactional
    public FoodDictionary editFood(Integer id, FoodDictionary updated) {
        FoodDictionary existing = foodDictionaryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy món ăn với ID: " + id));

        if (!existing.getFoodCode().equals(updated.getFoodCode())) {
            if (foodDictionaryRepository.existsByFoodCode(updated.getFoodCode())) {
                throw new IllegalArgumentException("Mã món ăn (food_code) đã tồn tại.");
            }
        }

        validateFoodNutrients(updated);

        String oldValueJson = "-";
        try {
            oldValueJson = objectMapper.writeValueAsString(existing);
        } catch (Exception ignored) {}

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

        String newValueJson = "-";
        try {
            newValueJson = objectMapper.writeValueAsString(saved);
        } catch (Exception ignored) {}

        saveAuditLog("UPDATE_FOOD", "foods_dictionary", saved.getId(), oldValueJson, newValueJson, "Cập nhật thông tin món ăn");
        return saved;
    }

    @Transactional
    public FoodDictionary toggleFoodActive(Integer id) {
        FoodDictionary existing = foodDictionaryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy món ăn với ID: " + id));

        String oldValueJson = "-";
        try {
            oldValueJson = objectMapper.writeValueAsString(existing);
        } catch (Exception ignored) {}

        existing.setIsActive(!existing.getIsActive());
        FoodDictionary saved = foodDictionaryRepository.save(existing);

        String newValueJson = "-";
        try {
            newValueJson = objectMapper.writeValueAsString(saved);
        } catch (Exception ignored) {}

        saveAuditLog("TOGGLE_FOOD_ACTIVE", "foods_dictionary", saved.getId(), oldValueJson, newValueJson, 
                     "Thay đổi trạng thái hoạt động món ăn thành: " + (saved.getIsActive() ? "Hoạt động" : "Vô hiệu hóa"));
        return saved;
    }

    private void validateFoodNutrients(FoodDictionary food) {
        if (food.getWaterG() != null && food.getWaterG().doubleValue() < 0) throw new IllegalArgumentException("Nước (g) phải >= 0");
        if (food.getEnergyKcal() != null && food.getEnergyKcal() < 0) throw new IllegalArgumentException("Calo (kcal) phải >= 0");
        if (food.getProteinG() != null && food.getProteinG().doubleValue() < 0) throw new IllegalArgumentException("Đạm (g) phải >= 0");
        if (food.getLipidG() != null && food.getLipidG().doubleValue() < 0) throw new IllegalArgumentException("Béo (g) phải >= 0");
        if (food.getGlucidG() != null && food.getGlucidG().doubleValue() < 0) throw new IllegalArgumentException("Tinh bột (g) phải >= 0");
        if (food.getCellulozaG() != null && food.getCellulozaG().doubleValue() < 0) throw new IllegalArgumentException("Xơ (g) phải >= 0");
        if (food.getAshG() != null && food.getAshG().doubleValue() < 0) throw new IllegalArgumentException("Tro (g) phải >= 0");
    }
}