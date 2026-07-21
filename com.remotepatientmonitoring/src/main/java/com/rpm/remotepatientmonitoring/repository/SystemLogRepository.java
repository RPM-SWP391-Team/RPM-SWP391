package com.rpm.remotepatientmonitoring.repository;

import com.rpm.remotepatientmonitoring.model.SystemLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {

    Page<SystemLog> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end, Pageable pageable);

    @Query("SELECT s FROM SystemLog s WHERE " +
           "(:logLevel IS NULL OR :logLevel = '' OR s.logLevel = :logLevel) AND " +
           "(:moduleName IS NULL OR :moduleName = '' OR s.moduleName = :moduleName) AND " +
           "(s.createdAt BETWEEN :start AND :end) " +
           "ORDER BY s.createdAt DESC")
    Page<SystemLog> filterSystemLogs(
            @Param("logLevel") String logLevel,
            @Param("moduleName") String moduleName,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable);
}
