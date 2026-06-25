package com.citu.nasync_backend.features.branch;

import com.citu.nasync_backend.features.branch.BranchResponse;
import com.citu.nasync_backend.features.branch.BranchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/branches")
@CrossOrigin(origins = "http://localhost:5173")
@PreAuthorize("hasRole('ADMIN')")
public class BranchController {

    @Autowired
    private BranchService branchService;

    @GetMapping
    public ResponseEntity<?> getAllBranches() {
        try {
            List<BranchResponse> branches = branchService.getAllBranches();
            return ResponseEntity.ok(Map.of("data", branches));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createBranch(@RequestBody Map<String, Object> request) {
        try {
            String name = (String) request.get("name");
            Integer deptId = (Integer) request.get("deptId");
            
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Branch name is required"));
            }
            if (deptId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Department ID is required"));
            }
            
            BranchResponse branch = branchService.createBranch(name.trim(), Long.valueOf(deptId));
            return ResponseEntity.ok(Map.of("data", branch));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBranch(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            String name = (String) request.get("name");
            Integer deptId = (Integer) request.get("deptId");
            
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Branch name is required"));
            }
            if (deptId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Department ID is required"));
            }
            
            BranchResponse branch = branchService.updateBranch(id, name.trim(), Long.valueOf(deptId));
            return ResponseEntity.ok(Map.of("data", branch));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deactivateBranch(@PathVariable Long id) {
        try {
            branchService.deactivateBranch(id);
            return ResponseEntity.ok(Map.of("message", "Branch deactivated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/reactivate")
    public ResponseEntity<?> reactivateBranch(@PathVariable Long id) {
        try {
            branchService.reactivateBranch(id);
            return ResponseEntity.ok(Map.of("message", "Branch reactivated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}