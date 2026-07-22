package com.rpm.remotepatientmonitoring.repository;

import com.rpm.remotepatientmonitoring.model.TreatmentPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TreatmentPlanRepository extends JpaRepository<TreatmentPlan, Integer> {
    Optional<TreatmentPlan> findByPatientIdAndIsCurrent(Integer patientId, Boolean isCurrent);
    List<TreatmentPlan> findByPatientIdOrderByCreatedAtDesc(Integer patientId);

    @Query("SELECT t FROM TreatmentPlan t WHERE t.patient.id = :patientId AND (:keyword IS NULL OR :keyword = '' OR LOWER(t.medicalOrder) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.additionalNotes) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.doctor.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))) ORDER BY t.createdAt DESC")
    Page<TreatmentPlan> searchPlanHistory(@Param("patientId") Integer patientId, @Param("keyword") String keyword, Pageable pageable);
}
