package com.citu.nasync_backend.features.department;

import com.citu.nasync_backend.features.department.DepartmentResponse;
import com.citu.nasync_backend.shared.entity.Branch;
import com.citu.nasync_backend.shared.entity.Department;
import com.citu.nasync_backend.shared.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll()
                .stream()
                .map(DepartmentResponse::from)
                .collect(Collectors.toList());
    }

    public DepartmentResponse createDepartment(String name) {
        if (departmentRepository.existsByName(name)) {
            throw new RuntimeException("Department with name '" + name + "' already exists");
        }
        
        Department department = new Department();
        department.setName(name);
        
        Department savedDepartment = departmentRepository.save(department);
        return DepartmentResponse.from(savedDepartment);
    }

    public DepartmentResponse updateDepartment(Long id, String name) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));
        
        if (!department.getName().equals(name) && departmentRepository.existsByName(name)) {
            throw new RuntimeException("Department with name '" + name + "' already exists");
        }
        
        department.setName(name);
        
        Department updatedDepartment = departmentRepository.save(department);
        return DepartmentResponse.from(updatedDepartment);
    }

    @Transactional
    public void deactivateDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));

        boolean hasActiveBranches = department.getBranches().stream().anyMatch(Branch::isActive);
        if (hasActiveBranches) {
            throw new RuntimeException("Cannot deactivate department with active branches. Deactivate all branches first.");
        }

        department.setActive(false);
        departmentRepository.save(department);
    }

    public void reactivateDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));
        department.setActive(true);
        departmentRepository.save(department);
    }
}