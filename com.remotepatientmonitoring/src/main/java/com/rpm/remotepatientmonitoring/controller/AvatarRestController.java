package com.rpm.remotepatientmonitoring.controller;

import com.rpm.remotepatientmonitoring.config.CustomUserDetails;
import com.rpm.remotepatientmonitoring.model.Account;
import com.rpm.remotepatientmonitoring.repository.AccountRepository;
import com.rpm.remotepatientmonitoring.service.AvatarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/account")
public class AvatarRestController {

    @Autowired
    private AvatarService avatarService;

    @Autowired
    private AccountRepository accountRepository;

    @PostMapping("/avatar")
    public ResponseEntity<Map<String, Object>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Map<String, Object> response = new HashMap<>();
        if (userDetails == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập để thực hiện tải ảnh đại diện.");
            return ResponseEntity.status(401).body(response);
        }

        try {
            // Load fresh account from db to avoid stale object exception
            Optional<Account> opt = accountRepository.findById(userDetails.getAccount().getId());
            if (!opt.isPresent()) {
                response.put("success", false);
                response.put("message", "Tài khoản không tồn tại.");
                return ResponseEntity.badRequest().body(response);
            }
            Account account = opt.get();
            String newAvatarUrl = avatarService.updateAvatar(account, file);
            
            // Also update the principal details so subsequent requests have correct info
            userDetails.getAccount().setAvatarUrl(newAvatarUrl);
            
            response.put("success", true);
            response.put("avatarUrl", newAvatarUrl);
            response.put("message", "Tải ảnh đại diện lên thành công!");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi máy chủ: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
