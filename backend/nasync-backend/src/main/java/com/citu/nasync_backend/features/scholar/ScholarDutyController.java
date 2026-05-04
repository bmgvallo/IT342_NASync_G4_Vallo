package com.citu.nasync_backend.features.scholar;

import com.citu.nasync_backend.dto.request.ClockInRequest;
import com.citu.nasync_backend.dto.response.DutyResponse;
import com.citu.nasync_backend.features.duty.DutyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
 
@RestController
@RequestMapping("/api/v1/scholar/duties")
@CrossOrigin(origins = "http://localhost:5173")
@PreAuthorize("hasRole('SCHOLAR')")
public class ScholarDutyController {
 
    @Autowired private DutyService dutyService;
 
    @PostMapping("/clock-in")
    public ResponseEntity<?> clockIn(@RequestBody ClockInRequest request,
                                     Authentication auth) {
        try {
            return ResponseEntity.ok(dutyService.clockIn(auth.getName(), request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
 
    @PutMapping("/clock-out")
    public ResponseEntity<?> clockOut(Authentication auth) {
        try {
            return ResponseEntity.ok(dutyService.clockOut(auth.getName()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
 
    @GetMapping
    public ResponseEntity<List<DutyResponse>> getMyDuties(Authentication auth) {
        return ResponseEntity.ok(dutyService.getMyDuties(auth.getName()));
    }
 
    @GetMapping("/summary")
    public ResponseEntity<?> getSummary(Authentication auth) {
        return ResponseEntity.ok(dutyService.getMySummary(auth.getName()));
    }
}
