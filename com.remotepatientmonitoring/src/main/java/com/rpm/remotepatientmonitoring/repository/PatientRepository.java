package com.rpm.remotepatientmonitoring.repository;

import com.rpm.remotepatientmonitoring.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Integer> {
    Optional<Patient> findByPatientCode(String patientCode);
    Optional<Patient> findByAccountId(Integer accountId);
    Optional<Patient> findByPhone(String phone);
    List<Patient> findByDoctorId(Integer doctorId);
    List<Patient> findByHospitalId(Integer hospitalId);
    // Tìm kiếm toàn bộ bệnh nhân thực tế đang điều trị của một bác sĩ cụ thể
    List<Patient> findByDoctorIdAndIsActiveTrue(Integer doctorId);
}
