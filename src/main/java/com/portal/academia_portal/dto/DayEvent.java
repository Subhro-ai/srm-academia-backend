package com.portal.academia_portal.dto;

public class DayEvent {
    private String date;
    private String day;
    private String event;
    private String dayOrder;

    public DayEvent(String date, String day, String event, String dayOrder) {
        this.date = date;
        this.day = day;
        this.event = event;
        this.dayOrder = dayOrder;
    }

    // Getters and Setters
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getDayOrder() {
        return dayOrder;
    }

    public void setDayOrder(String dayOrder) {
        this.dayOrder = dayOrder;
    }
}
