package com.rpm.remotepatientmonitoring.rating;

import com.rpm.remotepatientmonitoring.config.MockBeansConfig;
import com.rpm.remotepatientmonitoring.controller.RatingRestController;
import com.rpm.remotepatientmonitoring.controller.hospital.HospitalRatingController;
import com.rpm.remotepatientmonitoring.model.AppRating;
import com.rpm.remotepatientmonitoring.model.DoctorRating;
import com.rpm.remotepatientmonitoring.security.WithMockCustomUser;
import com.rpm.remotepatientmonitoring.service.RatingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {RatingRestController.class, HospitalRatingController.class})
@AutoConfigureMockMvc(addFilters = false)
@Import(MockBeansConfig.class)
class RatingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RatingService ratingService;

    @Autowired
    private HospitalRatingController hospitalRatingController;

    private MockMvc standaloneMockMvc;

    @BeforeEach
    void setUpStandalone() {
        reset(ratingService);

        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/views/");
        viewResolver.setSuffix(".jsp");

        standaloneMockMvc = MockMvcBuilders.standaloneSetup(hospitalRatingController)
                .setViewResolvers(viewResolver)
                .build();
    }

    @Nested
    @DisplayName("REST API - Submit Ratings")
    class RestApiSubmit {

        @Test
        @WithMockCustomUser(username = "patient@example.com", role = "PATIENT")
        @DisplayName("POST /api/ratings/doctor returns 200 OK on valid submission")
        void submitDoctorRating_success() throws Exception {
            String jsonRequest = "{\"appointmentId\":100,\"ratingValue\":5,\"comment\":\"Dịch vụ tuyệt vời!\"}";

            mockMvc.perform(post("/api/ratings/doctor")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Đánh giá bác sĩ thành công!"));

            verify(ratingService).submitDoctorRating(eq(100), eq(5), eq("Dịch vụ tuyệt vời!"), any());
        }

        @Test
        @WithMockCustomUser(username = "patient@example.com", role = "PATIENT")
        @DisplayName("POST /api/ratings/doctor returns 400 Bad Request on validation error")
        void submitDoctorRating_validationFailure_returnsBadRequest() throws Exception {
            doThrow(new IllegalArgumentException("Điểm đánh giá phải từ 1 đến 5 sao."))
                    .when(ratingService).submitDoctorRating(any(), any(), any(), any());

            String jsonRequest = "{\"appointmentId\":100,\"ratingValue\":6,\"comment\":\"Rất tốt\"}";

            mockMvc.perform(post("/api/ratings/doctor")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Điểm đánh giá phải từ 1 đến 5 sao."));
        }

        @Test
        @WithMockCustomUser(username = "patient@example.com", role = "PATIENT")
        @DisplayName("POST /api/ratings/app returns 200 OK on valid submission")
        void submitAppRating_success() throws Exception {
            String jsonRequest = "{\"ratingValue\":5,\"comment\":\"Ứng dụng mượt mà!\"}";

            mockMvc.perform(post("/api/ratings/app")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Cảm ơn bạn đã gửi đánh giá ứng dụng!"));

            verify(ratingService).submitAppRating(eq(5), eq("Ứng dụng mượt mà!"), any());
        }

        @Test
        @WithMockCustomUser(username = "admin@example.com", role = "HOSPITAL_ADMIN")
        @DisplayName("POST /api/ratings/app returns 403 Forbidden for HOSPITAL_ADMIN")
        void submitAppRating_adminBlocked_returnsForbidden() throws Exception {
            String jsonRequest = "{\"ratingValue\":5,\"comment\":\"Test\"}";

            mockMvc.perform(post("/api/ratings/app")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("Admin Controller - Rating Management")
    class AdminRatingManagement {

        @Test
        @DisplayName("GET /hospital/ratings renders ratings view with doctor and app ratings lists")
        void showRatings_rendersView() throws Exception {
            when(ratingService.getDoctorRatings(any(), any())).thenReturn(new PageImpl<>(List.of(new DoctorRating())));
            when(ratingService.getAppRatings(any(), any())).thenReturn(new PageImpl<>(List.of(new AppRating())));

            standaloneMockMvc.perform(get("/hospital/ratings"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("hospital/ratings"))
                    .andExpect(model().attributeExists("doctorRatings"))
                    .andExpect(model().attributeExists("appRatings"));
        }

        @Test
        @DisplayName("POST /hospital/ratings/doctor/toggle-hide/{id} toggles hide status and redirects")
        void toggleHideDoctorRating_redirectsWithFlashMessage() throws Exception {
            standaloneMockMvc.perform(post("/hospital/ratings/doctor/toggle-hide/10"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("/hospital/ratings*"))
                    .andExpect(flash().attributeExists("successMessage"));

            verify(ratingService).toggleHideDoctorRating(10);
        }

        @Test
        @DisplayName("POST /hospital/ratings/app/toggle-hide/{id} toggles hide status and redirects")
        void toggleHideAppRating_redirectsWithFlashMessage() throws Exception {
            standaloneMockMvc.perform(post("/hospital/ratings/app/toggle-hide/20"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("/hospital/ratings*"))
                    .andExpect(flash().attributeExists("successMessage"));

            verify(ratingService).toggleHideAppRating(20);
        }
    }
}
