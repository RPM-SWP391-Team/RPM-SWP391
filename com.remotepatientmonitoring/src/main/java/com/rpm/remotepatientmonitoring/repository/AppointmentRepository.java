package com.rpm.remotepatientmonitoring.repository;

import com.rpm.remotepatientmonitoring.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {
    List<Appointment> findByPatientIdOrderByAppointmentTimeDesc(Integer patientId);
    List<Appointment> findByDoctorIdOrderByAppointmentTimeDesc(Integer doctorId);

    @org.springframework.data.jpa.repository.Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND (:patientName IS NULL OR :patientName = '' OR LOWER(a.patient.fullName) LIKE LOWER(CONCAT('%', :patientName, '%')) OR LOWER(a.patient.patientCode) LIKE LOWER(CONCAT('%', :patientName, '%'))) AND a.appointmentTime >= :now ORDER BY CASE WHEN a.status = 'PENDING' THEN 0 ELSE 1 END ASC, a.appointmentTime ASC")
    org.springframework.data.domain.Page<Appointment> findUpcoming(@org.springframework.data.repository.query.Param("doctorId") Integer doctorId, @org.springframework.data.repository.query.Param("patientName") String patientName, @org.springframework.data.repository.query.Param("now") java.time.LocalDateTime now, org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND (:patientName IS NULL OR :patientName = '' OR LOWER(a.patient.fullName) LIKE LOWER(CONCAT('%', :patientName, '%')) OR LOWER(a.patient.patientCode) LIKE LOWER(CONCAT('%', :patientName, '%'))) AND a.appointmentTime < :now ORDER BY CASE WHEN a.status = 'PENDING' THEN 0 ELSE 1 END ASC, a.appointmentTime DESC")
    org.springframework.data.domain.Page<Appointment> findHistory(@org.springframework.data.repository.query.Param("doctorId") Integer doctorId, @org.springframework.data.repository.query.Param("patientName") String patientName, @org.springframework.data.repository.query.Param("now") java.time.LocalDateTime now, org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND (:patientName IS NULL OR :patientName = '' OR LOWER(a.patient.fullName) LIKE LOWER(CONCAT('%', :patientName, '%')) OR LOWER(a.patient.patientCode) LIKE LOWER(CONCAT('%', :patientName, '%'))) AND a.appointmentTime >= :start AND a.appointmentTime <= :end AND a.appointmentTime >= :now ORDER BY CASE WHEN a.status = 'PENDING' THEN 0 ELSE 1 END ASC, a.appointmentTime ASC")
    org.springframework.data.domain.Page<Appointment> findUpcomingByDate(@org.springframework.data.repository.query.Param("doctorId") Integer doctorId, @org.springframework.data.repository.query.Param("patientName") String patientName, @org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start, @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end, @org.springframework.data.repository.query.Param("now") java.time.LocalDateTime now, org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND (:patientName IS NULL OR :patientName = '' OR LOWER(a.patient.fullName) LIKE LOWER(CONCAT('%', :patientName, '%')) OR LOWER(a.patient.patientCode) LIKE LOWER(CONCAT('%', :patientName, '%'))) AND a.appointmentTime >= :start AND a.appointmentTime <= :end AND a.appointmentTime < :now ORDER BY CASE WHEN a.status = 'PENDING' THEN 0 ELSE 1 END ASC, a.appointmentTime DESC")
    org.springframework.data.domain.Page<Appointment> findHistoryByDate(@org.springframework.data.repository.query.Param("doctorId") Integer doctorId, @org.springframework.data.repository.query.Param("patientName") String patientName, @org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start, @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end, @org.springframework.data.repository.query.Param("now") java.time.LocalDateTime now, org.springframework.data.domain.Pageable pageable);
    
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(a) FROM Appointment a WHERE a.doctor.id = :doctorId AND (:patientName IS NULL OR :patientName = '' OR LOWER(a.patient.fullName) LIKE LOWER(CONCAT('%', :patientName, '%')) OR LOWER(a.patient.patientCode) LIKE LOWER(CONCAT('%', :patientName, '%'))) AND a.appointmentTime >= :now")
    long countUpcoming(@org.springframework.data.repository.query.Param("doctorId") Integer doctorId, @org.springframework.data.repository.query.Param("patientName") String patientName, @org.springframework.data.repository.query.Param("now") java.time.LocalDateTime now);

    // Bổ sung hàm này để phục vụ luồng vô hiệu hóa bác sĩ
    List<Appointment> findByPatientIdAndDoctorIdAndStatusIn(Integer patientId, Integer doctorId, List<String> statuses);
}
