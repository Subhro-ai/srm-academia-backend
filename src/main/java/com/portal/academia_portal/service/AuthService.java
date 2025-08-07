package com.portal.academia_portal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.portal.academia_portal.dto.UserLookupResponse;

@Service
public class AuthService {

    private final WebClient webClient;

    @Autowired
    public AuthService(WebClient webClient) {
        this.webClient = webClient;
    }
    public UserLookupResponse initiateLogin(String username) {
        String url = "https://academia.srmist.edu.in/accounts/p/40-10002227248/signin/v2/lookup/\" + username";
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("mode", "primary");
        formData.add("cli_time", String.valueOf(System.currentTimeMillis()));
        formData.add("servicename", "ZohoCreator");
        formData.add("service_language", "en");
        formData.add("serviceurl", "https://academia.srmist.edu.in/portal/academia-academic-services/redirectFromLogin");

        UserLookupResponse response = webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(UserLookupResponse.class)
                .block();

        return response;

    }

}
