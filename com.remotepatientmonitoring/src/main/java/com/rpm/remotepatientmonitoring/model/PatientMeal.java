package com.rpm.remotepatientmonitoring.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patient_meals")
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

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(name = "meal_type", nullable = false, length = 50)
    private String mealType; // Bữa Sáng, Bữa Trưa, Bữa Tối, Bữa Phụ

    @Column(name = "food_name", nullable = false, length = 500)
    private String foodName;

    @Column(name = "calories", nullable = false)
    private Integer calories; // kcal

    @Column(name = "salt_g", nullable = false)
    private Double saltG; // grams

    @Column(name = "fiber_g", nullable = false)
    private Double fiberG; // grams

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
