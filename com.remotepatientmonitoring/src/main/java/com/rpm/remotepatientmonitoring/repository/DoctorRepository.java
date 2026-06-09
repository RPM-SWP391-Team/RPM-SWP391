package com.rpm.remotepatientmonitoring.repository;

import com.rpm.remotepatientmonitoring.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Integer> {
    Optional<Doctor> findByDoctorCode(String doctorCode);
    Optional<Doctor> findByAccountId(Integer accountId);
    Optional<Doctor> findByPhone(String phone);
    List<Doctor> findByHospitalId(Integer hospitalId);
}
