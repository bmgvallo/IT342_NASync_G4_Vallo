package com.citu.nasync_backend.features.auth.chain;

import com.citu.nasync_backend.shared.entity.Branch;
import com.citu.nasync_backend.shared.entity.Department;

public class RegistrationContext {

    private Department department;
    private Branch branch;

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public Branch getBranch() { return branch; }
    public void setBranch(Branch branch) { this.branch = branch; }
}