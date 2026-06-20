package com.rpm.remotepatientmonitoring.repository;

import com.rpm.remotepatientmonitoring.model.Patient;
import org.springframework.data.domain.Page; // Import Page
import org.springframework.data.domain.Pageable; // Import Pageable
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Integer> {
    Optional<Patient> findByPatientCode(String patientCode);
    Optional<Patient> findByAccountId(Integer accountId);
    Optional<Patient> findByPhone(String phone);
    List<Patient> findByHospitalId(Integer hospitalId);
    // Tìm kiếm toàn bộ bệnh nhân thực tế đang điều trị của một bác sĩ cụ thể
    List<Patient> findByDoctorIdAndIsActiveTrue(Integer doctorId);


    @Query("SELECT p FROM Patient p WHERE p.status = 'NEW' AND p.doctor IS NULL AND p.hospital.id = :hospitalId AND (p.phone LIKE %:keyword% OR p.fullName LIKE %:keyword%)")
    List<Patient> searchUnassignedPatients(@Param("hospitalId") Integer hospitalId, @Param("keyword") String keyword);

    // 1. Sửa thành Page: Tìm bệnh nhân theo bác sĩ (có phân trang)
    Page<Patient> findByDoctorId(Integer doctorId, Pageable pageable);

    // 2. Sửa thành Page: Tìm kiếm kết hợp phân trang
    @Query("SELECT p FROM Patient p WHERE p.doctor.id = :doctorId AND (p.fullName LIKE %:keyword% OR p.phone LIKE %:keyword%)")
    Page<Patient> searchPatientsForDoctor(@Param("doctorId") Integer doctorId, @Param("keyword") String keyword, Pageable pageable);

    long countByDoctorId(Integer doctorId);
}