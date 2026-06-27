package com.rpm.remotepatientmonitoring.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "diet_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientMeal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "food_id", nullable = false)
    private FoodDictionary food;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(name = "meal_type", nullable = false, length = 10)
    private String mealType; // BREAKFAST, LUNCH, DINNER, SNACK

    @Column(name = "quantity_g", nullable = false)
    private Double quantityG; // Lượng thực tế (grams)

    @Column(name = "logged_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // Transient fields to keep compatibility with old controller/UI
    @Transient
    private String foodName;

    @Transient
    private Integer calories;

    @Transient
    private Double saltG;

    @Transient
    private Double fiberG;

    // Custom getters to calculate values dynamically from FoodDictionary
    public String getFoodName() {
        if (food != null) {
            return food.getFoodName();
        }
        return foodName;
    }

    public Integer getCalories() {
        if (food != null && food.getEnergyKcal() != null) {
            double c = (food.getEnergyKcal() * quantityG) / 100.0;
            return (int) c;
        }
        return calories;
    }

    public Double getSaltG() {
        if (food != null && food.getAshG() != null) {
            double s = (food.getAshG().doubleValue() * quantityG) / 100.0;
            // Round to 2 decimal places
            return Math.round(s * 100.0) / 100.0;
        }
        return saltG;
    }

    public Double getFiberG() {
        if (food != null && food.getCellulozaG() != null) {
            double f = (food.getCellulozaG().doubleValue() * quantityG) / 100.0;
            // Round to 2 decimal places
            return Math.round(f * 100.0) / 100.0;
        }
        return fiberG;
    }
}
