package com.portal.academia_portal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping; // Import Cookie
import org.springframework.web.bind.annotation.RestController; // Import HttpServletResponse

import com.portal.academia_portal.dto.LoginStep1Request;
import com.portal.academia_portal.dto.LoginStep2Request;
import com.portal.academia_portal.dto.UserLookupResponse;
import com.portal.academia_portal.service.AuthService;

import reactor.core.publisher.Mono;

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
    @PostMapping("/logout")
    public Mono<ResponseEntity<Void>> logout(@RequestHeader("X-Academia-Auth") String cookie) {
        return authService.logout(cookie)
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).<Void>build()));
    }
    
    
}