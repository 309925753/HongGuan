package com.sk.weichat.bean;

public class Report {

    public Report(int reportId, String reportContent) {
        this.reportId = reportId;
        this.reportContent = reportContent;
    }

    int reportId;
    String reportContent;

    public int getReportId() {
        return reportId;
    }

    public void setReportId(int reportId) {
        this.reportId = reportId;
    }

    public String getReportContent() {
        return reportContent;
    }

    public void setReportContent(String reportContent) {
        this.reportContent = reportContent;
    }
}
