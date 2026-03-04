package com.citu.nasync_backend.dto.response;

import com.citu.nasync_backend.entity.User;
import com.citu.nasync_backend.enums.Role;
import com.citu.nasync_backend.enums.Shift;
import java.time.LocalTime;

public class UserResponse {

    private Long userId;
    private String schoolId;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
    private String departmentName;
    private String branchName;
    private Shift shift;
    private LocalTime expectedTimeIn;
    private LocalTime expectedTimeOut;
    private boolean isActive;

    // static factory method — converts entity to DTO
    public static UserResponse from(User user) {
        UserResponse dto = new UserResponse();
        dto.userId = user.getUserId();
        dto.schoolId = user.getSchoolId();
        dto.firstName = user.getFirstName();
        dto.lastName = user.getLastName();
        dto.email = user.getEmail();
        dto.role = user.getRole();
        dto.shift = user.getShift();
        dto.expectedTimeIn = user.getExpectedTimeIn();
        dto.expectedTimeOut = user.getExpectedTimeOut();
        dto.isActive = user.isActive();
        if (user.getDepartment() != null)
            dto.departmentName = user.getDepartment().getName();
        if (user.getBranch() != null)
            dto.branchName = user.getBranch().getName();
        return dto;
    }

    public Long getUserId() { return userId; }
    public String getSchoolId() { return schoolId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }
    public String getDepartmentName() { return departmentName; }
    public String getBranchName() { return branchName; }
    public Shift getShift() { return shift; }
    public LocalTime getExpectedTimeIn() { return expectedTimeIn; }
    public LocalTime getExpectedTimeOut() { return expectedTimeOut; }
    public boolean isActive() { return isActive; }
}

