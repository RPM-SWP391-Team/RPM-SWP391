package com.rpm.remotepatientmonitoring.dto;

public class AiSearchResult {
    private String source;
    private String path;
    private String text;
    private String citation;
    private String pdf_url;
    private String pdfUrl;

    public AiSearchResult() {}

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCitation() {
        return citation;
    }

    public void setCitation(String citation) {
        this.citation = citation;
    }

    public String getPdf_url() {
        return pdf_url != null ? pdf_url : pdfUrl;
    }

    public void setPdf_url(String pdf_url) {
        this.pdf_url = pdf_url;
    }

    public String getPdfUrl() {
        return pdfUrl != null ? pdfUrl : pdf_url;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }
}
