package com.portal.academia_portal;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.portal.academia_portal.service.DataService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.portal.academia_portal.dto.AttendanceDetail;
import com.portal.academia_portal.dto.MarkDetail;


@RestController
@RequestMapping("/api/data")
public class DataController {
    private final DataService dataService;
    @Autowired
    public DataController(DataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/attendance")
    public List<AttendanceDetail> getAttendance(@RequestHeader("Cookie") String cookie) {
        return dataService.getAttendance(cookie);
    }

    @GetMapping("/marks")
    public List<MarkDetail> getMarks(@RequestHeader("Cookie") String cookie) {
        return dataService.getMarks(cookie);
    }
    
    
}
