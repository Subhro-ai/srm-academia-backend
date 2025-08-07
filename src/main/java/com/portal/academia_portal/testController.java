package com.portal.academia_portal;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
public class testController {
    @GetMapping("/hello")
    public String getMethodName() {
        return "HELLO WORLD";
    }
    
}
