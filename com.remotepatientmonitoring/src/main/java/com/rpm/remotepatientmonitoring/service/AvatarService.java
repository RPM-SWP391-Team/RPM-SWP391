package com.rpm.remotepatientmonitoring.service;

import com.rpm.remotepatientmonitoring.model.Account;
import com.rpm.remotepatientmonitoring.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

@Service
public class AvatarService {

    @Autowired
    private AccountRepository accountRepository;

    private static final long MAX_SIZE = 10 * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "webp");

    @Transactional
    public String updateAvatar(Account account, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn một tệp ảnh để tải lên.");
        }

        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("Dung lượng ảnh vượt quá giới hạn 10MB.");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        }

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Định dạng tệp không hợp lệ. Chỉ chấp nhận các định dạng: jpg, jpeg, png, webp.");
        }

        String projectPath = System.getProperty("user.dir");
        String relativeUploadDir = "/uploads/avatars/";
        
        // Xác định đúng thư mục uploads tùy thuộc vào việc chạy từ thư mục gốc hay thư mục com.remotepatientmonitoring
        File uploadDir = new File(projectPath, "uploads/avatars/");
        if (projectPath.endsWith("RPM-SWP391") && !projectPath.contains("com.remotepatientmonitoring")) {
            uploadDir = new File(projectPath + "/com.remotepatientmonitoring", "uploads/avatars/");
        }

        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // Delete old avatar files if present
        String oldAvatarUrl = account.getAvatarUrl();
        if (oldAvatarUrl != null && oldAvatarUrl.startsWith(relativeUploadDir)) {
            String oldFileName = oldAvatarUrl.substring(oldAvatarUrl.lastIndexOf("/") + 1);
            File oldFile = new File(uploadDir, oldFileName);
            if (oldFile.exists()) {
                oldFile.delete();
            }
        }

        // Save new file
        String newFileName = "avatar_" + account.getId() + "_" + System.currentTimeMillis() + "." + extension;
        Path filePath = uploadDir.toPath().resolve(newFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Update database
        String relativeAvatarUrl = relativeUploadDir + newFileName;
        account.setAvatarUrl(relativeAvatarUrl);
        accountRepository.save(account);

        return relativeAvatarUrl;
    }
}
