package com.portal.academia_portal;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin; // Import CookieValue
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.portal.academia_portal.dto.AttendanceDetail;
import com.portal.academia_portal.dto.DaySchedule;
import com.portal.academia_portal.dto.MarkDetail;
import com.portal.academia_portal.dto.TotalAttendance;
import com.portal.academia_portal.dto.UserInfo;
import com.portal.academia_portal.service.DataService;

@CrossOrigin(origins = "http://localhost:4200") // allowCredentials no longer needed here
@RestController
@RequestMapping("/api/data")
public class DataController {
    private final DataService dataService;

    @Autowired
    public DataController(DataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/attendance")
    public List<AttendanceDetail> getAttendance(@RequestHeader("X-Academia-Auth") String cookie) {
        return dataService.getAttendance(cookie);
    }

    @GetMapping("/marks")
    public List<MarkDetail> getMarks(@RequestHeader("X-Academia-Auth") String cookie) {
        return dataService.getMarks(cookie);
    }
    
    @GetMapping("/timetable")
    public List<DaySchedule> getTimetable(@RequestHeader("X-Academia-Auth") String cookie) {
        return dataService.getTimetable(cookie);
    }
    
    @GetMapping("/user-info")
    public UserInfo getUserInfo(@RequestHeader("X-Academia-Auth") String cookie) {
        return dataService.getUserInfo(cookie);
    }

    @GetMapping("/total-attendance")
    public TotalAttendance getTotalAttendance(@RequestHeader("Cookie") String cookie) {
        return dataService.getTotalAttendancePercentage(cookie);
    }
}