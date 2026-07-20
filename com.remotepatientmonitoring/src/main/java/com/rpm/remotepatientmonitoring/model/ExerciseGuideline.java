package com.rpm.remotepatientmonitoring.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "exercise_guidelines")
public class ExerciseGuideline {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @ManyToOne
    @JoinColumn(name = "disease_profile_id", nullable = false)
    private DiseaseProfile diseaseProfile;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "recommended_content", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String recommendedContent;

    @Column(name = "avoid_content", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String avoidContent;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public ExerciseGuideline() {
    }

    public ExerciseGuideline(Hospital hospital, DiseaseProfile diseaseProfile, String title, String recommendedContent, String avoidContent, Boolean isActive, LocalDateTime createdAt) {
        this.hospital = hospital;
        this.diseaseProfile = diseaseProfile;
        this.title = title;
        this.recommendedContent = recommendedContent;
        this.avoidContent = avoidContent;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Hospital getHospital() {
        return hospital;
    }

    public void setHospital(Hospital hospital) {
        this.hospital = hospital;
    }

    public DiseaseProfile getDiseaseProfile() {
        return diseaseProfile;
    }

    public void setDiseaseProfile(DiseaseProfile diseaseProfile) {
        this.diseaseProfile = diseaseProfile;
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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
