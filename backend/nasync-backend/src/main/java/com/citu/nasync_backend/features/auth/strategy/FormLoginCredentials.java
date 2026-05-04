package com.citu.nasync_backend.features.auth.strategy;

public class FormLoginCredentials {

    private final String schoolId;
    private final String password;

    public FormLoginCredentials(String schoolId, String password) {
        this.schoolId = schoolId;
        this.password = password;
    }

    public String getSchoolId() { return schoolId; }
    public String getPassword() { return password; }
}