package com.rpm.remotepatientmonitoring.repository;

import com.rpm.remotepatientmonitoring.model.PatientMedication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PatientMedicationRepository extends JpaRepository<PatientMedication, Integer> {
    List<PatientMedication> findByPatientIdAndIsActiveTrue(Integer patientId);
}
