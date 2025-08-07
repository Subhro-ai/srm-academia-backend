package com.portal.academia_portal.dto;

public class AttendanceDetail {
    private String courseCode;
    private String courseTitle;
    private String courseCategory;
    private String courseFaculty;
    private String courseSlot;
    private String courseAttendance;
    private int courseConducted;
    private int courseAbsent;
    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public String getCourseCategory() {
        return courseCategory;
    }

    public void setCourseCategory(String courseCategory) {
        this.courseCategory = courseCategory;
    }

    public String getCourseFaculty() {
        return courseFaculty;
    }

    public void setCourseFaculty(String courseFaculty) {
        this.courseFaculty = courseFaculty;
    }

    public String getCourseSlot() {
        return courseSlot;
    }

    public void setCourseSlot(String courseSlot) {
        this.courseSlot = courseSlot;
    }

    public String getCourseAttendance() {
        return courseAttendance;
    }

    public void setCourseAttendance(String courseAttendance) {
        this.courseAttendance = courseAttendance;
    }

    public int getCourseConducted() {
        return courseConducted;
    }

    public void setCourseConducted(int courseConducted) {
        this.courseConducted = courseConducted;
    }

    public int getCourseAbsent() {
        return courseAbsent;
    }

    public void setCourseAbsent(int courseAbsent) {
        this.courseAbsent = courseAbsent;
    }
}
