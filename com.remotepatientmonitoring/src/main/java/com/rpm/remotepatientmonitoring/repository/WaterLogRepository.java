package com.rpm.remotepatientmonitoring.repository;

import com.rpm.remotepatientmonitoring.model.WaterLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WaterLogRepository extends JpaRepository<WaterLog, Integer> {
    Optional<WaterLog> findByPatientIdAndLogDate(Integer patientId, LocalDate date);
    
    List<WaterLog> findByPatientIdAndLogDateGreaterThanEqualOrderByLogDateAsc(Integer patientId, LocalDate startDate);
}
