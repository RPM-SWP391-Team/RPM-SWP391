package com.rpm.remotepatientmonitoring.service.hospital;

import com.rpm.remotepatientmonitoring.model.AuditTrail;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public ByteArrayInputStream exportAuditLogsToCsv(List<AuditTrail> logs) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        // Write UTF-8 BOM for Excel compatibility
        out.write(0xEF);
        out.write(0xBB);
        out.write(0xBF);

        try (PrintWriter writer = new PrintWriter(out, true, StandardCharsets.UTF_8)) {
            // Header Row
            writer.println("STT,Thời Gian,Tên Người Thực Hiện,Vai Trò,ID Người Thực Hiện,Hành Động,Bảng Tác Động,ID Bản Ghi,IP Nguồn,Thiết Bị,Ghi Chú / Giải Trình");

            int index = 1;
            for (AuditTrail log : logs) {
                String timeStr = log.getCreatedAt() != null ? log.getCreatedAt().format(DATE_FORMATTER) : "";
                String actorName = escapeCsv(log.getActorName());
                String actorType = escapeCsv(log.getActorType());
                String actorId = log.getActorId() != null ? log.getActorId().toString() : "";
                String action = escapeCsv(log.getAction());
                String targetTable = escapeCsv(log.getTargetTable());
                String targetRecordId = log.getTargetRecordId() != null ? log.getTargetRecordId().toString() : "";
                String ipAddress = escapeCsv(log.getIpAddress());
                String deviceInfo = escapeCsv(log.getDeviceInfo());
                String notes = escapeCsv(log.getNotes());

                writer.printf("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                        index++, timeStr, actorName, actorType, actorId, action, targetTable, targetRecordId, ipAddress, deviceInfo, notes);
            }

            writer.flush();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi xuất file báo cáo nhật ký: " + e.getMessage(), e);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private String escapeCsv(String data) {
        if (data == null) return "-";
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            escapedData = escapedData.replace("\"", "\"\"");
        }
        return escapedData;
    }
}
