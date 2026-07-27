package com.rpm.remotepatientmonitoring.repository;

import com.rpm.remotepatientmonitoring.model.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Integer> {
    @Query("SELECT COUNT(a) FROM Alert a WHERE a.doctor.id = :doctorId AND UPPER(a.alertColor) = UPPER(:alertColor) AND a.isResolved = false")
    long countUnresolvedAlertsByColor(@Param("doctorId") Integer doctorId, @Param("alertColor") String alertColor);

    java.util.List<Alert> findByPatientIdAndIsResolvedFalse(Integer patientId);

    @Query("SELECT COUNT(a) FROM Alert a WHERE a.patient.hospital.id = :hospitalId AND (a.alertLevel >= 3 OR UPPER(a.alertColor) IN ('ORANGE', 'RED')) AND a.createdAt BETWEEN :startDate AND :endDate")
    long countLevel3AlertsInRange(@Param("hospitalId") Integer hospitalId, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);

    @Query("SELECT a FROM Alert a WHERE a.patient.hospital.id = :hospitalId AND a.isResolved = false AND UPPER(a.alertColor) = 'RED'")
    java.util.List<Alert> findUnresolvedRedAlertsByHospital(@Param("hospitalId") Integer hospitalId);

    @Query("SELECT a FROM Alert a WHERE a.patient.hospital.id = :hospitalId AND a.isResolved = false AND UPPER(a.alertColor) = 'RED'")
    Page<Alert> findUnresolvedRedAlertsByHospital(@Param("hospitalId") Integer hospitalId, Pageable pageable);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query("DELETE FROM Alert a WHERE a.healthLog.id = :healthLogId")
    void deleteByHealthLogId(@Param("healthLogId") Integer healthLogId);

    java.util.Optional<Alert> findFirstByPatientIdAndIsResolvedTrueAndResolutionNotesIsNotNullOrderByResolvedAtDesc(Integer patientId);
}
