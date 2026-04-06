package com.citu.nasync_backend.service;

import com.citu.nasync_backend.chain.BranchResolutionHandler;
import com.citu.nasync_backend.chain.DepartmentResolutionHandler;
import com.citu.nasync_backend.chain.EmailDuplicateHandler;
import com.citu.nasync_backend.chain.RegistrationContext;
import com.citu.nasync_backend.chain.SchoolIdDuplicateHandler;
import com.citu.nasync_backend.chain.UserRegistrationHandler;
import com.citu.nasync_backend.dto.request.RegisterUserRequest;
import com.citu.nasync_backend.dto.response.UserResponse;
import com.citu.nasync_backend.entity.User;
import com.citu.nasync_backend.enums.Role;
import com.citu.nasync_backend.repository.BranchRepository;
import com.citu.nasync_backend.repository.DepartmentRepository;
import com.citu.nasync_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AdminService {

    @Autowired private UserRepository userRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private BranchRepository branchRepository;
    @Autowired private PasswordEncoder passwordEncoder;


    public UserResponse registerUser(RegisterUserRequest request) {

        // ── Chain of Responsibility Pattern ─────────────────────────────────
        UserRegistrationHandler chain = new SchoolIdDuplicateHandler(userRepository);
        chain.setNext(new EmailDuplicateHandler(userRepository))
             .setNext(new DepartmentResolutionHandler(departmentRepository))
             .setNext(new BranchResolutionHandler(branchRepository));

        RegistrationContext context = new RegistrationContext();
        chain.handle(request, context);
        // ── End Chain ────────────────────────────────────────────────────────

        String hashedPassword = passwordEncoder.encode(request.getSchoolId());

        // ── Builder Pattern ──────────────────────────────────────────────────
        User user = new User.Builder()
                .schoolId(request.getSchoolId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .personalGmail(request.getPersonalGmail())
                .passwordHash(hashedPassword)
                .role(request.getRole())
                .department(context.getDepartment())
                .branch(context.getBranch())
                .shift(request.getShift())
                .expectedTimeIn(request.getExpectedTimeIn())
                .expectedTimeOut(request.getExpectedTimeOut())
                .active(true)
                .firstTimeLogin(true)
                .build();
        // ── End Builder ──────────────────────────────────────────────────────

        userRepository.save(user);

        // ✅ CHANGED: Use static from() method instead of factory
        return UserResponse.from(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::from)  // ✅ CHANGED: Static method reference
                .toList();
    }

    public List<UserResponse> getUsersByRole(Role role) {
        return userRepository.findAll()
                .stream()
                .filter(u -> u.getRole() == role)
                .map(UserResponse::from)  // ✅ CHANGED: Static method reference
                .toList();
    }

    public void toggleActive(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(!user.isActive());
        userRepository.save(user);
    }
}