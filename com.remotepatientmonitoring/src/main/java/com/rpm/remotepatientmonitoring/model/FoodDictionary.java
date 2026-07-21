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

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getFoodCode() { return foodCode; }
    public void setFoodCode(String foodCode) { this.foodCode = foodCode; }
    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }
    public String getEnglishName() { return englishName; }
    public void setEnglishName(String englishName) { this.englishName = englishName; }
    public BigDecimal getWaterG() { return waterG; }
    public void setWaterG(BigDecimal waterG) { this.waterG = waterG; }
    public Integer getEnergyKcal() { return energyKcal; }
    public void setEnergyKcal(Integer energyKcal) { this.energyKcal = energyKcal; }
    public BigDecimal getProteinG() { return proteinG; }
    public void setProteinG(BigDecimal proteinG) { this.proteinG = proteinG; }
    public BigDecimal getLipidG() { return lipidG; }
    public void setLipidG(BigDecimal lipidG) { this.lipidG = lipidG; }
    public BigDecimal getGlucidG() { return glucidG; }
    public void setGlucidG(BigDecimal glucidG) { this.glucidG = glucidG; }
    public BigDecimal getCellulozaG() { return cellulozaG; }
    public void setCellulozaG(BigDecimal cellulozaG) { this.cellulozaG = cellulozaG; }
    public BigDecimal getAshG() { return ashG; }
    public void setAshG(BigDecimal ashG) { this.ashG = ashG; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
