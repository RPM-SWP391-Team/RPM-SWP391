package com.rpm.remotepatientmonitoring.repository;

import com.rpm.remotepatientmonitoring.model.EmergencyProtocol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmergencyProtocolRepository extends JpaRepository<EmergencyProtocol, Integer> {
    List<EmergencyProtocol> findByHospitalId(Integer hospitalId);
    List<EmergencyProtocol> findByHospitalIdAndIsActiveTrue(Integer hospitalId);
    // Bổ sung hàm này để phục vụ Service (Chặn trùng lặp khi Add và Restore)
    boolean existsByHospitalIdAndConditionTypeAndIsActiveTrue(Integer hospitalId, String conditionType);
}
