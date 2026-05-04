package com.citu.nasync_backend.features.dutyday;

import com.citu.nasync_backend.dto.request.CreateDutyDayRequest;
import com.citu.nasync_backend.dto.response.DutyDayResponse;
import com.citu.nasync_backend.features.dutyday.DutyDayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
 
@RestController
@RequestMapping("/api/v1/admin/duty-days")
@CrossOrigin(origins = "http://localhost:5173")
@PreAuthorize("hasRole('ADMIN')")
public class DutyDayController {
 
    @Autowired private DutyDayService dutyDayService;
 
    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateDutyDayRequest request,
                                    Authentication auth) {
        try {
            DutyDayResponse r = dutyDayService.createDutyDay(request, auth.getName());
            return ResponseEntity.status(201).body(r);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
 
    @GetMapping
    public ResponseEntity<List<DutyDayResponse>> getBySemester(
            @RequestParam Long semesterId) {
        return ResponseEntity.ok(dutyDayService.getDutyDaysBySemester(semesterId));
    }
 
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            dutyDayService.deleteDutyDay(id);
            return ResponseEntity.ok(Map.of("message", "Duty day deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
