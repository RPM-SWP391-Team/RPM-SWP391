package com.rpm.remotepatientmonitoring.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "disease_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiseaseProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "profile_code", nullable = false, unique = true, length = 20)
    private String profileCode;

    @Column(name = "profile_name", nullable = false, length = 100)
    private String profileName;

    @Column(length = 500)
    private String description;

    @Column(name = "requires_bp_input", nullable = false)
    @Builder.Default
    private Boolean requiresBpInput = false;

    @Column(name = "requires_glucose_input", nullable = false)
    @Builder.Default
    private Boolean requiresGlucoseInput = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getProfileCode() { return profileCode; }
    public void setProfileCode(String profileCode) { this.profileCode = profileCode; }
    public String getProfileName() { return profileName; }
    public void setProfileName(String profileName) { this.profileName = profileName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Boolean getRequiresBpInput() { return requiresBpInput; }
    public void setRequiresBpInput(Boolean requiresBpInput) { this.requiresBpInput = requiresBpInput; }
    public Boolean getRequiresGlucoseInput() { return requiresGlucoseInput; }
    public void setRequiresGlucoseInput(Boolean requiresGlucoseInput) { this.requiresGlucoseInput = requiresGlucoseInput; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}