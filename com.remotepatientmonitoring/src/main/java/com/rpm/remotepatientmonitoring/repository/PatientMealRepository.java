package com.rpm.remotepatientmonitoring.repository;

import com.rpm.remotepatientmonitoring.model.PatientMeal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PatientMealRepository extends JpaRepository<PatientMeal, Integer> {
    List<PatientMeal> findByPatientIdAndLogDate(Integer patientId, LocalDate date);
}
