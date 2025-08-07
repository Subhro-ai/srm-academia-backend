package com.portal.academia_portal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.portal.academia_portal.service.DataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/data")
public class DataController {
    private final DataService dataService;
    @Autowired
    public DataController(DataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/attendance")
    public String getAttendance(@RequestHeader("Cookie") String cookie) {
        return dataService.getAttendance(cookie);
    }
    
}
