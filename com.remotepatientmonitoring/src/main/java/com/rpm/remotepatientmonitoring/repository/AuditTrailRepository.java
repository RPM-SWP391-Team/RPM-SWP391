package com.rpm.remotepatientmonitoring.repository;

import com.rpm.remotepatientmonitoring.model.AuditTrail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditTrailRepository extends JpaRepository<AuditTrail, Long> {
    List<AuditTrail> findByTargetTableAndTargetRecordIdOrderByCreatedAtDesc(String targetTable, Integer targetRecordId);
}
