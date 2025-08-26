package com.portal.academia_portal.dto;

public class TotalAttendance {
    private double totalAttendancePercentage;

    public TotalAttendance(double totalAttendancePercentage) {
        this.totalAttendancePercentage = totalAttendancePercentage;
    }

    public double getTotalAttendancePercentage() {
        return totalAttendancePercentage;
    }

    public void setTotalAttendancePercentage(double totalAttendancePercentage) {
        this.totalAttendancePercentage = totalAttendancePercentage;
    }
}