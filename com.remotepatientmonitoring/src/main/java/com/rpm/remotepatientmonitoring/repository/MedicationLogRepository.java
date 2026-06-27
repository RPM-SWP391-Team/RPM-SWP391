package com.rpm.remotepatientmonitoring.repository;

import com.rpm.remotepatientmonitoring.model.MedicationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicationLogRepository extends JpaRepository<MedicationLog, Integer> {
    Optional<MedicationLog> findByPatientMedicationIdAndLogDate(Integer medicationId, LocalDate date);
    
    List<MedicationLog> findByPatientMedicationPatientIdAndLogDateGreaterThanEqualOrderByLogDateAsc(Integer patientId, LocalDate startDate);
}
