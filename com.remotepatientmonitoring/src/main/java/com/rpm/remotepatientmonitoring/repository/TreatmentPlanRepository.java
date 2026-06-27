package com.rpm.remotepatientmonitoring.repository;

import com.rpm.remotepatientmonitoring.model.TreatmentPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TreatmentPlanRepository extends JpaRepository<TreatmentPlan, Integer> {
    Optional<TreatmentPlan> findByPatientIdAndIsCurrent(Integer patientId, Boolean isCurrent);
    List<TreatmentPlan> findByPatientIdOrderByCreatedAtDesc(Integer patientId);
}
