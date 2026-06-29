package com.rpm.remotepatientmonitoring.repository;

import com.rpm.remotepatientmonitoring.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByPatientIdOrderByCreatedAtDesc(Integer patientId);
    long countByPatientIdAndIsReadFalse(Integer patientId);

    List<Notification> findTop5ByDoctorIdOrderByCreatedAtDesc(Integer doctorId);
    long countByDoctorIdAndIsReadFalse(Integer doctorId);
}
