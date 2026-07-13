package com.rpm.remotepatientmonitoring.repository;

import com.rpm.remotepatientmonitoring.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    @org.springframework.data.jpa.repository.Query("SELECT n FROM Notification n WHERE n.patient.id = :patientId AND n.recipientType = 'PATIENT' ORDER BY n.createdAt DESC")
    List<Notification> findByPatientIdOrderByCreatedAtDesc(@org.springframework.data.repository.query.Param("patientId") Integer patientId);
    
    @org.springframework.data.jpa.repository.Query("SELECT count(n) FROM Notification n WHERE n.patient.id = :patientId AND n.recipientType = 'PATIENT' AND n.isRead = false")
    long countByPatientIdAndIsReadFalse(@org.springframework.data.repository.query.Param("patientId") Integer patientId);

    @org.springframework.data.jpa.repository.Query("SELECT count(n) FROM Notification n WHERE n.doctor.id = :doctorId AND n.recipientType = 'DOCTOR' AND n.isRead = false")
    long countByDoctorIdAndIsReadFalse(@org.springframework.data.repository.query.Param("doctorId") Integer doctorId);

    @org.springframework.data.jpa.repository.Query("SELECT n FROM Notification n WHERE n.doctor.id = :doctorId AND n.recipientType = 'DOCTOR' ORDER BY n.createdAt DESC")
    org.springframework.data.domain.Page<Notification> findByDoctorIdAndRecipientTypeOrderByCreatedAtDesc(
            @org.springframework.data.repository.query.Param("doctorId") Integer doctorId,
            org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT n FROM Notification n WHERE n.doctor.id = :doctorId AND n.recipientType = 'DOCTOR' AND n.createdAt >= :startDate AND n.createdAt <= :endDate ORDER BY n.createdAt DESC")
    org.springframework.data.domain.Page<Notification> findByDoctorIdAndRecipientTypeAndCreatedAtBetweenOrderByCreatedAtDesc(
            @org.springframework.data.repository.query.Param("doctorId") Integer doctorId,
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDateTime startDate,
            @org.springframework.data.repository.query.Param("endDate") java.time.LocalDateTime endDate,
            org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query("UPDATE Notification n SET n.isRead = true WHERE n.doctor.id = :doctorId AND n.recipientType = 'DOCTOR' AND n.isRead = false")
    void markAllAsReadByDoctorId(@org.springframework.data.repository.query.Param("doctorId") Integer doctorId);

    List<Notification> findTop5ByDoctorIdOrderByCreatedAtDesc(Integer doctorId);

    @Query("SELECT n FROM Notification n WHERE n.patient.id = :patientId " +
           "AND n.isRead = false " +
           "AND n.notificationType IN ('EXERCISE_REMINDER', 'EXERCISE_STREAK_MILESTONE', 'EXERCISE_BP_REMINDER', 'EXERCISE_INACTIVITY_REMINDER', 'EXERCISE_STREAK_AT_RISK', 'EXERCISE_SHORT_BURST_WARNING') " +
           "ORDER BY n.createdAt DESC")
    List<Notification> findUnreadExerciseNotifications(
            @Param("patientId") Integer patientId
    );
}
