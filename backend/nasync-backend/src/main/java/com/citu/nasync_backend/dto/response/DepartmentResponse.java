package com.citu.nasync_backend.dto.response;

import com.citu.nasync_backend.entity.Department;
import java.time.LocalDateTime;

public class DepartmentResponse {
    private Long departmentId;
    private String name;
    private LocalDateTime createdAt;

    public static DepartmentResponse from(Department department) {
        DepartmentResponse dto = new DepartmentResponse();
        dto.departmentId = department.getDepartmentId();
        dto.name = department.getName();
        dto.createdAt = department.getCreatedAt();
        return dto;
    }

    // Getters and setters
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}