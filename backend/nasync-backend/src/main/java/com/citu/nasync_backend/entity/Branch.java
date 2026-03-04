package com.citu.nasync_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "branches", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"dept_id", "name"})
})

public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "branch_id")
    private Long branchId;

    @ManyToOne
    @JoinColumn(name = "dept_id", nullable = false)
    private Department department;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    //constructors
    public Branch() {
    }

    public Branch(Department department, String name) {
        this.department = department;
        this.name = name;
    }

    //getters
    public Long getBranchId() {
        return branchId;
    }

    public Department getDepartment() {
        return department;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    //setters
    public void setBranchId(Long branchId) {
        this.branchId = branchId;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public void setName(String name) {
        this.name = name;
    }

}
