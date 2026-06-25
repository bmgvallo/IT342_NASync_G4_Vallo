package com.citu.nasync_backend.features.scholar;

import com.citu.nasync_backend.features.schedule.ScholarScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/scholar/schedule")
@PreAuthorize("hasRole('SCHOLAR')")
public class ScholarMyScheduleController {

    @Autowired private ScholarScheduleService scheduleService;

    @GetMapping("/today")
    public ResponseEntity<?> getTodayEntry(Authentication auth) {
        Map<String, Object> entry = scheduleService.getTodayEntry(auth.getName());
        if (entry == null) return ResponseEntity.ok(Map.of());
        return ResponseEntity.ok(entry);
    }
}
