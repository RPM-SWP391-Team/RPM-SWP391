package com.rpm.remotepatientmonitoring.repository;

import com.rpm.remotepatientmonitoring.model.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface HospitalRepository extends JpaRepository<Hospital, Integer> {
    Optional<Hospital> findByHospitalCode(String hospitalCode);


    @org.springframework.data.jpa.repository.Query(value = "EXEC sp_get_hospital_overview @hospital_id = :hospitalId", nativeQuery = true)
    java.util.List<Object[]> getHospitalOverviewRaw(@org.springframework.data.repository.query.Param("hospitalId") Integer hospitalId);
}
