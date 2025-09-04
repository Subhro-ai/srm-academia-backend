package com.portal.academia_portal.dto;

import java.util.List;

public class MarkDetail {
    private String course;
    private String category;
    private List<Mark> marks;

    public String getCourse() {
        return course;
    }

    public void printDetails() {
        System.out.println("Course: " + course);
        System.out.println("Category: " + category);
        System.out.println("Marks:");
        if (marks != null) {
            System.out.println("PRINTING MARKS");
            for (Mark mark : marks) {
                System.out.println("  - " + mark.getObtained() + ": " + mark.getMaxMark());
            }
        } else {
            System.out.println("  No marks available.");
        }
        System.out.println("------------------------------");
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<Mark> getMarks() {
        return marks;
    }

    public void setMarks(List<Mark> marks) {
        this.marks = marks;
    }
}
