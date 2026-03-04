package com.citu.nasync_backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank(message = "School ID is required")
    private String schoolId;

    @NotBlank(message = "Password is required")
    private String password;

    public LoginRequest() {
    }

    // Getters and setters
    public String getSchoolId() {
        return schoolId;
    }

    public String getPassword() {
        return password;
    }

    public void setSchoolId(String schoolId) {
        this.schoolId = schoolId;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}