package com.rpm.remotepatientmonitoring.repository;

import com.rpm.remotepatientmonitoring.model.ChangeRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChangeRequestRepository extends JpaRepository<ChangeRequest, Integer> {
    List<ChangeRequest> findByPatientIdOrderByCreatedAtDesc(Integer patientId);
    List<ChangeRequest> findByDoctorIdOrderByCreatedAtDesc(Integer doctorId);
    List<ChangeRequest> findByDoctorIdAndStatusOrderByCreatedAtDesc(Integer doctorId, String status);
    List<ChangeRequest> findByDoctorIdAndStatusNotOrderByCreatedAtDesc(Integer doctorId, String status);

    @org.springframework.data.jpa.repository.Query("SELECT c FROM ChangeRequest c WHERE c.doctor.id = :doctorId AND c.status != 'PENDING' AND c.createdAt BETWEEN :filterStart AND :filterEnd ORDER BY c.createdAt DESC")
    Page<ChangeRequest> findHistoryByDate(@org.springframework.data.repository.query.Param("doctorId") Integer doctorId, @org.springframework.data.repository.query.Param("filterStart") LocalDateTime filterStart, @org.springframework.data.repository.query.Param("filterEnd") LocalDateTime filterEnd, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT c FROM ChangeRequest c WHERE c.doctor.id = :doctorId AND c.status != 'PENDING' ORDER BY c.createdAt DESC")
    Page<ChangeRequest> findHistory(@org.springframework.data.repository.query.Param("doctorId") Integer doctorId, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT c FROM ChangeRequest c WHERE c.doctor.id = :doctorId AND c.status = 'PENDING' AND c.createdAt BETWEEN :filterStart AND :filterEnd ORDER BY c.createdAt DESC")
    Page<ChangeRequest> findPendingByDate(@org.springframework.data.repository.query.Param("doctorId") Integer doctorId, @org.springframework.data.repository.query.Param("filterStart") LocalDateTime filterStart, @org.springframework.data.repository.query.Param("filterEnd") LocalDateTime filterEnd, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT c FROM ChangeRequest c WHERE c.doctor.id = :doctorId AND c.status = 'PENDING' ORDER BY c.createdAt DESC")
    Page<ChangeRequest> findPending(@org.springframework.data.repository.query.Param("doctorId") Integer doctorId, Pageable pageable);
}
