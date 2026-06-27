package com.rpm.remotepatientmonitoring.repository;

import com.rpm.remotepatientmonitoring.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Integer> {
    @Query("SELECT COUNT(a) FROM Alert a WHERE a.doctor.id = :doctorId AND UPPER(a.alertColor) = UPPER(:alertColor) AND a.isResolved = false")
    long countUnresolvedAlertsByColor(@Param("doctorId") Integer doctorId, @Param("alertColor") String alertColor);

    java.util.List<Alert> findByPatientIdAndIsResolvedFalse(Integer patientId);
}
