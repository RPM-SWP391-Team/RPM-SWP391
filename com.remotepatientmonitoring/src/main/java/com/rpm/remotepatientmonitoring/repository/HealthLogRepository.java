package com.rpm.remotepatientmonitoring.repository;

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

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(DISTINCT hl.patient.id) FROM DailyHealthLog hl WHERE hl.patient.hospital.id = :hospitalId AND hl.patient.status = 'TREATING' AND hl.logDate = :today")
    long countPatientsWithLogsToday(@org.springframework.data.repository.query.Param("hospitalId") Integer hospitalId, @org.springframework.data.repository.query.Param("today") java.time.LocalDate today);
}

