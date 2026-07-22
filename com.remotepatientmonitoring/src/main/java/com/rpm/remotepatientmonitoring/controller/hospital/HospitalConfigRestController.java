package com.rpm.remotepatientmonitoring.controller.hospital;

import com.rpm.remotepatientmonitoring.model.ExerciseGuideline;
import com.rpm.remotepatientmonitoring.model.FoodDictionary;
import com.rpm.remotepatientmonitoring.service.hospital.HospitalConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hospital")
public class HospitalConfigRestController {

    private final Integer HARDCODED_HOSPITAL_ID = 1;

    @Autowired
    private HospitalConfigService configService;

    // ==========================================
    // KHUYẾN NGHỊ TẬP LUYỆN
    // ==========================================

    @GetMapping("/exercise-guidelines")
    public ResponseEntity<List<ExerciseGuideline>> getGuidelines() {
        try {
            List<ExerciseGuideline> list = configService.getExerciseGuidelines(HARDCODED_HOSPITAL_ID);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/exercise-guidelines")
    public ResponseEntity<?> createGuideline(@RequestBody GuidelineRequest req) {
        try {
            ExerciseGuideline created = configService.addExerciseGuideline(
                    HARDCODED_HOSPITAL_ID,
                    req.getDiseaseProfileId(),
                    req.getTitle(),
                    req.getRecommendedContent(),
                    req.getAvoidContent()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    @PutMapping("/exercise-guidelines/{id}")
    public ResponseEntity<?> updateGuideline(@PathVariable("id") Integer id, @RequestBody GuidelineRequest req) {
        try {
            ExerciseGuideline updated = configService.editExerciseGuideline(
                    id,
                    req.getDiseaseProfileId(),
                    req.getTitle(),
                    req.getRecommendedContent(),
                    req.getAvoidContent()
            );
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    @DeleteMapping("/exercise-guidelines/{id}")
    public ResponseEntity<?> deleteGuideline(@PathVariable("id") Integer id) {
        try {
            configService.deleteExerciseGuideline(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    // ==========================================
    // DANH MỤC THỰC PHẨM
    // ==========================================

    @GetMapping("/foods")
    public ResponseEntity<Page<FoodDictionary>> getFoods(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<FoodDictionary> result = configService.searchFoods(search, pageable);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/foods/{id}/diet-logs-count")
    public ResponseEntity<Long> getDietLogsCount(@PathVariable("id") Integer id) {
        try {
            long count = configService.getDietLogsCountByFoodId(id);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(0L);
        }
    }

    @PostMapping("/foods")
    public ResponseEntity<?> createFood(@RequestBody FoodDictionary food) {
        try {
            FoodDictionary created = configService.addFood(food);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    @PutMapping("/foods/{id}")
    public ResponseEntity<?> updateFood(@PathVariable("id") Integer id, @RequestBody FoodDictionary food) {
        try {
            FoodDictionary updated = configService.editFood(id, food);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    @jakarta.transaction.Transactional
    @PatchMapping("/foods/{id}/toggle-active")
    public ResponseEntity<?> toggleFoodActive(@PathVariable("id") Integer id) {
        try {
            FoodDictionary updated = configService.toggleFoodActive(id);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    // DTO Helper for Request Mapping
    public static class GuidelineRequest {
        private Integer diseaseProfileId;
        private String title;
        private String recommendedContent;
        private String avoidContent;

        public Integer getDiseaseProfileId() {
            return diseaseProfileId;
        }

        public void setDiseaseProfileId(Integer diseaseProfileId) {
            this.diseaseProfileId = diseaseProfileId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getRecommendedContent() {
            return recommendedContent;
        }

        public void setRecommendedContent(String recommendedContent) {
            this.recommendedContent = recommendedContent;
        }

        public String getAvoidContent() {
            return avoidContent;
        }

        public void setAvoidContent(String avoidContent) {
            this.avoidContent = avoidContent;
        }
    }
}
