package com.rpm.remotepatientmonitoring.repository;

import com.rpm.remotepatientmonitoring.model.DoctorRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRatingRepository extends JpaRepository<DoctorRating, Integer> {
    Optional<DoctorRating> findByAppointmentId(Integer appointmentId);
    boolean existsByAppointmentId(Integer appointmentId);
    List<DoctorRating> findByDoctorId(Integer doctorId);
    List<DoctorRating> findByPatientId(Integer patientId);

    @Query("SELECT COUNT(r) FROM DoctorRating r WHERE r.doctor.id = :doctorId")
    long countByDoctorId(@Param("doctorId") Integer doctorId);

    @Query("SELECT AVG(r.ratingValue) FROM DoctorRating r WHERE r.doctor.id = :doctorId")
    Double getAverageRatingByDoctorId(@Param("doctorId") Integer doctorId);

    Page<DoctorRating> findByRatingValueIn(List<Integer> ratingValues, Pageable pageable);
    Page<DoctorRating> findAll(Pageable pageable);
}
