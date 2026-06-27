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

    // Helper getters to compute values dynamically from FoodDictionary for UI templates
    public String getFoodName() {
        if (food != null) {
            return food.getFoodName();
        }
        return "";
    }

    public Integer getCalories() {
        if (food != null && food.getEnergyKcal() != null) {
            double c = (food.getEnergyKcal() * quantityG) / 100.0;
            return (int) c;
        }
        return 0;
    }

    public Double getSaltG() {
        if (food != null && food.getAshG() != null) {
            double s = (food.getAshG().doubleValue() * quantityG) / 100.0;
            return Math.round(s * 100.0) / 100.0;
        }
        return 0.0;
    }

    public Double getFiberG() {
        if (food != null && food.getCellulozaG() != null) {
            double f = (food.getCellulozaG().doubleValue() * quantityG) / 100.0;
            return Math.round(f * 100.0) / 100.0;
        }
        return 0.0;
    }
}
