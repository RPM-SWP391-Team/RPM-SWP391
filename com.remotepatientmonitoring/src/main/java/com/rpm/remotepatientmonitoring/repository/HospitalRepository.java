package com.rpm.remotepatientmonitoring.repository;

import com.rpm.remotepatientmonitoring.model.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface HospitalRepository extends JpaRepository<Hospital, Integer> {
    Optional<Hospital> findByHospitalCode(String hospitalCode);
    Optional<Hospital> findByAccountId(Integer accountId);
}
