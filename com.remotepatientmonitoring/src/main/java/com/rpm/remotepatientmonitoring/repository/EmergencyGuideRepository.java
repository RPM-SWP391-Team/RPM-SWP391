package com.rpm.remotepatientmonitoring.repository;

import com.rpm.remotepatientmonitoring.model.EmergencyGuide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmergencyGuideRepository extends JpaRepository<EmergencyGuide, Integer> {
    List<EmergencyGuide> findByHospitalId(Integer hospitalId);
    List<EmergencyGuide> findByHospitalIdAndIsActive(Integer hospitalId, Boolean isActive);
}
