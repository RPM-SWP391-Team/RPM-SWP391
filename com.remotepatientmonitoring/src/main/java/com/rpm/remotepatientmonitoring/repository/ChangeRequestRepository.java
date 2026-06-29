package com.rpm.remotepatientmonitoring.repository;

import com.rpm.remotepatientmonitoring.model.ChangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChangeRequestRepository extends JpaRepository<ChangeRequest, Integer> {
    List<ChangeRequest> findByPatientIdOrderByCreatedAtDesc(Integer patientId);
    List<ChangeRequest> findByDoctorIdOrderByCreatedAtDesc(Integer doctorId);
    List<ChangeRequest> findByDoctorIdAndStatusOrderByCreatedAtDesc(Integer doctorId, String status);
    List<ChangeRequest> findByDoctorIdAndStatusNotOrderByCreatedAtDesc(Integer doctorId, String status);
}
