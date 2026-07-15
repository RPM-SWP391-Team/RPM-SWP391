package com.rpm.remotepatientmonitoring.service.doctor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rpm.remotepatientmonitoring.model.AuditTrail;
import com.rpm.remotepatientmonitoring.repository.AuditTrailRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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
        String oldValue = null;
        String newValue = null;

        try {
            if (oldObj != null) {
                oldValue = objectMapper.writeValueAsString(oldObj);
            }
            if (newObj != null) {
                newValue = objectMapper.writeValueAsString(newObj);
            }
        } catch (JsonProcessingException e) {
            oldValue = "Error parsing old object";
            newValue = "Error parsing new object";
        }

        String ipAddress = null;
        String deviceInfo = null;

        if (request != null) {
            ipAddress = request.getHeader("X-Forwarded-For");
            if (ipAddress == null) {
                ipAddress = request.getRemoteAddr();
            }
            deviceInfo = request.getHeader("User-Agent");
            // Truncate if necessary to fit DB schema limits
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
}
