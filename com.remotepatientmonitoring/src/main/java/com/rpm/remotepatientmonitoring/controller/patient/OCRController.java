package com.rpm.remotepatientmonitoring.controller.patient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@RestController
@RequestMapping("/patient/log/ocr")
public class OCRController {

    @Value("${ocr.python.command:python}")
    private String pythonCommand;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> processOCR(@RequestParam("file") MultipartFile file) {
        Path tempFile = null;
        try {
            // 1. Tạo file tạm để lưu ảnh tải lên
            tempFile = Files.createTempFile("ocr-upload-", ".jpg");
            Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

            // 2. Định nghĩa script Python ocr_service.py (tìm kiếm thông minh ở các thư mục liên quan)
            String scriptPath = "ocr_service.py";
            java.nio.file.Path localScript = java.nio.file.Path.of(scriptPath);
            if (!Files.exists(localScript)) {
                // Thử tìm trong thư mục con nếu IntelliJ chạy từ gốc repo
                java.nio.file.Path alternativeScript = java.nio.file.Path.of("code1", "com.remotepatientmonitoring", "ocr_service.py");
                if (Files.exists(alternativeScript)) {
                    scriptPath = alternativeScript.toAbsolutePath().toString();
                }
            } else {
                scriptPath = localScript.toAbsolutePath().toString();
            }

            // 3. Xây dựng và khởi chạy tiến trình chạy Python
            ProcessBuilder pb = new ProcessBuilder(
                    pythonCommand, 
                    scriptPath, 
                    tempFile.toAbsolutePath().toString()
            );
            pb.redirectErrorStream(false); // Tách biệt stdout và stderr để chỉ lấy JSON từ stdout
            Process process = pb.start();

            // Đọc kết quả JSON từ stdout
            StringBuilder jsonOutput = new StringBuilder();
            try (BufferedReader stdReader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = stdReader.readLine()) != null) {
                    jsonOutput.append(line);
                }
            }

            // Đọc log lỗi từ stderr (nếu có) để ghi nhận phục vụ debug
            StringBuilder errorLog = new StringBuilder();
            try (BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = errReader.readLine()) != null) {
                    errorLog.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            if (errorLog.length() > 0) {
                System.err.println("[OCR Python Stderr]: " + errorLog.toString());
            }

            // 4. Nếu chạy lệnh thành công (exitCode = 0), trả kết quả JSON về cho client
            if (exitCode == 0 && jsonOutput.length() > 0) {
                return ResponseEntity.ok(jsonOutput.toString());
            } else {
                return ResponseEntity.ok("{\"error\": \"Lỗi chạy script OCR hoặc EasyOCR chưa được cài đặt. Vui lòng kiểm tra console logs.\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok("{\"error\": \"Không thể xử lý ảnh: " + e.getMessage() + "\"}");
        } finally {
            // Dọn dẹp file tạm
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (Exception ignored) {}
            }
        }
    }
}
