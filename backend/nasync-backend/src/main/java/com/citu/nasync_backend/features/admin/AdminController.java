package com.citu.nasync_backend.features.admin;

import com.citu.nasync_backend.features.auth.dto.RegisterUserRequest;
import com.citu.nasync_backend.shared.dto.response.UserResponse;
import com.citu.nasync_backend.shared.entity.User;
import com.citu.nasync_backend.shared.enums.Role;
import com.citu.nasync_backend.shared.repository.UserRepository;
import com.citu.nasync_backend.features.admin.AdminService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin(origins = "http://localhost:5173")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    @Value("${supabase.url}") private String supabaseUrl;
    @Value("${supabase.service-key}") private String supabaseServiceKey;
    @Value("${supabase.storage.bucket}") private String bucket;

    @Autowired private AdminService adminService;
    @Autowired private UserRepository userRepository;

    @PostMapping("/users/register")
    public ResponseEntity<?> registerUser(@RequestBody @Valid RegisterUserRequest request) {
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

    @PutMapping("/users/{userId}/reassign")
    public ResponseEntity<?> reassignUser(@PathVariable Long userId,
                                        @RequestBody Map<String, Long> request) {
        try {
            Long deptId = request.get("deptId");
            Long branchId = request.get("branchId");
            adminService.reassignUser(userId, deptId, branchId);
            return ResponseEntity.ok(Map.of("message", "User reassigned successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable Long userId,
                                        @RequestBody @Valid RegisterUserRequest request) {
        try {
            UserResponse updated = adminService.updateUser(userId, request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/users/{userId}/photo")
    public ResponseEntity<?> uploadUserPhoto(@PathVariable Long userId,
                                             @RequestParam("file") MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null ||
                (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            return ResponseEntity.badRequest().body(Map.of("error", "Only JPG and PNG files are allowed."));
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest().body(Map.of("error", "File size must not exceed 5 MB."));
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String ext = contentType.equals("image/png") ? "png" : "jpg";
        String filename = "user-" + userId + "-" + UUID.randomUUID() + "." + ext;
        String uploadPath = supabaseUrl + "/storage/v1/object/" + bucket + "/" + filename;

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + supabaseServiceKey);
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.set("x-upsert", "true");

            HttpEntity<byte[]> entity = new HttpEntity<>(file.getBytes(), headers);
            restTemplate.exchange(uploadPath, HttpMethod.POST, entity, String.class);

            String publicUrl = supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + filename;
            user.setProfilePhotoUrl(publicUrl);
            userRepository.save(user);

            return ResponseEntity.ok(Map.of("photoUrl", publicUrl));
        } catch (Exception e) {
            log.error("Photo upload failed for userId {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Upload failed. Check Supabase configuration."));
        }
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

}