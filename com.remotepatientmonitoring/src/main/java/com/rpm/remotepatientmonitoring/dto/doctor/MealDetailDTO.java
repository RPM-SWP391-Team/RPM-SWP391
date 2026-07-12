package com.rpm.remotepatientmonitoring.dto.doctor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealDetailDTO {
    private String mealType;
    private String foodName;
    private Integer quantity;
    private Integer totalCalories;
    private LocalDateTime logTime;
}
