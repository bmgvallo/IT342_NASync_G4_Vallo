package com.citu.nasync_backend.chain;

import com.citu.nasync_backend.dto.request.RegisterUserRequest;
import com.citu.nasync_backend.entity.Department;
import com.citu.nasync_backend.repository.DepartmentRepository;

public class DepartmentResolutionHandler extends UserRegistrationHandler {

    private final DepartmentRepository departmentRepository;

    public DepartmentResolutionHandler(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    public void handle(RegisterUserRequest request, RegistrationContext context) {
        if (request.getDeptId() != null) {
            Department department = departmentRepository.findById(request.getDeptId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            context.setDepartment(department);
        }
        if (next != null) next.handle(request, context);
    }
}