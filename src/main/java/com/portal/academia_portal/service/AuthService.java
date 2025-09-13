package com.portal.academia_portal.service;

import com.portal.academia_portal.dto.LoginStep2Request;
import com.portal.academia_portal.dto.UserLookupResponse;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AuthService {

    private final WebClient webClient;
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private static final String LOGIN_PAGE_URL = "https://academia.srmist.edu.in/accounts/p/10002227248/signin?hide_fp=true&servicename=ZohoCreator&service_language=en&css_url=/49910842/academia-academic-services/downloadPortalCustomCss/login&dcc=true&serviceurl=https%3A%2F%2Facademia.srmist.edu.in%2Fportal%2Facademia-academic-services%2FredirectFromLogin";


    private String sessionCookies;
    private String sessionCsrfToken;

    @Autowired
    public AuthService(WebClient webClient) {
        this.webClient = webClient;
    }

    public UserLookupResponse initiateLogin(String username) {
        ResponseEntity<String> initialResponse = webClient.get()
                .uri(LOGIN_PAGE_URL)
                .header(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36")
                .retrieve()
                .toEntity(String.class)
                .block();

        if (initialResponse == null) {
            throw new IllegalStateException("Failed to get a valid response from the main login page.");
        }


        this.sessionCookies = String.join("; ", initialResponse.getHeaders().getOrEmpty(HttpHeaders.SET_COOKIE));
        Pattern pattern = Pattern.compile("iamcsr=([^;]+)");
        Matcher matcher = pattern.matcher(this.sessionCookies);
        if (matcher.find()) {
            this.sessionCsrfToken = matcher.group(1);
        }

        if (this.sessionCsrfToken == null || this.sessionCsrfToken.isEmpty()) {
            throw new IllegalStateException("Could not find the 'iamcsr' cookie in the response headers.");
        }

        String lookupUrl = "https://academia.srmist.edu.in/accounts/p/40-10002227248/signin/v2/lookup/" + username;

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("mode", "primary");
        formData.add("cli_time", String.valueOf(System.currentTimeMillis()));
        formData.add("servicename", "ZohoCreator");
        formData.add("service_language", "en");
        formData.add("serviceurl", "https://academia.srmist.edu.in/portal/academia-academic-services/redirectFromLogin");

        return webClient.post()
                .uri(lookupUrl)
                .header(HttpHeaders.COOKIE, this.sessionCookies)
                .header("x-zcsrf-token", "iamcsrcoo=" + this.sessionCsrfToken)
                .header("Referer", LOGIN_PAGE_URL)
                .contentType(MediaType.valueOf("application/x-www-form-urlencoded"))
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(UserLookupResponse.class)
                .block();
    }

    public String completeLogin(LoginStep2Request request) {
        ResponseEntity<String> responseEntity = webClient.post()
            .uri(uriBuilder -> uriBuilder
                .scheme("https")
                .host("academia.srmist.edu.in")
                .path("/accounts/p/40-10002227248/signin/v2/primary/{identifier}/password")
                .queryParam("digest", request.getDigest())

                .build(request.getIdentifier()))

            .header(HttpHeaders.COOKIE, this.sessionCookies)
            .header("x-zcsrf-token", "iamcsrcoo=" + this.sessionCsrfToken)
            .header("Referer", LOGIN_PAGE_URL)
            .header(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("passwordauth", Map.of("password", request.getPassword())))
            .retrieve()
            .toEntity(String.class)
            .block();

        if (responseEntity != null && responseEntity.getHeaders().containsKey(HttpHeaders.SET_COOKIE)) {
            List<String> newCookies = responseEntity.getHeaders().get(HttpHeaders.SET_COOKIE);
            return this.sessionCookies + "; " + String.join("; ", newCookies);
        }

        throw new IllegalStateException("Login failed: Could not retrieve final authentication cookies.");
    }
    public void logout(String cookie) {
        String logoutUrl = "https://academia.srmist.edu.in/accounts/p/10002227248/logout?servicename=ZohoCreator&serviceurl=https://academia.srmist.edu.in";
        try {
            ResponseEntity<Void> response = webClient.get()
                .uri(logoutUrl)
                .header(HttpHeaders.COOKIE, cookie)
                .header(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36")
                .retrieve()
                .toBodilessEntity()
                .block();
            
            if (response != null && (response.getStatusCode().is2xxSuccessful() || response.getStatusCode().is3xxRedirection())) {
                logger.info("Successfully initiated logout from Academia server. Status: " + response.getStatusCode());
            } else {
                logger.warn("Academia server returned an unexpected status for logout: " + (response != null ? response.getStatusCode() : "N/A"));
            }
        } catch (Exception e) {
            logger.error("An error occurred while trying to log out from Academia server.", e);
        }
    }
}