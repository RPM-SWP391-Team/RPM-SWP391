package com.rpm.remotepatientmonitoring.controller.hospital;

import com.rpm.remotepatientmonitoring.model.AuditTrail;
import com.rpm.remotepatientmonitoring.model.HospitalAdmin;
import com.rpm.remotepatientmonitoring.model.Doctor;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.repository.HospitalAdminRepository;
import com.rpm.remotepatientmonitoring.repository.DoctorRepository;
import com.rpm.remotepatientmonitoring.repository.PatientRepository;
import java.util.Optional;
import com.rpm.remotepatientmonitoring.repository.AuditTrailRepository;
import com.rpm.remotepatientmonitoring.service.hospital.AuditLogMaskingService;
import com.rpm.remotepatientmonitoring.service.hospital.ExcelExportService;
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

    @Autowired
    private HospitalAdminRepository hospitalAdminRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

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
        
        List<AuditTrail> maskedContent = new java.util.ArrayList<>();
        for (AuditTrail log : rawLogs.getContent()) {
            maskedContent.add(auditLogMaskingService.maskAuditTrailForAdmin(log));
        }

        for (AuditTrail log : maskedContent) {
            log.setActorName(getActorNameHelper(log.getActorType(), log.getActorId()));
        }

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
        List<AuditTrail> maskedLogs = new java.util.ArrayList<>();
        for (AuditTrail log : rawLogs) {
            maskedLogs.add(auditLogMaskingService.maskAuditTrailForAdmin(log));
        }

        for (AuditTrail log : maskedLogs) {
            log.setActorName(getActorNameHelper(log.getActorType(), log.getActorId()));
        }

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

    private String getActorNameHelper(String actorType, Integer actorId) {
        if (actorType == null || actorId == null) {
            return "-";
        }
        try {
            if ("HOSPITAL_ADMIN".equalsIgnoreCase(actorType)) {
                Optional<HospitalAdmin> adminOpt = hospitalAdminRepository.findByAccountId(actorId);
                if (adminOpt.isPresent()) {
                    return adminOpt.get().getFullName();
                }
                adminOpt = hospitalAdminRepository.findById(actorId);
                if (adminOpt.isPresent()) {
                    return adminOpt.get().getFullName();
                }
            } else if ("DOCTOR".equalsIgnoreCase(actorType)) {
                Optional<Doctor> docOpt = doctorRepository.findById(actorId);
                if (docOpt.isPresent()) {
                    return docOpt.get().getFullName();
                }
                docOpt = doctorRepository.findByAccountId(actorId);
                if (docOpt.isPresent()) {
                    return docOpt.get().getFullName();
                }
            } else if ("PATIENT".equalsIgnoreCase(actorType)) {
                Optional<Patient> patOpt = patientRepository.findById(actorId);
                if (patOpt.isPresent()) {
                    return patOpt.get().getFullName();
                }
                patOpt = patientRepository.findByAccountId(actorId);
                if (patOpt.isPresent()) {
                    return patOpt.get().getFullName();
                }
            }
        } catch (Exception e) {
            // ignore and fallback
        }
        return actorType + " (ID: " + actorId + ")";
    }
}
