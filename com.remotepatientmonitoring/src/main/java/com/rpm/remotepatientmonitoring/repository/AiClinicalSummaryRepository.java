package com.rpm.remotepatientmonitoring.repository;

import com.rpm.remotepatientmonitoring.model.AiClinicalSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiClinicalSummaryRepository extends JpaRepository<AiClinicalSummary, Long> {
    List<AiClinicalSummary> findByPatientIdOrderByCreatedAtDesc(Integer patientId);
    Optional<AiClinicalSummary> findFirstByPatientIdOrderByCreatedAtDesc(Integer patientId);
}
