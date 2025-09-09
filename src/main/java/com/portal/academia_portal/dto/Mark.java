package com.portal.academia_portal.dto;

public class Mark {
    private String exam;
    private double obtained;
    private double maxMark;
    private boolean isPresent;

    public String getExam() {
        return exam;
    }

    public void setExam(String exam) {
        this.exam = exam;
    }

    public double getObtained() {
        return obtained;
    }

    public void setObtained(double obtained) {
        this.obtained = obtained;
    }

    public double getMaxMark() {
        return maxMark;
    }

    public void setMaxMark(double maxMark) {
        this.maxMark = maxMark;
    }


    public boolean isPresent() {
        return isPresent;
    }

    public void setPresent(boolean isPresent) {
        this.isPresent = isPresent;
    }
}
