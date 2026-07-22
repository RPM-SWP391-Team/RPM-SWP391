package com.rpm.remotepatientmonitoring.repository;

import com.rpm.remotepatientmonitoring.model.HospitalAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface HospitalAdminRepository extends JpaRepository<HospitalAdmin, Integer> {
    Optional<HospitalAdmin> findByAccountId(Integer accountId);
    Optional<HospitalAdmin> findByAdminCode(String adminCode);
}
