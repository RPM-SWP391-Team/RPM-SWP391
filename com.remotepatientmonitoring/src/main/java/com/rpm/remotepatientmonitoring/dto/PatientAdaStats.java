package com.rpm.remotepatientmonitoring.dto;

public class PatientAdaStats {
    private Double meanGlucose;
    private Double minGlucose;
    private Double maxGlucose;
    private Double tir;
    private Double tar;
    private Double tbr;
    private Double cv;
    private Double gmi;
    private Integer totalReadings;

    public PatientAdaStats() {}

    public Double getMeanGlucose() { return meanGlucose; }
    public void setMeanGlucose(Double meanGlucose) { this.meanGlucose = meanGlucose; }

    public Double getMinGlucose() { return minGlucose; }
    public void setMinGlucose(Double minGlucose) { this.minGlucose = minGlucose; }

    public Double getMaxGlucose() { return maxGlucose; }
    public void setMaxGlucose(Double maxGlucose) { this.maxGlucose = maxGlucose; }

    public Double getTir() { return tir; }
    public void setTir(Double tir) { this.tir = tir; }

    public Double getTar() { return tar; }
    public void setTar(Double tar) { this.tar = tar; }

    public Double getTbr() { return tbr; }
    public void setTbr(Double tbr) { this.tbr = tbr; }

    public Double getCv() { return cv; }
    public void setCv(Double cv) { this.cv = cv; }

    public Double getGmi() { return gmi; }
    public void setGmi(Double gmi) { this.gmi = gmi; }

    public Integer getTotalReadings() { return totalReadings; }
    public void setTotalReadings(Integer totalReadings) { this.totalReadings = totalReadings; }
}
