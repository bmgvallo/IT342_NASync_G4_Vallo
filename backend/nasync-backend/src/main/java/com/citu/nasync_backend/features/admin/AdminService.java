package com.citu.nasync_backend.features.admin;

import com.citu.nasync_backend.features.auth.chain.BranchResolutionHandler;
import com.citu.nasync_backend.features.auth.chain.DepartmentResolutionHandler;
import com.citu.nasync_backend.features.auth.chain.EmailDuplicateHandler;
import com.citu.nasync_backend.features.auth.chain.RegistrationContext;
import com.citu.nasync_backend.features.auth.chain.SchoolIdDuplicateHandler;
import com.citu.nasync_backend.features.auth.chain.UserRegistrationHandler;
import com.citu.nasync_backend.features.auth.dto.RegisterUserRequest;
import com.citu.nasync_backend.shared.dto.response.UserResponse;
import com.citu.nasync_backend.shared.entity.Branch;
import com.citu.nasync_backend.shared.entity.Department;
import com.citu.nasync_backend.shared.entity.User;
import com.citu.nasync_backend.shared.enums.Role;
import com.citu.nasync_backend.shared.repository.BranchRepository;
import com.citu.nasync_backend.shared.repository.DepartmentRepository;
import com.citu.nasync_backend.shared.repository.UserRepository;
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

        UserRegistrationHandler chain = new SchoolIdDuplicateHandler(userRepository);
        chain.setNext(new EmailDuplicateHandler(userRepository))
             .setNext(new DepartmentResolutionHandler(departmentRepository))
             .setNext(new BranchResolutionHandler(branchRepository));

        RegistrationContext context = new RegistrationContext();
        chain.handle(request, context);

        if (context.getBranch() != null && context.getDepartment() != null &&
                !context.getBranch().getDepartment().getDepartmentId()
                        .equals(context.getDepartment().getDepartmentId()))
            throw new RuntimeException("The selected branch does not belong to the specified department.");

        if (request.getRole() == Role.DEPARTMENT_HEAD && context.getBranch() != null &&
                userRepository.existsByBranchAndRole(context.getBranch(), Role.DEPARTMENT_HEAD))
            throw new RuntimeException("This branch already has an assigned Department Head.");

        String hashedPassword = passwordEncoder.encode(request.getSchoolId());

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

        userRepository.save(user);

        return UserResponse.from(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::from)
                .toList();
    }

    public List<UserResponse> getUsersByRole(Role role) {
        return userRepository.findAll()
                .stream()
                .filter(u -> u.getRole() == role)
                .map(UserResponse::from)
                .toList();
    }

    public void toggleActive(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    public void reassignUser(Long userId, Long deptId, Long branchId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (deptId != null) {
            Department dept = departmentRepository.findById(deptId)
                .orElseThrow(() -> new RuntimeException("Department not found"));
            user.setDepartment(dept);
        }
        if (branchId != null) {
            Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Branch not found"));
            user.setBranch(branch);
        }
        userRepository.save(user);
    }

    public UserResponse updateUser(Long userId, RegisterUserRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        String newEmail = request.getEmail();
        if (newEmail != null && !newEmail.equals(user.getEmail())) {
            userRepository.findByEmail(newEmail).ifPresent(existing -> {
                if (!existing.getUserId().equals(userId)) {
                    throw new RuntimeException("Email is already in use by another account.");
                }
            });
        }
        user.setEmail(newEmail);
        user.setPersonalGmail(request.getPersonalGmail());
        
        if (request.getDeptId() != null) {
            Department dept = departmentRepository.findById(request.getDeptId())
                .orElseThrow(() -> new RuntimeException("Department not found"));
            user.setDepartment(dept);
        }
        if (request.getBranchId() != null) {
            Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found"));
            Department effectiveDept = user.getDepartment();
            if (effectiveDept != null && branch.getDepartment() != null &&
                    !branch.getDepartment().getDepartmentId().equals(effectiveDept.getDepartmentId()))
                throw new RuntimeException("The selected branch does not belong to the specified department.");
            if (user.getRole() == Role.DEPARTMENT_HEAD) {
                boolean alreadyAssigned = userRepository.findByBranchAndRole(branch, Role.DEPARTMENT_HEAD)
                    .stream().anyMatch(u -> !u.getUserId().equals(userId));
                if (alreadyAssigned)
                    throw new RuntimeException("This branch already has an assigned Department Head.");
            }
            user.setBranch(branch);
        }
        if (request.getShift() != null) user.setShift(request.getShift());
        if (request.getExpectedTimeIn() != null) user.setExpectedTimeIn(request.getExpectedTimeIn());
        if (request.getExpectedTimeOut() != null) user.setExpectedTimeOut(request.getExpectedTimeOut());
        
        return UserResponse.from(userRepository.save(user));
    }
}