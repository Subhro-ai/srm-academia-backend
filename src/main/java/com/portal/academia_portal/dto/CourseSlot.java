package com.portal.academia_portal.dto;

public class CourseSlot {
    private String slot;
    private boolean isClass;
    private String courseTitle;
    private String courseCode;
    private String courseType;
    private String courseCategory;
    private String courseRoomNo;
    private String time;

    // Getters and Setters

    public String getSlot() {
        return slot;
    }

    public void setSlot(String slot) {
        this.slot = slot;
    }

    public boolean isClass() {
        return isClass;
    }

    public void setClass(boolean aClass) {
        isClass = aClass;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseType() {
        return courseType;
    }

    public void setCourseType(String courseType) {
        this.courseType = courseType;
    }

    public String getCourseCategory() {
        return courseCategory;
    }

    public void setCourseCategory(String courseCategory) {
        this.courseCategory = courseCategory;
    }

    public String getCourseRoomNo() {
        return courseRoomNo;
    }

    public void setCourseRoomNo(String courseRoomNo) {
        this.courseRoomNo = courseRoomNo;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
