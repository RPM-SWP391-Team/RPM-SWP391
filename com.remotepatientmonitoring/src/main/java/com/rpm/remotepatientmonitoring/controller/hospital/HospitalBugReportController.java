package com.rpm.remotepatientmonitoring.controller.hospital;

import com.rpm.remotepatientmonitoring.config.CustomUserDetails;
import com.rpm.remotepatientmonitoring.model.Account;
import com.rpm.remotepatientmonitoring.model.BugReport;
import com.rpm.remotepatientmonitoring.service.BugReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/hospital/bug-reports")
public class HospitalBugReportController {

    @Autowired
    private BugReportService bugReportService;

    @GetMapping
    public String listBugReports(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "status", required = false) String statusFilter,
            Model model,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        // Let security context handler check, but model binding will have currentAccount from GlobalHeaderAdvice.
        // We configure pageable to query database.
        Pageable pageable = PageRequest.of(page, 10);
        
        String statusVal = (statusFilter == null || statusFilter.trim().isEmpty() || "ALL".equalsIgnoreCase(statusFilter)) ? null : statusFilter;
        
        Page<BugReport> bugPage = bugReportService.getBugReports(statusVal, pageable);
        
        model.addAttribute("bugReports", bugPage.getContent());
        model.addAttribute("pageObj", bugPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("statusFilter", statusFilter != null ? statusFilter : "ALL");
        model.addAttribute("bannedWords", bugReportService.getBannedWords());

        return "hospital/bug-reports";
    }

    @PostMapping("/update/{id}")
    public String updateBugReport(
            @PathVariable("id") Integer id,
            @RequestParam("status") String status,
            @RequestParam("adminResponse") String adminResponse,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "statusFilter", required = false) String statusFilter,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        
        if (userDetails == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn phải đăng nhập để thực hiện hành động này.");
            return "redirect:/login";
        }

        try {
            Account adminAccount = userDetails.getAccount();
            bugReportService.updateBugStatus(id, status, adminResponse, adminAccount);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái và ghi chú xử lý thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }

        return "redirect:/hospital/bug-reports?page=" + page + "&status=" + (statusFilter != null ? statusFilter : "ALL");
    }
}
