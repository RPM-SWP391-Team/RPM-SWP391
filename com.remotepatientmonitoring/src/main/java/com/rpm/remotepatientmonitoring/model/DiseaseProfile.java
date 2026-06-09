package com.rpm.remotepatientmonitoring.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "disease_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
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
    private Boolean requiresBpInput = false;

    @Column(name = "requires_glucose_input", nullable = false)
    private Boolean requiresGlucoseInput = false;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}