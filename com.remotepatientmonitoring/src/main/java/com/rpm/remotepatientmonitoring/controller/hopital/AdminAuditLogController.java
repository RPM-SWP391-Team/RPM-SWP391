package com.rpm.remotepatientmonitoring.controller.hopital;

import com.rpm.remotepatientmonitoring.model.AuditTrail;
import com.rpm.remotepatientmonitoring.repository.AuditTrailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
public class AdminAuditLogController {

    @Autowired
    private AuditTrailRepository auditTrailRepository;

    @GetMapping("/hospital/audit-logs")
    public String getAuditLogs(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "fromDate", required = false) String fromDate,
            @RequestParam(value = "toDate", required = false) String toDate,
            Model model) {

        LocalDateTime start;
        if (fromDate == null || fromDate.trim().isEmpty()) {
            start = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        } else {
            try {
                start = LocalDate.parse(fromDate).atStartOfDay();
            } catch (Exception e) {
                start = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
            }
        }

        LocalDateTime end;
        if (toDate == null || toDate.trim().isEmpty()) {
            end = LocalDateTime.of(9999, 12, 31, 23, 59, 59);
        } else {
            try {
                end = LocalDate.parse(toDate).atTime(23, 59, 59);
            } catch (Exception e) {
                end = LocalDateTime.of(9999, 12, 31, 23, 59, 59);
            }
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<AuditTrail> logs = auditTrailRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end, pageable);

        model.addAttribute("logs", logs);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);

        return "hospital/audit_logs";
    }
}
