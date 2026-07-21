package com.rpm.remotepatientmonitoring.dto;

public class PatientBpStats {
    private Double averageSbp;
    private Double averageDbp;
    private Double morningAvgSbp;
    private Double morningAvgDbp;
    private Double eveningAvgSbp;
    private Double eveningAvgDbp;
    private Integer highestSbp;
    private Integer highestDbp;
    private Integer lowestSbp;
    private Integer lowestDbp;
    private Integer highBpCount;
    private Integer veryHighBpCount;
    private Integer totalReadings;

    public PatientBpStats() {}

    public Double getAverageSbp() { return averageSbp; }
    public void setAverageSbp(Double averageSbp) { this.averageSbp = averageSbp; }

    public Double getAverageDbp() { return averageDbp; }
    public void setAverageDbp(Double averageDbp) { this.averageDbp = averageDbp; }

    public Double getMorningAvgSbp() { return morningAvgSbp; }
    public void setMorningAvgSbp(Double morningAvgSbp) { this.morningAvgSbp = morningAvgSbp; }

    public Double getMorningAvgDbp() { return morningAvgDbp; }
    public void setMorningAvgDbp(Double morningAvgDbp) { this.morningAvgDbp = morningAvgDbp; }

    public Double getEveningAvgSbp() { return eveningAvgSbp; }
    public void setEveningAvgSbp(Double eveningAvgSbp) { this.eveningAvgSbp = eveningAvgSbp; }

    public Double getEveningAvgDbp() { return eveningAvgDbp; }
    public void setEveningAvgDbp(Double eveningAvgDbp) { this.eveningAvgDbp = eveningAvgDbp; }

    public Integer getHighestSbp() { return highestSbp; }
    public void setHighestSbp(Integer highestSbp) { this.highestSbp = highestSbp; }

    public Integer getHighestDbp() { return highestDbp; }
    public void setHighestDbp(Integer highestDbp) { this.highestDbp = highestDbp; }

    public Integer getLowestSbp() { return lowestSbp; }
    public void setLowestSbp(Integer lowestSbp) { this.lowestSbp = lowestSbp; }

    public Integer getLowestDbp() { return lowestDbp; }
    public void setLowestDbp(Integer lowestDbp) { this.lowestDbp = lowestDbp; }

    public Integer getHighBpCount() { return highBpCount; }
    public void setHighBpCount(Integer highBpCount) { this.highBpCount = highBpCount; }

    public Integer getVeryHighBpCount() { return veryHighBpCount; }
    public void setVeryHighBpCount(Integer veryHighBpCount) { this.veryHighBpCount = veryHighBpCount; }

    public Integer getTotalReadings() { return totalReadings; }
    public void setTotalReadings(Integer totalReadings) { this.totalReadings = totalReadings; }
}
