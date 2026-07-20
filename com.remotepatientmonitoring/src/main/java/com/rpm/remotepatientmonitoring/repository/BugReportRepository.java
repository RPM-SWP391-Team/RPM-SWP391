package com.rpm.remotepatientmonitoring.repository;

import com.rpm.remotepatientmonitoring.model.BugReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BugReportRepository extends JpaRepository<BugReport, Integer> {

    @Query("SELECT b FROM BugReport b WHERE (:status IS NULL OR b.status = :status) " +
           "ORDER BY CASE " +
           "  WHEN b.status = 'NEW' THEN 0 " +
           "  WHEN b.status = 'IN_PROGRESS' THEN 1 " +
           "  WHEN b.status = 'RESOLVED' THEN 2 " +
           "  WHEN b.status = 'REJECTED' THEN 3 " +
           "  ELSE 4 END ASC, b.createdAt DESC")
    Page<BugReport> findByStatusWithDefaultSort(@Param("status") String status, Pageable pageable);

    @Query("SELECT COUNT(b) > 0 FROM BugReport b WHERE b.account.id = :accountId " +
           "AND LOWER(TRIM(b.title)) = LOWER(TRIM(:title)) " +
           "AND b.createdAt >= :since")
    boolean existsDuplicateRecentReport(@Param("accountId") Integer accountId, 
                                        @Param("title") String title, 
                                        @Param("since") java.time.LocalDateTime since);
}
