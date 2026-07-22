package com.rpm.remotepatientmonitoring.repository;

import com.rpm.remotepatientmonitoring.model.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Integer> {
    Optional<Doctor> findByDoctorCode(String doctorCode);
    Optional<Doctor> findByAccountId(Integer accountId);
    Optional<Doctor> findByPhone(String phone);
    List<Doctor> findByHospitalId(Integer hospitalId);
    boolean existsByDoctorCode(String doctorCode);
    boolean existsByPhone(String phone);

    // [TÍNH NĂNG 1]: Tự động lọc ẩn bác sĩ quá tải khỏi danh sách tìm kiếm trực tuyến
    @Query("SELECT d FROM Doctor d WHERE d.hospital.id = :hospitalId AND d.isActive = true AND d.currentPatientCount < d.capacityLimit")
    List<Doctor> findAvailableDoctorsByHospital(@Param("hospitalId") Integer hospitalId);

    // [TÍNH NĂNG 2]: Tìm kiếm nhân sự thay thế cùng viện, sắp xếp người rảnh nhất lên đầu bảng
    @Query("SELECT d FROM Doctor d WHERE d.hospital.id = :hospitalId AND d.id <> :currentDoctorId AND d.isActive = true AND d.currentPatientCount < d.capacityLimit ORDER BY (d.capacityLimit - d.currentPatientCount) DESC")
    List<Doctor> findBestReplacementDoctors(@Param("hospitalId") Integer hospitalId, @Param("currentDoctorId") Integer currentDoctorId);

    // ĐÃ SỬA: Bỏ chữ N đứng trước chuỗi 'Cả tiểu đường và huyết áp'
    @Query("SELECT d FROM Doctor d WHERE d.hospital.id = :hospitalId " +
            "AND d.isActive = true " +
            "AND d.currentPatientCount < d.capacityLimit " +
            "AND (d.specialty = :diseaseType OR d.specialty = 'Cả tiểu đường và huyết áp')")
    List<Doctor> findAvailableDoctorsBySpecialty(@Param("hospitalId") Integer hospitalId,
                                                 @Param("diseaseType") String diseaseType);

    // Thêm duy nhất hàm này vào cuối DoctorRepository.java của bạn
    @Query(value = "SELECT TOP 1 doctor_code FROM doctors WHERE doctor_code LIKE 'BS%' ORDER BY id DESC", nativeQuery = true)
    String findLatestDoctorCode();

    // Bộ lọc kép kết hợp Tìm kiếm từ khóa + Lọc Chuyên khoa + Sắp xếp ID giảm dần (mới nhất lên đầu)
    // Bộ lọc kép kết hợp Tìm kiếm từ khóa + Lọc Chuyên khoa + Phân trang
    // SỬA TẠI ĐÂY: Chuyển sang Page và đón nhận Pageable từ Service
    @Query("SELECT d FROM Doctor d WHERE " +
            "(:specialty IS NULL OR :specialty = '' OR d.specialty = :specialty) AND " +
            "(:status IS NULL OR :status = '' OR " +
            " (:status = 'ACTIVE' AND d.isActive = true) OR " +
            " (:status = 'INACTIVE' AND d.isActive = false)) AND " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(d.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.doctorCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "CAST(d.id AS string) LIKE CONCAT('%', :keyword, '%'))")
    Page<Doctor> searchAndFilterDoctors(@Param("keyword") String keyword, @Param("specialty") String specialty, @Param("status") String status, Pageable pageable);
    // Tìm kiếm chính xác theo ID (Dùng làm cơ chế dự phòng an toàn)
    List<Doctor> findById(int id);

    // Kiểm tra số điện thoại đã tồn tại ở một bác sĩ khác chưa (loại trừ chính bác sĩ đang sửa)
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM Doctor d WHERE d.phone = :phone AND d.id <> :id")
    boolean existsByPhoneAndIdNot(@Param("phone") String phone, @Param("id") Integer id);

    List<Doctor> findByHospitalIdAndIsActiveTrue(Integer hospitalId);
    Page<Doctor> findByHospitalIdAndIsActiveTrue(Integer hospitalId, Pageable pageable);

    @Query("SELECT d FROM Doctor d WHERE d.hospital.id = :hospitalId " +
           "AND d.id <> :currentDoctorId " +
           "AND d.isActive = true " +
           "AND d.currentPatientCount < d.capacityLimit " +
           "AND (:specialty IS NULL OR :specialty = '' OR d.specialty = :specialty) " +
           "AND (:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(d.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.doctorCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY (d.capacityLimit - d.currentPatientCount) DESC")
    Page<Doctor> searchReplacementDoctors(@Param("hospitalId") Integer hospitalId, 
                                          @Param("currentDoctorId") Integer currentDoctorId,
                                          @Param("keyword") String keyword, 
                                          @Param("specialty") String specialty, 
                                          Pageable pageable);

    @Query("SELECT DISTINCT d.specialty FROM Doctor d WHERE d.hospital.id = :hospitalId AND d.isActive = true AND d.specialty IS NOT NULL")
    List<String> findDistinctSpecialtiesByHospital(@Param("hospitalId") Integer hospitalId);

    @Query("SELECT d FROM Doctor d WHERE d.isActive = true AND d.currentPatientCount < d.capacityLimit")
    List<Doctor> findAllAvailableDoctors();
}