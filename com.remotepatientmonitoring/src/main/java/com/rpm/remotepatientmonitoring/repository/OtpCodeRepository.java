package com.rpm.remotepatientmonitoring.repository;

import com.rpm.remotepatientmonitoring.model.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, Integer> {
    Optional<OtpCode> findByEmailAndOtpCodeAndOtpTypeAndIsUsedFalse(
            String email, String otpCode, String otpType);

    List<OtpCode> findByEmailAndOtpTypeAndIsUsedFalse(String email, String otpType);
}
