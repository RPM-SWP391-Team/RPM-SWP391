package com.rpm.remotepatientmonitoring.service.hospital;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rpm.remotepatientmonitoring.model.AuditTrail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AuditLogMaskingService {

    @Autowired
    private ObjectMapper objectMapper;

    private static final Set<String> SENSITIVE_FIELDS = new HashSet<>(Arrays.asList(
            // Mật khẩu & Bảo mật (Bắt buộc giấu 100%)
            "passwordhash", "password_hash", "password", "otpcode", "otp_code", "token", "secret", "registrationdetails",
            // Các chỉ số y tế lâm sàng & Mục tiêu điều trị
            "systolic_bp", "systolicbp", "systolic",
            "diastolic_bp", "diastolicbp", "diastolic",
            "glucose_level", "glucoselevel", "glucose", "fasting_glucose", "fastingglucose",
            "baselinesystolicbp", "baselinediastolicbp", "baselinefastingglucose", "baselinehba1c", "baselineweightkg",
            "targetsystolicbp", "targetdiastolicbp", "targetfastingglucose", "targethba1c", "targetweightkg",
            "medicalorder", "exercisegoal", "nutritionnotes", "additionalnotes",
            "hba1c", "weight", "height", "bmi", "heart_rate", "heartrate",
            "diagnosis", "clinical_notes", "clinicalnotes", "medical_history", "medicalhistory",
            "medication_name", "medicationname", "medicinename", "dosage", "instructions", "prescription",
            "patientnotes", "patient_notes"
    ));

    public AuditTrail maskAuditTrailForAdmin(AuditTrail auditTrail) {
        if (auditTrail == null) {
            return null;
        }

        String table = auditTrail.getTargetTable() != null ? auditTrail.getTargetTable().toLowerCase() : "";
        String action = auditTrail.getAction() != null ? auditTrail.getAction().toUpperCase() : "";

        // Bảng cấu hình hệ thống (Cấu hình ngưỡng cảnh báo hệ thống, Cẩm nang, Từ điển thực phẩm...)
        // là dữ liệu cấu hình quản trị viện, không phải thông tin cá nhân y tế bệnh nhân -> hiển thị mốc chỉ số cấu hình bình thường
        boolean isSystemConfigTable = table.equals("alert_thresholds") ||
                                      table.equals("exercise_guidelines") ||
                                      table.equals("emergency_protocols") ||
                                      table.equals("emergency_guides") ||
                                      table.equals("foods_dictionary") ||
                                      action.equals("UPDATE_THRESHOLD");

        return AuditTrail.builder()
                .id(auditTrail.getId())
                .actorType(auditTrail.getActorType())
                .actorId(auditTrail.getActorId())
                .action(auditTrail.getAction())
                .targetTable(auditTrail.getTargetTable())
                .targetRecordId(auditTrail.getTargetRecordId())
                .oldValue(isSystemConfigTable ? maskSecurityOnly(auditTrail.getOldValue()) : maskJsonContent(auditTrail.getOldValue()))
                .newValue(isSystemConfigTable ? maskSecurityOnly(auditTrail.getNewValue()) : maskJsonContent(auditTrail.getNewValue()))
                .ipAddress(auditTrail.getIpAddress())
                .deviceInfo(auditTrail.getDeviceInfo())
                .notes(auditTrail.getNotes())
                .createdAt(auditTrail.getCreatedAt())
                .build();
    }

    public String maskSecurityOnly(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return jsonString;
        }
        try {
            JsonNode rootNode = objectMapper.readTree(jsonString);
            maskSecurityFieldsOnly(rootNode);
            return objectMapper.writeValueAsString(rootNode);
        } catch (Exception e) {
            return jsonString;
        }
    }

    private void maskSecurityFieldsOnly(JsonNode node) {
        if (node == null) return;
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            List<String> fieldNames = new ArrayList<>();
            objectNode.fieldNames().forEachRemaining(fieldNames::add);
            for (String fieldName : fieldNames) {
                String lower = fieldName.toLowerCase();
                if (lower.contains("password") || lower.contains("otp") || lower.contains("secret") || lower.contains("registrationdetails")) {
                    objectNode.put(fieldName, "***MASKED***");
                } else {
                    maskSecurityFieldsOnly(objectNode.get(fieldName));
                }
            }
        } else if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            for (JsonNode element : arrayNode) {
                maskSecurityFieldsOnly(element);
            }
        }
    }

    public String maskJsonContent(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return jsonString;
        }

        try {
            JsonNode rootNode = objectMapper.readTree(jsonString);
            maskJsonNode(rootNode);
            return objectMapper.writeValueAsString(rootNode);
        } catch (Exception e) {
            String masked = jsonString;
            masked = masked.replaceAll("(?i)\"(passwordHash|password_hash|password|otpCode|otp_code|registrationDetails)\"\\s*:\\s*\"[^\"]*\"", "\"$1\":\"***MASKED***\"");
            for (String field : SENSITIVE_FIELDS) {
                masked = masked.replaceAll("(?i)\"" + field + "\"\\s*:\\s*\"[^\"]*\"", "\"" + field + "\":\"***MASKED***\"");
                masked = masked.replaceAll("(?i)\"" + field + "\"\\s*:\\s*[0-9.]+", "\"" + field + "\":\"***MASKED***\"");
            }
            return masked;
        }
    }

    private void maskJsonNode(JsonNode node) {
        if (node == null) return;

        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            List<String> fieldNames = new ArrayList<>();
            objectNode.fieldNames().forEachRemaining(fieldNames::add);

            for (String fieldName : fieldNames) {
                String lower = fieldName.toLowerCase();

                // Rút gọn các cây JPA rườm rà đối với bản ghi cũ trong CSDL
                if ("account".equalsIgnoreCase(fieldName)) {
                    objectNode.remove(fieldName);
                } else if ("hospital".equalsIgnoreCase(fieldName)) {
                    JsonNode hospNode = objectNode.get(fieldName);
                    if (hospNode != null && hospNode.has("id")) {
                        objectNode.put("hospitalId", hospNode.get("id").asInt());
                    }
                    objectNode.remove(fieldName);
                } else if ("patient".equalsIgnoreCase(fieldName)) {
                    JsonNode patNode = objectNode.get(fieldName);
                    if (patNode != null && patNode.has("id")) {
                        objectNode.put("patientId", patNode.get("id").asInt());
                        if (patNode.has("fullName")) {
                            objectNode.put("patientName", patNode.get("fullName").asText());
                        }
                    }
                    objectNode.remove(fieldName);
                } else if ("doctor".equalsIgnoreCase(fieldName)) {
                    JsonNode docNode = objectNode.get(fieldName);
                    if (docNode != null && docNode.has("id")) {
                        objectNode.put("doctorId", docNode.get("id").asInt());
                        if (docNode.has("fullName")) {
                            objectNode.put("doctorName", docNode.get("fullName").asText());
                        }
                    }
                    objectNode.remove(fieldName);
                } else if (isSensitiveField(fieldName)) {
                    objectNode.put(fieldName, "***MASKED***");
                } else {
                    JsonNode child = objectNode.get(fieldName);
                    if (child != null) {
                        maskJsonNode(child);
                    }
                }
            }
        } else if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            for (JsonNode element : arrayNode) {
                maskJsonNode(element);
            }
        }
    }

    private boolean isSensitiveField(String fieldName) {
        if (fieldName == null) return false;
        String lower = fieldName.toLowerCase().replaceAll("[_-]", "");

        if (SENSITIVE_FIELDS.contains(lower)) {
            return true;
        }

        return lower.contains("password") ||
               lower.contains("otp") ||
               lower.contains("glucose") ||
               lower.contains("bp") ||
               lower.contains("systolic") ||
               lower.contains("diastolic") ||
               lower.contains("hba1c") ||
               lower.contains("weight") ||
               lower.contains("diagnosis") ||
               lower.contains("medication") ||
               lower.contains("medicine") ||
               lower.contains("order") ||
               lower.contains("goal") ||
               lower.contains("notes");
    }
}
