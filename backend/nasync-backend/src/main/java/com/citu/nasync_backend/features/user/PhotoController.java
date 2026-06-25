package com.citu.nasync_backend.features.user;

import com.citu.nasync_backend.shared.repository.UserRepository;
import com.citu.nasync_backend.shared.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/me")
public class PhotoController {

    private static final Logger log = LoggerFactory.getLogger(PhotoController.class);

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-key}")
    private String supabaseServiceKey;

    @Value("${supabase.storage.bucket}")
    private String bucket;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/photo")
    public ResponseEntity<?> uploadPhoto(@RequestParam("file") MultipartFile file,
                                         Authentication auth) {
        String contentType = file.getContentType();
        if (contentType == null ||
                (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Only JPG and PNG files are allowed."));
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "File size must not exceed 5 MB."));
        }

        String ext = contentType.equals("image/png") ? "png" : "jpg";
        String schoolId = auth.getName();

        User user = userRepository.findBySchoolId(schoolId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String filename = "user-" + user.getUserId() + "-" + UUID.randomUUID() + "." + ext;
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
            log.error("Photo upload failed for user {}: {}", schoolId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Upload failed. Check Supabase configuration."));
        }
    }
}
