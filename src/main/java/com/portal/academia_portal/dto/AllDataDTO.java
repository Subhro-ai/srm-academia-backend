package com.portal.academia_portal.dto;

import java.util.List;

public class AllDataDTO {
    private List<AttendanceDetail> attendance;
    private List<MarkDetail> marks;
    private List<DaySchedule> timetable;
    private List<Month> calendar;
    private UserInfo userInfo;

    // Getters and Setters
    public List<AttendanceDetail> getAttendance() {
        return attendance;
    }

    public void setAttendance(List<AttendanceDetail> attendance) {
        this.attendance = attendance;
    }

    public List<MarkDetail> getMarks() {
        return marks;
    }

    public void setMarks(List<MarkDetail> marks) {
        this.marks = marks;
    }

    public List<DaySchedule> getTimetable() {
        return timetable;
    }

    public void setTimetable(List<DaySchedule> timetable) {
        this.timetable = timetable;
    }

    public List<Month> getCalendar() {
        return calendar;
    }

    public void setCalendar(List<Month> calendar) {
        this.calendar = calendar;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }
}

