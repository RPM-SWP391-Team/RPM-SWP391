package com.rpm.remotepatientmonitoring.repository;

import com.rpm.remotepatientmonitoring.model.AiChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiChatHistoryRepository extends JpaRepository<AiChatHistory, Long> {
    List<AiChatHistory> findByAccountIdOrderByCreatedAtDesc(Integer accountId);
    List<AiChatHistory> findByPatientIdOrderByCreatedAtDesc(Integer patientId);
}
