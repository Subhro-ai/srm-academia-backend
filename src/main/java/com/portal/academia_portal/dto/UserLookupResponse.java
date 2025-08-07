package com.portal.academia_portal.dto;
import com.fasterxml.jackson.annotation.JsonProperty;


public class UserLookupResponse {

    @JsonProperty("lookup") 
    private LookupData lookupData;

    public LookupData getLookupData() {
        return lookupData;
    }

    public void setLookupData(LookupData lookupData) {
        this.lookupData = lookupData;
    }

    // This is a static nested class to represent the "lookup" object
    public static class LookupData {
        private String identifier;
        private String digest;

        // Getters and Setters
        public String getIdentifier() {
            return identifier;
        }

        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }

        public String getDigest() {
            return digest;
        }

        public void setDigest(String digest) {
            this.digest = digest;
        }
    }
}