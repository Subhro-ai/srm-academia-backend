package com.portal.academia_portal;

import org.springframework.web.bind.annotation.RestController;

import com.portal.academia_portal.dto.LoginStep1Request;
import com.portal.academia_portal.dto.LoginStep2Request;
import com.portal.academia_portal.service.AuthService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.portal.academia_portal.dto.UserLookupResponse;


@RestController
@RequestMapping("/api/auth") // Base path for all auth-related endpoints
public class AuthController {

    private final AuthService authService;
    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login-step1")
    public UserLookupResponse loginStep1(@RequestBody LoginStep1Request request) {
        return authService.initiateLogin(request.getUsername());
    }
    @PostMapping("/login-step2")
    public String loginStep2(@RequestBody LoginStep2Request request) {
        return authService.completeLogin(request);
    }
    
}