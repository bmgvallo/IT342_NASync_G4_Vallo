package com.citu.nasync_backend.features.branch;

import com.citu.nasync_backend.shared.entity.Branch;
import com.citu.nasync_backend.shared.entity.Department;
import com.citu.nasync_backend.shared.repository.BranchRepository;
import com.citu.nasync_backend.shared.repository.DepartmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BranchServiceTest {

    @Mock private BranchRepository branchRepository;
    @Mock private DepartmentRepository departmentRepository;
    @InjectMocks private BranchService branchService;

    private Department dept() {
        Department d = new Department();
        d.setDepartmentId(1L);
        d.setName("CITE");
        return d;
    }

    private Branch branch(String name) {
        Branch b = new Branch();
        b.setBranchId(1L);
        b.setName(name);
        b.setDepartment(dept());
        return b;
    }

    // BRANCH-01: list all branches
    @Test
    void getAllBranches_returnsList() {
        when(branchRepository.findAll()).thenReturn(List.of(branch("Alpha"), branch("Beta")));
        List<BranchResponse> result = branchService.getAllBranches();
        assertEquals(2, result.size());
    }

    // BRANCH-02: create branch successfully
    @Test
    void createBranch_validInput_returnsBranch() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept()));
        when(branchRepository.existsByNameAndDepartment(any(), any())).thenReturn(false);
        when(branchRepository.save(any())).thenReturn(branch("Alpha"));

        BranchResponse result = branchService.createBranch("Alpha", 1L);
        assertNotNull(result);
    }

    // BRANCH-03: duplicate name in same dept is rejected
    @Test
    void createBranch_duplicateName_throwsException() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept()));
        when(branchRepository.existsByNameAndDepartment(any(), any())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> branchService.createBranch("Alpha", 1L));
    }

    // BRANCH-06: cannot deactivate branch with users
    @Test
    void deactivateBranch_hasUsers_throwsException() {
        Branch b = branch("Alpha");
        b.setUsers(List.of(new com.citu.nasync_backend.shared.entity.User.Builder()
        .schoolId("2021-00001").firstName("Test").lastName("User")
        .email("test@cit.edu").role(com.citu.nasync_backend.shared.enums.Role.SCHOLAR)
        .active(true).firstTimeLogin(false).build()));

        when(branchRepository.findById(1L)).thenReturn(Optional.of(b));

        assertThrows(RuntimeException.class, () -> branchService.deactivateBranch(1L));
    }

    // BRANCH-05: deactivate empty branch succeeds
    @Test
    void deactivateBranch_noUsers_succeeds() {
        Branch b = branch("Empty");
        b.setUsers(List.of());
        when(branchRepository.findById(1L)).thenReturn(Optional.of(b));

        assertDoesNotThrow(() -> branchService.deactivateBranch(1L));
        verify(branchRepository).save(b);
    }
}
