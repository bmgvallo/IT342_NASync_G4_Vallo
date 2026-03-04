package com.citu.nasync_backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.citu.nasync_backend.enums.Role;
import com.citu.nasync_backend.enums.Shift;
import java.time.LocalTime;


public class RegisterUserRequest {

    @NotBlank(message = "School ID is required")
    private String schoolId;

    @NotBlank(message = "First Name is required")
    private String firstName;

    @NotBlank(message = "Last Name is required")
    private String lastName;

    @NotBlank @Email(message = "Must be a valid CIT email")
    private String email;

    private String personalGmail; //optional for dept heads, required for scholars

    @NotBlank(message = "Default password is required")
    private String password;

    @NotNull(message = "Role is required")
    private Role role;

    private Long deptId;
    private Long branchId;

    //schedule fields - only for SCHOLAR role
    private Shift shift;
    private LocalTime expectedTimeIn;
    private LocalTime expectedTimeOut;

    //getters
    public String getSchoolId() { return schoolId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPersonalGmail() { return personalGmail; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }
    public Long getDeptId() { return deptId; }
    public Long getBranchId() { return branchId; }
    public Shift getShift() { return shift; }
    public LocalTime getExpectedTimeIn() { return expectedTimeIn; }
    public LocalTime getExpectedTimeOut() { return expectedTimeOut; }

    //setters
    public void setSchoolId(String schoolId) { this.schoolId = schoolId; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setEmail(String email) { this.email = email; }
    public void setPersonalGmail(String personalGmail) { this.personalGmail = personalGmail; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(Role role) { this.role = role; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }
    public void setShift(Shift shift) { this.shift = shift; }
    public void setExpectedTimeIn(LocalTime expectedTimeIn) { this.expectedTimeIn = expectedTimeIn; }
    public void setExpectedTimeOut(LocalTime expectedTimeOut) { this.expectedTimeOut = expectedTimeOut; }


}