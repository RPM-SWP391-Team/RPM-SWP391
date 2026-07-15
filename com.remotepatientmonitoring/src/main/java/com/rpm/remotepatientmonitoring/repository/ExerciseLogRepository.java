package com.rpm.remotepatientmonitoring.repository;

import com.rpm.remotepatientmonitoring.model.ExerciseLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExerciseLogRepository extends JpaRepository<ExerciseLog, Integer> {
    List<ExerciseLog> findByPatientIdAndLogDate(Integer patientId, LocalDate logDate);
    List<ExerciseLog> findByPatientIdAndLogDateBetween(Integer patientId, LocalDate startDate, LocalDate endDate);
    List<ExerciseLog> findByPatientIdAndLoggedAtGreaterThanEqual(Integer patientId, LocalDateTime time);
}
