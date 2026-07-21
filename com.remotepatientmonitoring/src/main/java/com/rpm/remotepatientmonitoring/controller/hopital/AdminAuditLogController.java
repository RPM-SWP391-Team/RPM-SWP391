package com.rpm.remotepatientmonitoring.controller.hopital;

import com.rpm.remotepatientmonitoring.model.AuditTrail;
import com.rpm.remotepatientmonitoring.repository.AuditTrailRepository;
import com.rpm.remotepatientmonitoring.service.hopital.AuditLogMaskingService;
import com.rpm.remotepatientmonitoring.service.hopital.ExcelExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class AdminAuditLogController {

    @Autowired
    private AuditTrailRepository auditTrailRepository;

    @Autowired
    private AuditLogMaskingService auditLogMaskingService;

    @Autowired
    private ExcelExportService excelExportService;

    @GetMapping("/hospital/audit-logs")
    public String getAuditLogs(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "actorType", required = false) String actorType,
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "fromDate", required = false) String fromDate,
            @RequestParam(value = "toDate", required = false) String toDate,
            Model model) {

        LocalDateTime start = parseFromDate(fromDate);
        LocalDateTime end = parseToDate(toDate);
        Pageable pageable = PageRequest.of(page, size);

        // Audit Logs (Lưu vết thao tác DOCTOR, PATIENT, HOSPITAL_ADMIN với Data Masking)
        Page<AuditTrail> rawLogs = auditTrailRepository.filterAuditLogs(actorType, action, keyword, start, end, pageable);
        
        // Masking dữ liệu y tế nhạy cảm cho role Admin theo Mục 1.4 BRD
        List<AuditTrail> maskedContent = rawLogs.getContent().stream()
                .map(auditLogMaskingService::maskAuditTrailForAdmin)
                .collect(Collectors.toList());

        Page<AuditTrail> maskedLogs = new PageImpl<>(maskedContent, pageable, rawLogs.getTotalElements());
        model.addAttribute("logs", maskedLogs);

        model.addAttribute("actorType", actorType);
        model.addAttribute("action", action);
        model.addAttribute("keyword", keyword);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);

        return "hospital/audit_logs";
    }

    @GetMapping("/hospital/audit-logs/export")
    public ResponseEntity<InputStreamResource> exportAuditLogs(
            @RequestParam(value = "actorType", required = false) String actorType,
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "fromDate", required = false) String fromDate,
            @RequestParam(value = "toDate", required = false) String toDate) {

        LocalDateTime start = parseFromDate(fromDate);
        LocalDateTime end = parseToDate(toDate);

        List<AuditTrail> rawLogs = auditTrailRepository.filterAuditLogsForExport(actorType, action, keyword, start, end);
        List<AuditTrail> maskedLogs = rawLogs.stream()
                .map(auditLogMaskingService::maskAuditTrailForAdmin)
                .collect(Collectors.toList());

        ByteArrayInputStream in = excelExportService.exportAuditLogsToCsv(maskedLogs);

        String filename = "NhatKy_KiemToan_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(new InputStreamResource(in));
    }

    private LocalDateTime parseFromDate(String fromDate) {
        if (fromDate == null || fromDate.trim().isEmpty()) {
            return LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        }
        try {
            return LocalDate.parse(fromDate).atStartOfDay();
        } catch (Exception e) {
            return LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        }
    }

    private LocalDateTime parseToDate(String toDate) {
        if (toDate == null || toDate.trim().isEmpty()) {
            return LocalDateTime.of(9999, 12, 31, 23, 59, 59);
        }
        try {
            return LocalDate.parse(toDate).atTime(23, 59, 59);
        } catch (Exception e) {
            return LocalDateTime.of(9999, 12, 31, 23, 59, 59);
        }
    }
}
