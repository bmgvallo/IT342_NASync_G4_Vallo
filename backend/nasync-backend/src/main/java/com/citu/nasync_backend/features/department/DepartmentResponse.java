package com.citu.nasync_backend.features.department;

import com.citu.nasync_backend.shared.entity.Department;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class DepartmentResponse {
    private Long departmentId;
    private String name;
    private boolean isActive;
    private LocalDateTime createdAt;

    public static DepartmentResponse from(Department department) {
        DepartmentResponse dto = new DepartmentResponse();
        dto.departmentId = department.getDepartmentId();
        dto.name = department.getName();
        dto.isActive = department.isActive();
        dto.createdAt = department.getCreatedAt();
        return dto;
    }

    // Getters and setters
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @JsonProperty("isActive")
    public boolean isActive() { return isActive; }
    public void setActive(boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}