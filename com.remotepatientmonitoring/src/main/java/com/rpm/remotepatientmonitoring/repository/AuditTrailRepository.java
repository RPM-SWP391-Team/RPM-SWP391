package com.rpm.remotepatientmonitoring.repository;

import com.rpm.remotepatientmonitoring.model.AuditTrail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditTrailRepository extends JpaRepository<AuditTrail, Long> {

    Page<AuditTrail> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<AuditTrail> findByTargetTableAndTargetRecordIdOrderByCreatedAtDesc(String targetTable, Integer targetRecordId);

    @Query("SELECT a FROM AuditTrail a WHERE " +
           "(:actorType IS NULL OR :actorType = '' OR a.actorType = :actorType) AND " +
           "(:action IS NULL OR :action = '' OR a.action LIKE %:action%) AND " +
           "(:keyword IS NULL OR :keyword = '' OR a.notes LIKE %:keyword% OR a.ipAddress LIKE %:keyword% OR CAST(a.targetRecordId AS string) LIKE %:keyword%) AND " +
           "(a.createdAt BETWEEN :start AND :end) " +
           "ORDER BY a.createdAt DESC")
    Page<AuditTrail> filterAuditLogs(
            @Param("actorType") String actorType,
            @Param("action") String action,
            @Param("keyword") String keyword,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable);

    @Query("SELECT a FROM AuditTrail a WHERE " +
           "(:actorType IS NULL OR :actorType = '' OR a.actorType = :actorType) AND " +
           "(:action IS NULL OR :action = '' OR a.action LIKE %:action%) AND " +
           "(:keyword IS NULL OR :keyword = '' OR a.notes LIKE %:keyword% OR a.ipAddress LIKE %:keyword% OR CAST(a.targetRecordId AS string) LIKE %:keyword%) AND " +
           "(a.createdAt BETWEEN :start AND :end) " +
           "ORDER BY a.createdAt DESC")
    List<AuditTrail> filterAuditLogsForExport(
            @Param("actorType") String actorType,
            @Param("action") String action,
            @Param("keyword") String keyword,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
