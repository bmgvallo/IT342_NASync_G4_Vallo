package com.citu.nasync_backend.service;

import com.citu.nasync_backend.dto.response.BranchResponse;
import com.citu.nasync_backend.entity.Branch;
import com.citu.nasync_backend.entity.Department;
import com.citu.nasync_backend.repository.BranchRepository;
import com.citu.nasync_backend.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BranchService {

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    public List<BranchResponse> getAllBranches() {
        return branchRepository.findAll()
                .stream()
                .map(BranchResponse::from)
                .collect(Collectors.toList());
    }

    public BranchResponse createBranch(String name, Long deptId) {
        Department department = departmentRepository.findById(deptId)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + deptId));
        
        if (branchRepository.existsByNameAndDepartment(name, department)) {
            throw new RuntimeException("Branch with name '" + name + "' already exists in this department");
        }
        
        Branch branch = new Branch();
        branch.setName(name);
        branch.setDepartment(department);
        
        Branch savedBranch = branchRepository.save(branch);
        return BranchResponse.from(savedBranch);
    }

    public BranchResponse updateBranch(Long id, String name, Long deptId) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found with id: " + id));
        
        Department department = departmentRepository.findById(deptId)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + deptId));
        
        if (!branch.getName().equals(name) && 
            branchRepository.existsByNameAndDepartment(name, department)) {
            throw new RuntimeException("Branch with name '" + name + "' already exists in this department");
        }
        
        branch.setName(name);
        branch.setDepartment(department);
        
        Branch updatedBranch = branchRepository.save(branch);
        return BranchResponse.from(updatedBranch);
    }

    public void deleteBranch(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found with id: " + id));
        
        if (!branch.getUsers().isEmpty()) {
            throw new RuntimeException("Cannot delete branch with existing users");
        }
        
        branchRepository.delete(branch);
    }
}