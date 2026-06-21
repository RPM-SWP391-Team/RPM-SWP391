package com.rpm.remotepatientmonitoring.repository.patient;

import com.rpm.remotepatientmonitoring.model.DailyHealthLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HealthLogRepository extends JpaRepository<DailyHealthLog, Integer> {
    List<DailyHealthLog> findByPatientIdAndLogDate(Integer patientId, LocalDate logDate);
    List<DailyHealthLog> findByPatientIdAndLogDateGreaterThanEqualOrderByLogDateAsc(Integer patientId, LocalDate date);
    Optional<DailyHealthLog> findFirstByPatientIdOrderByLogTimeDesc(Integer patientId);
}

