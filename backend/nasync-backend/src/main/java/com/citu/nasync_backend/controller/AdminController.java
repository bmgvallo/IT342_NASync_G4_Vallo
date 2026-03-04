package com.citu.nasync_backend.controller;

import com.citu.nasync_backend.dto.request.RegisterUserRequest;
import com.citu.nasync_backend.dto.response.UserResponse;
import com.citu.nasync_backend.enums.Role;
import com.citu.nasync_backend.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin(origins = "http://localhost:5173")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired private AdminService adminService;

    @PostMapping("/users/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterUserRequest request) {
        try {
            UserResponse user = adminService.registerUser(request);
            return ResponseEntity.status(201).body(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/users/scholars")
    public ResponseEntity<List<UserResponse>> getScholars() {
        return ResponseEntity.ok(adminService.getUsersByRole(Role.SCHOLAR));
    }

    @GetMapping("/users/department-heads")
    public ResponseEntity<List<UserResponse>> getDepartmentHeads() {
        return ResponseEntity.ok(adminService.getUsersByRole(Role.DEPARTMENT_HEAD));
    }

    @PutMapping("/users/{userId}/toggle-active")
    public ResponseEntity<?> toggleActive(@PathVariable Long userId) {
        try {
            adminService.toggleActive(userId);
            return ResponseEntity.ok(Map.of("message", "User status updated"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}