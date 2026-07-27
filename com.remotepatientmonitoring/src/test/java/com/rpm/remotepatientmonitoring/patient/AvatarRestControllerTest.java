package com.rpm.remotepatientmonitoring.patient;

import com.rpm.remotepatientmonitoring.config.CustomUserDetails;
import com.rpm.remotepatientmonitoring.config.MockBeansConfig;
import com.rpm.remotepatientmonitoring.controller.AvatarRestController;
import com.rpm.remotepatientmonitoring.model.Account;
import com.rpm.remotepatientmonitoring.repository.AccountRepository;
import com.rpm.remotepatientmonitoring.service.AvatarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AvatarRestController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(MockBeansConfig.class)
class AvatarRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AvatarService avatarService;

    @Autowired
    private AccountRepository accountRepository;

    private Account sampleAccount;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        sampleAccount = Account.builder()
                .id(1)
                .email("test@example.com")
                .role("PATIENT")
                .build();

        userDetails = new CustomUserDetails(sampleAccount);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Nested
    @DisplayName("Avatar REST API Endpoint Tests")
    class AvatarRestApi {

        @Test
        @DisplayName("POST /api/account/avatar returns error response when file exceeds 10MB")
        void uploadAvatar_exceeds10MB_returnsBadRequest() throws Exception {
            MockMultipartFile file = new MockMultipartFile("file", "large.png", "image/png", new byte[10]);

            when(accountRepository.findById(1)).thenReturn(Optional.of(sampleAccount));
            when(avatarService.updateAvatar(eq(sampleAccount), any()))
                    .thenThrow(new IllegalArgumentException("Dung lượng ảnh vượt quá giới hạn 10MB."));

            mockMvc.perform(multipart("/api/account/avatar").file(file))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Dung lượng ảnh vượt quá giới hạn 10MB."));
        }

        @Test
        @DisplayName("POST /api/account/avatar returns error response when file extension is invalid")
        void uploadAvatar_invalidExtension_returnsBadRequest() throws Exception {
            MockMultipartFile file = new MockMultipartFile("file", "document.pdf", "application/pdf", "pdf content".getBytes());

            when(accountRepository.findById(1)).thenReturn(Optional.of(sampleAccount));
            when(avatarService.updateAvatar(eq(sampleAccount), any()))
                    .thenThrow(new IllegalArgumentException("Định dạng tệp không hợp lệ. Chỉ chấp nhận các định dạng: jpg, jpeg, png, webp."));

            mockMvc.perform(multipart("/api/account/avatar").file(file))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Định dạng tệp không hợp lệ. Chỉ chấp nhận các định dạng: jpg, jpeg, png, webp."));
        }

        @Test
        @DisplayName("POST /api/account/avatar returns ok and new avatarUrl for valid file")
        void uploadAvatar_validFile_returnsSuccess() throws Exception {
            MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "png content".getBytes());

            when(accountRepository.findById(1)).thenReturn(Optional.of(sampleAccount));
            when(avatarService.updateAvatar(eq(sampleAccount), any()))
                    .thenReturn("/uploads/avatars/avatar_1_123.png");

            mockMvc.perform(multipart("/api/account/avatar").file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.avatarUrl").value("/uploads/avatars/avatar_1_123.png"))
                    .andExpect(jsonPath("$.message").value("Tải ảnh đại diện lên thành công!"));
        }
    }
}
