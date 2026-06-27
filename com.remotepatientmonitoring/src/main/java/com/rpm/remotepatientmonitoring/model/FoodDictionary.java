package com.rpm.remotepatientmonitoring.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "foods_dictionary")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodDictionary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "food_code", nullable = false, unique = true, length = 50)
    private String foodCode;

    @Column(name = "food_name", nullable = false, length = 255)
    private String foodName;

    @Column(name = "english_name", length = 255)
    private String englishName;

    @Column(name = "water_g", precision = 5, scale = 2)
    private BigDecimal waterG;

    @Column(name = "energy_kcal")
    private Integer energyKcal;

    @Column(name = "protein_g", precision = 5, scale = 2)
    private BigDecimal proteinG;

    @Column(name = "lipid_g", precision = 5, scale = 2)
    private BigDecimal lipidG;

    @Column(name = "glucid_g", precision = 5, scale = 2)
    private BigDecimal glucidG;

    @Column(name = "celluloza_g", precision = 5, scale = 2)
    private BigDecimal cellulozaG;

    @Column(name = "ash_g", precision = 5, scale = 2)
    private BigDecimal ashG;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
