package com.rpm.remotepatientmonitoring.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Entity
@Table(name = "nutrition_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NutritionRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;
    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;
    @Column(name = "max_calories_per_day", nullable = false)
    private Integer maxCaloriesPerDay;
    @Column(name = "max_carbs_g", nullable = false, precision = 7, scale = 2)
    private BigDecimal maxCarbsG;
    @Column(name = "max_salt_g", nullable = false, precision = 6, scale = 2)
    private BigDecimal maxSaltG;
    @Column(name = "min_fiber_g", nullable = false, precision = 6, scale = 2)
    private BigDecimal minFiberG;
    @Column(name = "max_fat_g", precision = 7, scale = 2)
    private BigDecimal maxFatG;
    @Column(name = "min_protein_g", precision = 7, scale = 2)
    private BigDecimal minProteinG;
    @Column(name = "daily_water_ml")
    private Integer dailyWaterMl = 2000;
    @Column(name = "additional_notes", columnDefinition = "NVARCHAR(MAX)")
    private String additionalNotes;
    @Column(name = "is_current", nullable = false)
    private Boolean isCurrent = true;
    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom = LocalDate.now();
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
