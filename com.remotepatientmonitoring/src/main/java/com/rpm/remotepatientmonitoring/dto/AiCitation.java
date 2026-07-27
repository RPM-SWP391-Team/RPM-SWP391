package com.rpm.remotepatientmonitoring.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiCitation {
    private String file;
    private String text;
    private double score;
}
