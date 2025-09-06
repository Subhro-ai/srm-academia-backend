package com.portal.academia_portal.dto;

import java.util.List;

public class Month {
    private String month;
    private List<DayEvent> days;

    public Month(String month, List<DayEvent> days) {
        this.month = month;
        this.days = days;
    }

    // Getters and Setters
    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public List<DayEvent> getDays() {
        return days;
    }

    public void setDays(List<DayEvent> days) {
        this.days = days;
    }
}
