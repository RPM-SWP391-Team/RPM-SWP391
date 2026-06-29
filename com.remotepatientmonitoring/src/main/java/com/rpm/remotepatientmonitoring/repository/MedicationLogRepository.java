package com.rpm.remotepatientmonitoring.repository;

import com.rpm.remotepatientmonitoring.model.MedicationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicationLogRepository extends JpaRepository<MedicationLog, Integer> {
    Optional<MedicationLog> findByPatientMedicationIdAndLogDate(Integer medicationId, LocalDate date);
    
    List<MedicationLog> findByPatientMedicationPatientIdAndLogDateGreaterThanEqualOrderByLogDateAsc(Integer patientId, LocalDate startDate);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(ml) FROM MedicationLog ml WHERE ml.patientMedication.patient.hospital.id = :hospitalId AND ml.isTaken = true AND ml.logDate >= :startDate")
    long countTakenMedicationLogs(@org.springframework.data.repository.query.Param("hospitalId") Integer hospitalId, @org.springframework.data.repository.query.Param("startDate") java.time.LocalDate startDate);
    List<MedicationLog> findByPatientMedicationPatientIdOrderByLogDateAsc(Integer patientId);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(ml) FROM MedicationLog ml WHERE ml.patientMedication.patient.hospital.id = :hospitalId AND ml.isTaken = true AND ml.logDate BETWEEN :startDate AND :endDate")
    long countTakenMedicationLogs(@org.springframework.data.repository.query.Param("hospitalId") Integer hospitalId, @org.springframework.data.repository.query.Param("startDate") java.time.LocalDate startDate, @org.springframework.data.repository.query.Param("endDate") java.time.LocalDate endDate);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(ml) FROM MedicationLog ml WHERE ml.patientMedication.patient.hospital.id = :hospitalId AND ml.logDate BETWEEN :startDate AND :endDate")
    long countTotalMedicationLogs(@org.springframework.data.repository.query.Param("hospitalId") Integer hospitalId, @org.springframework.data.repository.query.Param("startDate") java.time.LocalDate startDate, @org.springframework.data.repository.query.Param("endDate") java.time.LocalDate endDate);
}
