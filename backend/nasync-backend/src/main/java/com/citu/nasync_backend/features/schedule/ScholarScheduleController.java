package com.citu.nasync_backend.features.schedule;

import com.citu.nasync_backend.features.admin.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/scholars")
@PreAuthorize("hasRole('ADMIN')")
public class ScholarScheduleController {

    @Autowired private ScholarScheduleService scheduleService;
    @Autowired private AdminService adminService;

    @PostMapping("/{schoolId}/schedule")
    public ResponseEntity<?> setSchedule(
            @PathVariable String schoolId,
            @RequestBody List<ScheduleEntryRequest> entries) {
        try {
            scheduleService.setSchedule(schoolId, entries);
            return ResponseEntity.ok(Map.of("message", "Schedule set successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{schoolId}/schedule")
    public ResponseEntity<?> getSchedule(@PathVariable String schoolId) {
        try {
            return ResponseEntity.ok(scheduleService.getSchedule(schoolId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{schoolId}/duty-summary")
    public ResponseEntity<?> getDutySummary(@PathVariable String schoolId) {
        try {
            return ResponseEntity.ok(adminService.getScholarDutySummary(schoolId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{schoolId}/duties")
    public ResponseEntity<?> getDutyHistory(@PathVariable String schoolId) {
        try {
            return ResponseEntity.ok(adminService.getScholarDutyHistory(schoolId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{schoolId}/send-reminder")
    public ResponseEntity<?> sendReminder(@PathVariable String schoolId) {
        try {
            adminService.sendDutyReminder(schoolId);
            return ResponseEntity.ok(Map.of("message", "Reminder email sent successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
