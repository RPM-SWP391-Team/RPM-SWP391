package com.rpm.remotepatientmonitoring.repository;

import com.rpm.remotepatientmonitoring.model.AiChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface AiChatHistoryRepository extends JpaRepository<AiChatHistory, Long> {
    List<AiChatHistory> findTop20ByAccountIdOrderByCreatedAtDesc(Integer accountId);
    List<AiChatHistory> findTop20ByAccountIdAndPatientIdIsNullOrderByCreatedAtDesc(Integer accountId);
    List<AiChatHistory> findTop20ByPatientIdOrderByCreatedAtDesc(Integer patientId);

    @Modifying
    @Transactional
    @Query("DELETE FROM AiChatHistory h WHERE h.patientId = :patientId")
    void deleteByPatientId(@Param("patientId") Integer patientId);

    @Modifying
    @Transactional
    @Query("DELETE FROM AiChatHistory h WHERE h.accountId = :accountId AND h.patientId IS NULL")
    void deleteByAccountIdAndPatientIdIsNull(@Param("accountId") Integer accountId);
}
