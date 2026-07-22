package com.rpm.remotepatientmonitoring.service.doctor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rpm.remotepatientmonitoring.model.AuditTrail;
import com.rpm.remotepatientmonitoring.repository.AuditTrailRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AuditTrailService {

    @Autowired
    private AuditTrailRepository auditTrailRepository;

    @Autowired(required = false)
    private HttpServletRequest request;

    private final ObjectMapper objectMapper;

    public AuditTrailService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public void logAction(String actorType, Integer actorId, String action, String targetTable, Integer targetRecordId, Object oldObj, Object newObj, String notes) {
        String oldValue = serializeAndClean(oldObj);
        String newValue = serializeAndClean(newObj);

        String ipAddress = null;
        String deviceInfo = null;

        if (request != null) {
            ipAddress = request.getHeader("X-Forwarded-For");
            if (ipAddress == null) {
                ipAddress = request.getRemoteAddr();
            }
            deviceInfo = request.getHeader("User-Agent");
            if (deviceInfo != null && deviceInfo.length() > 500) {
                deviceInfo = deviceInfo.substring(0, 497) + "...";
            }
        }

        AuditTrail audit = AuditTrail.builder()
                .actorType(actorType)
                .actorId(actorId)
                .action(action)
                .targetTable(targetTable)
                .targetRecordId(targetRecordId)
                .oldValue(oldValue)
                .newValue(newValue)
                .ipAddress(ipAddress)
                .deviceInfo(deviceInfo)
                .notes(notes)
                .createdAt(LocalDateTime.now())
                .build();

        auditTrailRepository.save(audit);
    }

    private String serializeAndClean(Object obj) {
        if (obj == null) return null;
        if (obj instanceof String) return stripPasswords((String) obj);

        try {
            JsonNode tree = objectMapper.valueToTree(obj);
            cleanSecurityAndEntitySubtrees(tree);
            return objectMapper.writeValueAsString(tree);
        } catch (Exception e) {
            try {
                return stripPasswords(objectMapper.writeValueAsString(obj));
            } catch (JsonProcessingException ex) {
                return "{\"summary\":\"" + obj.toString() + "\"}";
            }
        }
    }

    private void cleanSecurityAndEntitySubtrees(JsonNode node) {
        if (node == null) return;

        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            List<String> fieldNames = new ArrayList<>();
            objectNode.fieldNames().forEachRemaining(fieldNames::add);

            for (String fieldName : fieldNames) {
                String lower = fieldName.toLowerCase();

                // 1. Loại bỏ các trường bảo mật mật khẩu
                if (lower.contains("password") || lower.contains("secret") || lower.contains("token") || lower.contains("registrationdetails")) {
                    objectNode.put(fieldName, "***PROTECTED***");
                } 
                // 2. Rút gọn các đối tượng JPA lồng nhau rườm rà (Account, Hospital, Patient, Doctor)
                else if ("account".equalsIgnoreCase(fieldName)) {
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
                } else if ("nutritionrule".equalsIgnoreCase(fieldName)) {
                    JsonNode nutNode = objectNode.get(fieldName);
                    if (nutNode != null && nutNode.has("id")) {
                        objectNode.put("nutritionRuleId", nutNode.get("id").asInt());
                    }
                    objectNode.remove(fieldName);
                } else {
                    cleanSecurityAndEntitySubtrees(objectNode.get(fieldName));
                }
            }
        } else if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            for (JsonNode element : arrayNode) {
                cleanSecurityAndEntitySubtrees(element);
            }
        }
    }

    private String stripPasswords(String jsonStr) {
        if (jsonStr == null) return null;
        return jsonStr.replaceAll("(?i)\"(passwordHash|password_hash|password)\"\\s*:\\s*\"[^\"]*\"", "\"$1\":\"***PROTECTED***\"");
    }
}
