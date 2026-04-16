package com.citu.nasync_backend.controller;
 
import com.citu.nasync_backend.dto.request.ApprovalRequest;
import com.citu.nasync_backend.dto.response.DutyResponse;
import com.citu.nasync_backend.service.DutyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.List;
 
@RestController
@RequestMapping("/api/v1/depthead/duties")
@CrossOrigin(origins = "http://localhost:5173")
@PreAuthorize("hasRole('DEPARTMENT_HEAD')")
public class DeptHeadDutyController {
 
    @Autowired private DutyService dutyService;
 
    @GetMapping("/pending")
    public ResponseEntity<?> getPending(Authentication auth) {
        try {
            String schoolId = auth.getName();
            System.out.println("Dept Head schoolId: " + schoolId);
            
            List<DutyResponse> pending = dutyService.getPendingDuties(schoolId);
            System.out.println("Found pending duties: " + pending.size());
            
            return ResponseEntity.ok(pending);
        } catch (RuntimeException e) {
            System.err.println("Error in getPending: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
 
    @GetMapping
    public ResponseEntity<?> getAll(Authentication auth) {
        return ResponseEntity.ok(dutyService.getBranchDuties(auth.getName()));
    }
 
    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Long id,
                                     @RequestBody(required = false) ApprovalRequest req,
                                     Authentication auth) {
        try {
            DutyResponse r = dutyService.approveDuty(id, auth.getName(), req);
            return ResponseEntity.ok(r);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
 
    @PutMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id,
                                    @RequestBody(required = false) ApprovalRequest req,
                                    Authentication auth) {
        try {
            DutyResponse r = dutyService.rejectDuty(id, auth.getName(), req);
            return ResponseEntity.ok(r);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/scholars")
    public ResponseEntity<?> getMyScholars(Authentication auth) {
        try {
            return ResponseEntity.ok(dutyService.getScholarsByBranch(auth.getName()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }


}
