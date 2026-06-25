package com.citu.nasync_backend.features.auth.dto;

import jakarta.validation.constraints.Email;

public class UpdateGmailRequest {

    @Email(message = "Invalid email format.")
    private String gmail;

    public String getGmail() { return gmail; }
    public void setGmail(String gmail) { this.gmail = gmail; }
}
