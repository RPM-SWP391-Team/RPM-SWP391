package com.rpm.remotepatientmonitoring.dto;

import java.util.List;

public class AiSearchResponse {
    private List<AiSearchResult> results;
    private int total_found;
    private double latency_seconds;

    public AiSearchResponse() {}

    public List<AiSearchResult> getResults() {
        return results;
    }

    public void setResults(List<AiSearchResult> results) {
        this.results = results;
    }

    public int getTotal_found() {
        return total_found;
    }

    public void setTotal_found(int total_found) {
        this.total_found = total_found;
    }

    public double getLatency_seconds() {
        return latency_seconds;
    }

    public void setLatency_seconds(double latency_seconds) {
        this.latency_seconds = latency_seconds;
    }
}
