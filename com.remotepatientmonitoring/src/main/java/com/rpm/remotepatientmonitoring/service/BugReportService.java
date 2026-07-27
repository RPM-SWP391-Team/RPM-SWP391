package com.rpm.remotepatientmonitoring.service;

import com.rpm.remotepatientmonitoring.model.Account;
import com.rpm.remotepatientmonitoring.model.BugReport;
import com.rpm.remotepatientmonitoring.repository.BugReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BugReportService {

    @Autowired
    private BugReportRepository bugReportRepository;

    private List<String> bannedWords;

    public List<String> getBannedWords() {
        return bannedWords;
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        getClass().getResourceAsStream("/banned_words.txt"),
                        StandardCharsets.UTF_8))) {
            bannedWords = reader.lines()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .map(String::toLowerCase)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            bannedWords = Arrays.asList("đm", "vcl", "dkm", "ngu", "chó");
        }
    }

    private void checkBannedWords(String content) {
        if (content == null || content.trim().isEmpty()) {
            return;
        }
        String lowerContent = content.toLowerCase();
        for (String word : bannedWords) {
            if (lowerContent.contains(word)) {
                throw new IllegalArgumentException("Nội dung chứa từ ngữ không phù hợp. Vui lòng chỉnh sửa lại.");
            }
        }
    }

    @Transactional
    public BugReport submitBugReport(String title, String description, String pageUrl, Account account) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Tiêu đề không được để trống.");
        }
        if (title.length() > 255) {
            throw new IllegalArgumentException("Tiêu đề không được vượt quá 255 ký tự.");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Mô tả chi tiết không được để trống.");
        }

        checkBannedWords(title);
        checkBannedWords(description);

        if (hasDuplicateRecentReport(account.getId(), title)) {
            throw new IllegalStateException("Bạn vừa gửi báo cáo này rồi. Vui lòng đợi ít phút trước khi gửi lại, hoặc chỉnh sửa tiêu đề nếu đây là lỗi khác.");
        }

        BugReport bugReport = BugReport.builder()
                .account(account)
                .title(title.trim())
                .description(description.trim())
                .pageUrl(pageUrl != null ? pageUrl.trim() : null)
                .status("NEW")
                .createdAt(LocalDateTime.now())
                .build();

        return bugReportRepository.save(bugReport);
    }

    public boolean hasDuplicateRecentReport(Integer accountId, String title) {
        if (accountId == null || title == null) {
            return false;
        }
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        return bugReportRepository.existsDuplicateRecentReport(accountId, title.trim(), fiveMinutesAgo);
    }

    public Page<BugReport> getBugReports(String status, Pageable pageable) {
        return bugReportRepository.findByStatusWithDefaultSort(status, pageable);
    }

    @Transactional
    public BugReport updateBugStatus(Integer bugId, String newStatus, String adminResponse, Account adminAccount) {
        BugReport report = bugReportRepository.findById(bugId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy báo cáo lỗi với ID: " + bugId));

        if (!Arrays.asList("NEW", "IN_PROGRESS", "RESOLVED", "REJECTED").contains(newStatus)) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ.");
        }

        if (adminResponse != null && adminResponse.trim().length() > 1000) {
            throw new IllegalArgumentException("Ghi chú phản hồi của Admin không được vượt quá 1000 ký tự.");
        }

        if ("RESOLVED".equals(newStatus) || "REJECTED".equals(newStatus)) {
            if (adminResponse == null || adminResponse.trim().isEmpty()) {
                throw new IllegalArgumentException("Vui lòng nhập ghi chú phản hồi giải trình lý do khi giải quyết hoặc từ chối báo cáo lỗi.");
            }
        }

        report.setStatus(newStatus);
        report.setAdminResponse(adminResponse != null && !adminResponse.trim().isEmpty() ? adminResponse.trim() : null);

        if ("RESOLVED".equals(newStatus) || "REJECTED".equals(newStatus)) {
            report.setResolvedAt(LocalDateTime.now());
            report.setResolvedBy(adminAccount);
        } else {
            report.setResolvedAt(null);
            report.setResolvedBy(null);
        }

        return bugReportRepository.save(report);
    }
}
