package com.portal.academia_portal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class AuthService {

    private final WebClient webClient;
    public String initiateLogin(String username) {
        // Logic to initiate login process
        return "Login initiated for user: " + username;
    }
    @Autowired
    public AuthService(WebClient webClient) {
        this.webClient = webClient;
    }
}
