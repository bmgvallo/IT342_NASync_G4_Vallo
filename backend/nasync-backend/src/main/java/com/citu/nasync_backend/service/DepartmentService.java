package com.citu.nasync_backend.service;

import com.citu.nasync_backend.dto.response.DepartmentResponse;
import com.citu.nasync_backend.entity.Department;
import com.citu.nasync_backend.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));
        
        if (!department.getBranches().isEmpty()) {
            throw new RuntimeException("Cannot delete department with existing branches");
        }
        
        departmentRepository.delete(department);
    }
}