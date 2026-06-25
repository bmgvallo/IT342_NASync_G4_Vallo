package com.citu.nasync_backend.shared.dto.response;

import com.citu.nasync_backend.shared.entity.User;
import com.citu.nasync_backend.shared.enums.Role;
import com.citu.nasync_backend.shared.enums.Shift;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalTime;

public class UserResponse {

    private Long userId;
    private String schoolId;
    private String firstName;
    private String lastName;
    private String email;
    private String personalGmail;
    private Role role;
    private Long departmentId;
    private String departmentName;
    private Long branchId;
    private String branchName;
    private Shift shift;
    private LocalTime expectedTimeIn;
    private LocalTime expectedTimeOut;
    private boolean isActive;
    private boolean firstTimeLogin;
    private String profilePhotoUrl;

    public static UserResponse from(User user) {
        UserResponse dto = new UserResponse();
        dto.userId = user.getUserId();
        dto.schoolId = user.getSchoolId();
        dto.firstName = user.getFirstName();
        dto.lastName = user.getLastName();
        dto.email = user.getEmail();
        dto.personalGmail = user.getPersonalGmail();
        dto.role = user.getRole();
        dto.shift = user.getShift();
        dto.expectedTimeIn = user.getExpectedTimeIn();
        dto.expectedTimeOut = user.getExpectedTimeOut();
        dto.isActive = user.isActive();
        dto.firstTimeLogin = Boolean.TRUE.equals(user.getFirstTimeLogin());
        dto.profilePhotoUrl = user.getProfilePhotoUrl();
        if (user.getDepartment() != null) {
            dto.departmentId = user.getDepartment().getDepartmentId();
            dto.departmentName = user.getDepartment().getName();
        }
        if (user.getBranch() != null) {
            dto.branchId = user.getBranch().getBranchId();
            dto.branchName = user.getBranch().getName();
        }
        return dto;
    }

    public Long getUserId() { return userId; }
    public String getSchoolId() { return schoolId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPersonalGmail() { return personalGmail; }
    public Role getRole() { return role; }
    public Long getDepartmentId() { return departmentId; }
    public String getDepartmentName() { return departmentName; }
    public Long getBranchId() { return branchId; }
    public String getBranchName() { return branchName; }
    public Shift getShift() { return shift; }
    public LocalTime getExpectedTimeIn() { return expectedTimeIn; }
    public LocalTime getExpectedTimeOut() { return expectedTimeOut; }
    @JsonProperty("isActive")
    public boolean isActive() { return isActive; }
    public boolean isFirstTimeLogin() { return firstTimeLogin; }
    public String getProfilePhotoUrl() { return profilePhotoUrl; }
}

