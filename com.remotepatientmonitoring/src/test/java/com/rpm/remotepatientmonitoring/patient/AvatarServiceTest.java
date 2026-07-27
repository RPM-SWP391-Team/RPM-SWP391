package com.rpm.remotepatientmonitoring.patient;

import com.rpm.remotepatientmonitoring.model.Account;
import com.rpm.remotepatientmonitoring.repository.AccountRepository;
import com.rpm.remotepatientmonitoring.service.AvatarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvatarServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AvatarService avatarService;

    private Account sampleAccount;

    @BeforeEach
    void setUp() {
        sampleAccount = Account.builder()
                .id(1)
                .email("test@example.com")
                .role("PATIENT")
                .build();
    }

    @Nested
    @DisplayName("Avatar Upload Validation & Service Logic")
    class AvatarUploadValidation {

        @Test
        @DisplayName("uploadAvatar throws IllegalArgumentException when file size exceeds 10MB")
        void updateAvatar_fileExceeds10MB_throwsException() {
            byte[] largeContent = new byte[10 * 1024 * 1024 + 1]; // 10MB + 1 byte
            MockMultipartFile largeFile = new MockMultipartFile(
                    "file", "large_image.png", "image/png", largeContent
            );

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    avatarService.updateAvatar(sampleAccount, largeFile));

            assertTrue(ex.getMessage().contains("Dung lượng ảnh vượt quá giới hạn 10MB."));
            verifyNoInteractions(accountRepository);
        }

        @Test
        @DisplayName("uploadAvatar throws IllegalArgumentException when file extension is invalid")
        void updateAvatar_invalidExtension_throwsException() {
            MockMultipartFile invalidFile = new MockMultipartFile(
                    "file", "document.pdf", "application/pdf", "pdf content".getBytes()
            );

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    avatarService.updateAvatar(sampleAccount, invalidFile));

            assertTrue(ex.getMessage().contains("Định dạng tệp không hợp lệ"));
            verifyNoInteractions(accountRepository);
        }

        @Test
        @DisplayName("uploadAvatar saves file and updates account avatarUrl for valid PNG file < 10MB")
        void updateAvatar_validPngFile_success() throws Exception {
            MockMultipartFile validFile = new MockMultipartFile(
                    "file", "avatar.png", "image/png", "png image content".getBytes()
            );

            String avatarUrl = avatarService.updateAvatar(sampleAccount, validFile);

            assertNotNull(avatarUrl);
            assertTrue(avatarUrl.startsWith("/uploads/avatars/avatar_1_"));
            assertTrue(avatarUrl.endsWith(".png"));
            assertEquals(avatarUrl, sampleAccount.getAvatarUrl());
            verify(accountRepository).save(sampleAccount);
        }
    }
}
