package com.citu.nasync_backend.entity;

import com.citu.nasync_backend.enums.Role;
import com.citu.nasync_backend.enums.Shift;
import jakarta.persistence.*;

import java.time.LocalTime;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "dept_id", nullable = true)
    @JsonIgnore
    private Department department;

    @ManyToOne
    @JoinColumn(name = "branch_id", nullable = true)
    @JsonIgnore
    private Branch branch;

    @Column(name = "school_id", nullable = false, unique = true)
    private String schoolId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "personal_gmail", nullable = true, unique = true)
    private String personalGmail;

    @Column(name = "password_hash", nullable = true)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "shift", nullable = true)
    private Shift shift;

    @Column(name = "expected_time_in", nullable = true)
    private LocalTime expectedTimeIn;

    @Column(name = "expected_time_out", nullable = true)
    private LocalTime expectedTimeOut;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "first_time_login", nullable = false)
    private Boolean firstTimeLogin = true;

    @Column(name = "oauth_provider", nullable = true)
    private String oauthProvider;

    @Column(name = "oauth_subject", nullable = true)
    private String oauthSubject;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public User() {}

    public User(Department department, Branch branch, String schoolId,
                String firstName, String lastName, String email,
                String passwordHash, Role role) {
        this.department = department;
        this.branch = branch;
        this.schoolId = schoolId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.isActive = true;
        this.firstTimeLogin = true;
    }

    public Long getUserId() { return userId; }
    public Department getDepartment() { return department; }
    public Branch getBranch() { return branch; }
    public String getSchoolId() { return schoolId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPersonalGmail() { return personalGmail; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public Shift getShift() { return shift; }
    public LocalTime getExpectedTimeIn() { return expectedTimeIn; }
    public LocalTime getExpectedTimeOut() { return expectedTimeOut; }
    public boolean isActive() { return isActive; }
    public Boolean getFirstTimeLogin() { return firstTimeLogin; }
    public String getOauthProvider() { return oauthProvider; }
    public String getOauthSubject() { return oauthSubject; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setUserId(Long userId) { this.userId = userId; }
    public void setDepartment(Department department) { this.department = department; }
    public void setBranch(Branch branch) { this.branch = branch; }
    public void setSchoolId(String schoolId) { this.schoolId = schoolId; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setEmail(String email) { this.email = email; }
    public void setPersonalGmail(String personalGmail) { this.personalGmail = personalGmail; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setRole(Role role) { this.role = role; }
    public void setShift(Shift shift) { this.shift = shift; }
    public void setExpectedTimeIn(LocalTime expectedTimeIn) { this.expectedTimeIn = expectedTimeIn; }
    public void setExpectedTimeOut(LocalTime expectedTimeOut) { this.expectedTimeOut = expectedTimeOut; }
    public void setActive(boolean isActive) { this.isActive = isActive; }
    public void setFirstTimeLogin(Boolean firstTimeLogin) { this.firstTimeLogin = firstTimeLogin; }
    public void setOauthProvider(String oauthProvider) { this.oauthProvider = oauthProvider; }
    public void setOauthSubject(String oauthSubject) { this.oauthSubject = oauthSubject; }
}