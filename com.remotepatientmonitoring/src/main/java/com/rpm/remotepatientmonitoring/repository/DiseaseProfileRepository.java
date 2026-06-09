package com.rpm.remotepatientmonitoring.repository;

import com.rpm.remotepatientmonitoring.model.DiseaseProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DiseaseProfileRepository extends JpaRepository<DiseaseProfile, Integer> {
    Optional<DiseaseProfile> findByProfileCode(String profileCode);
}
