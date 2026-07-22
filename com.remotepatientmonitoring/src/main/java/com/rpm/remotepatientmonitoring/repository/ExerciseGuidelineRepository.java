package com.rpm.remotepatientmonitoring.repository;

import com.rpm.remotepatientmonitoring.model.ExerciseGuideline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExerciseGuidelineRepository extends JpaRepository<ExerciseGuideline, Integer> {
    Optional<ExerciseGuideline> findByDiseaseProfileIdAndHospitalIdAndIsActiveTrue(Integer diseaseProfileId, Integer hospitalId);
    List<ExerciseGuideline> findByHospitalIdAndIsActiveTrue(Integer hospitalId);
    List<ExerciseGuideline> findByHospitalId(Integer hospitalId);
}
