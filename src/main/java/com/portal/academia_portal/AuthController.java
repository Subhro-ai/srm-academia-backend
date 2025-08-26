package com.portal.academia_portal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping; // Import Cookie
import org.springframework.web.bind.annotation.RestController; // Import HttpServletResponse

import com.portal.academia_portal.dto.LoginStep1Request;
import com.portal.academia_portal.dto.LoginStep2Request;
import com.portal.academia_portal.dto.UserLookupResponse;
import com.portal.academia_portal.service.AuthService;

@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@RestController
@RequestMapping("/api/auth")
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
        // Return the session cookie string directly in the response body
        return authService.completeLogin(request);
    }
}