package com.citu.nasync_backend.features.branch;

import com.citu.nasync_backend.shared.entity.Branch;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class BranchResponse {
    private Long branchId;
    private String name;
    private Long deptId;
    private String departmentName;
    private boolean isActive;
    private LocalDateTime createdAt;

    public static BranchResponse from(Branch branch) {
        BranchResponse dto = new BranchResponse();
        dto.branchId = branch.getBranchId();
        dto.name = branch.getName();
        dto.deptId = branch.getDepartment() != null ? branch.getDepartment().getDepartmentId() : null;
        dto.departmentName = branch.getDepartment() != null ? branch.getDepartment().getName() : null;
        dto.isActive = branch.isActive();
        dto.createdAt = branch.getCreatedAt();
        return dto;
    }

    // Getters and setters
    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    @JsonProperty("isActive")
    public boolean isActive() { return isActive; }
    public void setActive(boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}