package com.rpm.remotepatientmonitoring.repository;

import com.rpm.remotepatientmonitoring.model.Doctor;
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
}