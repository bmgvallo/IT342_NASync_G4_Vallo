package com.citu.nasync_backend.features.admin;

import com.citu.nasync_backend.shared.dto.response.UserResponse;
import com.citu.nasync_backend.shared.entity.Branch;
import com.citu.nasync_backend.shared.entity.Department;
import com.citu.nasync_backend.shared.entity.User;
import com.citu.nasync_backend.shared.enums.Role;
import com.citu.nasync_backend.shared.repository.BranchRepository;
import com.citu.nasync_backend.shared.repository.DepartmentRepository;
import com.citu.nasync_backend.shared.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private BranchRepository branchRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private AdminService adminService;

    private User buildUser(Long id, Role role, boolean active) {
        Department dept = new Department();
        dept.setDepartmentId(1L);
        dept.setName("CITE");

        Branch branch = new Branch();
        branch.setBranchId(1L);
        branch.setName("Main");

        User u = new User.Builder()
                .schoolId("2021-0000" + id)
                .firstName("Test")
                .lastName("User")
                .email("test" + id + "@cit.edu")
                .role(role)
                .department(dept)
                .branch(branch)
                .active(active)
                .firstTimeLogin(true)
                .build();
        return u;
    }

    // ADMIN-05: getAllUsers returns all
    @Test
    void getAllUsers_returnsAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(
                buildUser(1L, Role.SCHOLAR, true),
                buildUser(2L, Role.ADMIN, true)
        ));
        List<UserResponse> result = adminService.getAllUsers();
        assertEquals(2, result.size());
    }

    // ADMIN-06: getUsersByRole filters correctly
    @Test
    void getUsersByRole_scholar_returnsOnlyScholars() {
        when(userRepository.findAll()).thenReturn(List.of(
                buildUser(1L, Role.SCHOLAR, true),
                buildUser(2L, Role.ADMIN, true),
                buildUser(3L, Role.SCHOLAR, true)
        ));
        List<UserResponse> result = adminService.getUsersByRole(Role.SCHOLAR);
        assertEquals(2, result.size());
        result.forEach(u -> assertEquals(Role.SCHOLAR, u.getRole()));    }

    // ADMIN-08: toggleActive flips the active flag
    @Test
    void toggleActive_activeUser_becomesInactive() {
        User user = buildUser(1L, Role.SCHOLAR, true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        adminService.toggleActive(1L);

        assertFalse(user.isActive());
        verify(userRepository).save(user);
    }

    @Test
    void toggleActive_inactiveUser_becomesActive() {
        User user = buildUser(1L, Role.SCHOLAR, false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        adminService.toggleActive(1L);

        assertTrue(user.isActive());
    }

    // ADMIN-08: toggleActive on missing user throws
    @Test
    void toggleActive_userNotFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> adminService.toggleActive(99L));
    }
}
