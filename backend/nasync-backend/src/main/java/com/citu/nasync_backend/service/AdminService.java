package com.citu.nasync_backend.service;

import com.citu.nasync_backend.dto.request.RegisterUserRequest;
import com.citu.nasync_backend.dto.response.UserResponse;
import com.citu.nasync_backend.entity.Branch;
import com.citu.nasync_backend.entity.Department;
import com.citu.nasync_backend.entity.User;
import com.citu.nasync_backend.enums.Role;
import com.citu.nasync_backend.repository.BranchRepository;
import com.citu.nasync_backend.repository.DepartmentRepository;
import com.citu.nasync_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    @Autowired private UserRepository userRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private BranchRepository branchRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    public UserResponse registerUser(RegisterUserRequest request) {

        if (userRepository.existsBySchoolId(request.getSchoolId())) {
            throw new RuntimeException("School ID already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        Department department = null;
        if (request.getDeptId() != null) {
            department = departmentRepository.findById(request.getDeptId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
        }

        Branch branch = null;
        if (request.getBranchId() != null) {
            branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new RuntimeException("Branch not found"));
        }

        // Default password is the school ID — user must change on first login
        String hashedPassword = passwordEncoder.encode(request.getSchoolId());

        User user = new User();
        user.setSchoolId(request.getSchoolId());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPersonalGmail(request.getPersonalGmail());
        user.setPasswordHash(hashedPassword);
        user.setRole(request.getRole());
        user.setDepartment(department);
        user.setBranch(branch);
        user.setShift(request.getShift());
        user.setExpectedTimeIn(request.getExpectedTimeIn());
        user.setExpectedTimeOut(request.getExpectedTimeOut());
        user.setActive(true);
        user.setFirstTimeLogin(true);

        userRepository.save(user);
        return UserResponse.from(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::from)
                .toList();
    }

    public List<UserResponse> getUsersByRole(Role role) {
        return userRepository.findAll()
                .stream()
                .filter(u -> u.getRole() == role)
                .map(UserResponse::from)
                .toList();
    }

    public void toggleActive(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(!user.isActive());
        userRepository.save(user);
    }
}