package com.rpm.remotepatientmonitoring.config;

import com.rpm.remotepatientmonitoring.model.Account;
import com.rpm.remotepatientmonitoring.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Tự động tạo tài khoản admin ban đầu nếu chưa có trong DB
        if (accountRepository.findByEmail("admin@gmail.com").isEmpty()) {
            Account admin = Account.builder()
                    .email("admin@gmail.com")
                    .passwordHash(passwordEncoder.encode("12345678"))
                    .role("HOSPITAL_ADMIN")
                    .isEmailVerified(true)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            accountRepository.save(admin);
            System.out.println("=================================================");
            System.out.println("ĐÃ TỰ ĐỘNG KHỞI TẠO TÀI KHOẢN ADMIN BAN ĐẦU:");
            System.out.println("Email: admin@gmail.com");
            System.out.println("Mật khẩu: 12345678");
            System.out.println("=================================================");
        }
    }
}
