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
public class ExerciseDetailDTO {
    private String exerciseType;
    private Integer durationMinutes;
    private Integer caloriesBurned;
    private LocalDateTime logTime;
}
