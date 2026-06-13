package com.rpm.remotepatientmonitoring.repository;

import com.rpm.remotepatientmonitoring.model.AlertThreshold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AlertThresholdRepository extends JpaRepository<AlertThreshold, Integer> {
    Optional<AlertThreshold> findByHospitalIdAndScope(Integer hospitalId, String scope);
}
