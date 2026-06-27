package com.rpm.remotepatientmonitoring.repository;

import com.rpm.remotepatientmonitoring.model.NutritionRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NutritionRuleRepository extends JpaRepository<NutritionRule, Integer> {
    Optional<NutritionRule> findByPatientIdAndIsCurrent(Integer patientId, Boolean isCurrent);
}
