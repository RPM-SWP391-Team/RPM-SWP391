package com.rpm.remotepatientmonitoring.repository;

import com.rpm.remotepatientmonitoring.model.WaterLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WaterLogRepository extends JpaRepository<WaterLog, Integer> {
    
    @Query("SELECT wl FROM WaterLog wl WHERE wl.patient.id = :patientId AND wl.logDate = :date ORDER BY wl.id ASC")
    List<WaterLog> findAllByPatientIdAndLogDate(@Param("patientId") Integer patientId, @Param("date") LocalDate date);
    
    default Optional<WaterLog> findByPatientIdAndLogDate(Integer patientId, LocalDate date) {
        List<WaterLog> logs = findAllByPatientIdAndLogDate(patientId, date);
        if (logs.isEmpty()) {
            return Optional.empty();
        }
        int sum = logs.stream().mapToInt(WaterLog::getAmountMl).sum();
        WaterLog combined = WaterLog.builder()
                .id(logs.get(0).getId())
                .patient(logs.get(0).getPatient())
                .logDate(date)
                .amountMl(sum)
                .loggedAt(logs.get(0).getLoggedAt())
                .build();
        return Optional.of(combined);
    }
    
    List<WaterLog> findByPatientIdAndLogDateGreaterThanEqualOrderByLogDateAsc(Integer patientId, LocalDate startDate);
}
