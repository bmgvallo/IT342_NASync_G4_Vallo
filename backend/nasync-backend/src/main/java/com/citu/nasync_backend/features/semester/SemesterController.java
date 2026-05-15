package com.citu.nasync_backend.features.semester;

import com.citu.nasync_backend.features.semester.CreateSemesterRequest;
import com.citu.nasync_backend.features.semester.SemesterResponse;
import com.citu.nasync_backend.features.semester.SemesterService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
 
@RestController
@RequestMapping("/api/v1/admin/semesters")
@CrossOrigin(origins = "http://localhost:5173")
@PreAuthorize("hasRole('ADMIN')")
public class SemesterController {
 
    @Autowired private SemesterService semesterService;
 
    @PostMapping
    public ResponseEntity<?> createSemester(@RequestBody @Valid CreateSemesterRequest request) {
        try {
            SemesterResponse r = semesterService.createSemester(request);
            return ResponseEntity.status(201).body(r);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
 
    @GetMapping
    public ResponseEntity<List<SemesterResponse>> getAllSemesters() {
        return ResponseEntity.ok(semesterService.getAllSemesters());
    }
 
    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activateSemester(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(semesterService.activateSemester(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
 
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateSemester(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(semesterService.deactivateSemester(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
 
    @GetMapping("/active")
    public ResponseEntity<?> getActiveSemester() {
        return semesterService.getActiveSemester()
            .<ResponseEntity<?>>map(ResponseEntity::ok)
            .orElse(ResponseEntity.ok(Map.of("message", "No active semester")));
    }
}