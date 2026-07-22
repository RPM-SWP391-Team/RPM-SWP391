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

    List<Patient> findAllByPatientCodeIsNotNull();


    @Query("SELECT p FROM Patient p WHERE p.status = 'NEW' AND p.doctor IS NULL AND p.hospital.id = :hospitalId AND (p.phone LIKE %:keyword% OR p.fullName LIKE %:keyword%)")
    List<Patient> searchUnassignedPatients(@Param("hospitalId") Integer hospitalId, @Param("keyword") String keyword);

    // 1. Sửa thành Page: Tìm bệnh nhân theo bác sĩ (có phân trang) và sắp xếp ưu tiên theo cảnh báo Đỏ -> Cam -> Vàng
    @Query("SELECT p FROM Patient p LEFT JOIN Alert a ON a.patient = p AND a.isResolved = false " +
           "WHERE p.doctor.id = :doctorId AND p.isActive = true " +
           "GROUP BY p " +
           "ORDER BY MAX(CASE a.alertColor WHEN 'RED' THEN 4 WHEN 'ORANGE' THEN 3 WHEN 'YELLOW' THEN 2 WHEN 'GREEN' THEN 1 ELSE 0 END) DESC, p.updatedAt DESC")
    Page<Patient> findPatientsSortedByAlerts(@Param("doctorId") Integer doctorId, Pageable pageable);

    // Vẫn giữ lại findByDoctorId cũ nếu cần dùng ở nơi khác
    Page<Patient> findByDoctorId(Integer doctorId, Pageable pageable);

    // 2. Sửa thành Page: Tìm kiếm kết hợp phân trang
    @Query("SELECT p FROM Patient p WHERE p.doctor.id = :doctorId AND (p.fullName LIKE %:keyword% OR p.phone LIKE %:keyword%)")
    Page<Patient> searchPatientsForDoctor(@Param("doctorId") Integer doctorId, @Param("keyword") String keyword, Pageable pageable);

    long countByDoctorId(Integer doctorId);

    @Query("SELECT COUNT(p) FROM Patient p WHERE p.hospital.id = :hospitalId AND p.status = 'TREATING'")
    long countTotalTreatingPatients(@Param("hospitalId") Integer hospitalId);

    List<Patient> findByHospitalIdAndStatus(Integer hospitalId, String status);

    Page<Patient> findByHospitalIdAndStatus(Integer hospitalId, String status, Pageable pageable);

    @Query("SELECT p FROM Patient p WHERE p.hospital.id = :hospitalId AND " +
           "(:search IS NULL OR :search = '' OR p.fullName LIKE %:search% OR p.patientCode LIKE %:search% OR p.phone LIKE %:search%) AND " +
           "(:assignStatus = 'ALL' OR " +
           " (:assignStatus = 'UNASSIGNED' AND p.doctor IS NULL) OR " +
           " (:assignStatus = 'ASSIGNED' AND p.doctor IS NOT NULL)) AND " +
           "(:diseaseCode = 'ALL' OR p.diseaseProfile.profileCode = :diseaseCode)")
    Page<Patient> findPatientsWithFilters(@Param("hospitalId") Integer hospitalId,
                                          @Param("search") String search,
                                          @Param("assignStatus") String assignStatus,
                                          @Param("diseaseCode") String diseaseCode,
                                          Pageable pageable);
}