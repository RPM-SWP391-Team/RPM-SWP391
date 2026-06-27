package com.rpm.remotepatientmonitoring.repository;

import com.rpm.remotepatientmonitoring.model.PatientExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PatientExerciseRepository extends JpaRepository<PatientExercise, Integer> {
    List<PatientExercise> findByPatientIdAndLogDate(Integer patientId, LocalDate logDate);
    
    List<PatientExercise> findByPatientIdAndLogDateGreaterThanEqualOrderByLogDateAsc(Integer patientId, LocalDate startDate);
}
