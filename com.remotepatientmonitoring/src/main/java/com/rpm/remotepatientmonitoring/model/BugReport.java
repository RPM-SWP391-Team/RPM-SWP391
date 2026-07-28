package com.rpm.remotepatientmonitoring.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "bug_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BugReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "page_url", length = 500)
    private String pageUrl;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "NEW";

    @Column(name = "admin_response", columnDefinition = "NVARCHAR(MAX)")
    private String adminResponse;

    @ManyToOne
    @JoinColumn(name = "resolved_by")
    private Account resolvedBy;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
