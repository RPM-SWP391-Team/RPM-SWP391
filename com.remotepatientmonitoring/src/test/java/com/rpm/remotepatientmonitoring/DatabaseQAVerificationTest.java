package com.rpm.remotepatientmonitoring;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class DatabaseQAVerificationTest {
    @Test
    public void generateHashes() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println("BCRYPT_123456: " + encoder.encode("123456"));
        System.out.println("BCRYPT_12345678: " + encoder.encode("12345678"));
        System.out.println("BCRYPT_admin: " + encoder.encode("admin"));
        System.out.println("BCRYPT_123: " + encoder.encode("123"));
    }
}
