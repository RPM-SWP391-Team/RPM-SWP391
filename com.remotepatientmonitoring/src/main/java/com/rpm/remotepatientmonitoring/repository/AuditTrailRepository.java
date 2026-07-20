package com.rpm.remotepatientmonitoring.repository;

import com.rpm.remotepatientmonitoring.model.AuditTrail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

import java.util.List;

@Repository
public interface AuditTrailRepository extends JpaRepository<AuditTrail, Long> {
    Page<AuditTrail> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end, Pageable pageable);
    List<AuditTrail> findByTargetTableAndTargetRecordIdOrderByCreatedAtDesc(String targetTable, Integer targetRecordId);
    Page<AuditTrail> findByTargetTableAndTargetRecordIdAndActionOrderByCreatedAtDesc(String targetTable, Integer targetRecordId, String action, Pageable pageable);
    Page<AuditTrail> findByTargetTableAndTargetRecordIdAndActionAndCreatedAtBetweenOrderByCreatedAtDesc(String targetTable, Integer targetRecordId, String action, LocalDateTime start, LocalDateTime end, Pageable pageable);
}
