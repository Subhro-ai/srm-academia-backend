package com.portal.academia_portal.dto;

import java.util.List;


public class DaySchedule {

    private String dayOrder;
    private List<CourseSlot> classes;

    // Getters and Setters

    public String getDayOrder() {
        return dayOrder;
    }

    public void setDayOrder(String dayOrder) {
        this.dayOrder = dayOrder;
    }

    public List<CourseSlot> getClasses() {
        return classes;
    }

    public void setClasses(List<CourseSlot> classes) {
        this.classes = classes;
    }
}
