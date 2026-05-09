package com.citu.nasync_backend.shared.entity;

import com.citu.nasync_backend.shared.enums.Role;
import com.citu.nasync_backend.shared.enums.Shift;
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

    private User(Builder builder) {
        this.schoolId = builder.schoolId;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.email = builder.email;
        this.personalGmail = builder.personalGmail;
        this.passwordHash = builder.passwordHash;
        this.role = builder.role;
        this.department = builder.department;
        this.branch = builder.branch;
        this.shift = builder.shift;
        this.expectedTimeIn = builder.expectedTimeIn;
        this.expectedTimeOut = builder.expectedTimeOut;
        this.isActive = builder.isActive;
        this.firstTimeLogin = builder.firstTimeLogin;
        this.oauthProvider = builder.oauthProvider;
        this.oauthSubject = builder.oauthSubject;
    }

    public static class Builder {

        // required fields
        private String schoolId;
        private String firstName;
        private String lastName;
        private String email;
        private Role   role;

        // optional fields
        private String personalGmail;
        private String passwordHash;
        private Department department;
        private Branch branch;
        private Shift shift;
        private LocalTime expectedTimeIn;
        private LocalTime expectedTimeOut;
        private boolean isActive = true;
        private Boolean firstTimeLogin = true;
        private String oauthProvider;
        private String oauthSubject;

        public Builder schoolId(String schoolId) {
            this.schoolId = schoolId;
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder personalGmail(String personalGmail) {
            this.personalGmail = personalGmail;
            return this;
        }

        public Builder passwordHash(String passwordHash) {
            this.passwordHash = passwordHash;
            return this;
        }

        public Builder role(Role role) {
            this.role = role;
            return this;
        }

        public Builder department(Department department) {
            this.department = department;
            return this;
        }

        public Builder branch(Branch branch) {
            this.branch = branch;
            return this;
        }

        public Builder shift(Shift shift) {
            this.shift = shift;
            return this;
        }

        public Builder expectedTimeIn(LocalTime expectedTimeIn) {
            this.expectedTimeIn = expectedTimeIn;
            return this;
        }

        public Builder expectedTimeOut(LocalTime expectedTimeOut) {
            this.expectedTimeOut = expectedTimeOut;
            return this;
        }

        public Builder active(boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public Builder firstTimeLogin(Boolean firstTimeLogin) {
            this.firstTimeLogin = firstTimeLogin;
            return this;
        }

        public Builder oauthProvider(String oauthProvider) {
            this.oauthProvider = oauthProvider;
            return this;
        }

        public Builder oauthSubject(String oauthSubject) {
            this.oauthSubject = oauthSubject;
            return this;
        }

        public User build() {
            if (schoolId  == null || schoolId.isBlank())  throw new IllegalStateException("schoolId is required");
            if (firstName == null || firstName.isBlank()) throw new IllegalStateException("firstName is required");
            if (lastName  == null || lastName.isBlank())  throw new IllegalStateException("lastName is required");
            if (email     == null || email.isBlank())     throw new IllegalStateException("email is required");
            if (role      == null) throw new IllegalStateException("role is required");
            return new User(this);
        }
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