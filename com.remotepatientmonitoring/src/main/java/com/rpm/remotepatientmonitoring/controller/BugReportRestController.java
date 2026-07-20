package com.rpm.remotepatientmonitoring.controller;

import com.rpm.remotepatientmonitoring.config.CustomUserDetails;
import com.rpm.remotepatientmonitoring.model.Account;
import com.rpm.remotepatientmonitoring.service.BugReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/bug-reports")
public class BugReportRestController {

    @Autowired
    private BugReportService bugReportService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> submitBugReport(
            @RequestBody BugReportRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Map<String, Object> response = new HashMap<>();
        if (userDetails == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập để gửi báo cáo lỗi.");
            return ResponseEntity.status(401).body(response);
        }

        try {
            Account account = userDetails.getAccount();
            bugReportService.submitBugReport(request.getTitle(), request.getDescription(), request.getPageUrl(), account);
            response.put("success", true);
            response.put("message", "Báo cáo lỗi của bạn đã được gửi thành công. Cảm ơn sự đóng góp của bạn!");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi máy chủ: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    public static class BugReportRequest {
        private String title;
        private String description;
        private String pageUrl;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getPageUrl() { return pageUrl; }
        public void setPageUrl(String pageUrl) { this.pageUrl = pageUrl; }
    }
}
