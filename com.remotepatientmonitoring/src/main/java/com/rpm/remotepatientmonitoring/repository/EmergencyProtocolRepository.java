package com.rpm.remotepatientmonitoring.repository;

import com.rpm.remotepatientmonitoring.model.EmergencyProtocol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmergencyProtocolRepository extends JpaRepository<EmergencyProtocol, Integer> {
    List<EmergencyProtocol> findByHospitalIdAndIsActiveTrue(Integer hospitalId);
}
