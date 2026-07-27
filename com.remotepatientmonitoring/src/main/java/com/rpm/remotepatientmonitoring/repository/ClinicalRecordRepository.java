package com.rpm.remotepatientmonitoring.repository;

import com.rpm.remotepatientmonitoring.model.ClinicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClinicalRecordRepository extends JpaRepository<ClinicalRecord, Integer> {
    
    java.util.List<ClinicalRecord> findByPatientIdOrderByExaminationDateDesc(Integer patientId);
    
    java.util.Optional<ClinicalRecord> findFirstByPatientIdOrderByExaminationDateDesc(Integer patientId);
    
    boolean existsByPatientId(Integer patientId);
}
