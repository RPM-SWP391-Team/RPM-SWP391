package com.rpm.remotepatientmonitoring.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "water_logs", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"patient_id", "log_date"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaterLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(name = "amount_ml", nullable = false)
    @Builder.Default
    private Integer amountMl = 0;
}
