package com.citu.nasync_backend.features.department;

import com.citu.nasync_backend.shared.entity.Branch;
import com.citu.nasync_backend.shared.entity.Department;
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
class DepartmentServiceTest {

    @Mock private DepartmentRepository departmentRepository;
    @InjectMocks private DepartmentService departmentService;

    private Department dept(String name) {
        Department d = new Department();
        d.setDepartmentId(1L);
        d.setName(name);
        d.setBranches(List.of());
        return d;
    }

    // DEPT-01: list all departments
    @Test
    void getAllDepartments_returnsList() {
        when(departmentRepository.findAll()).thenReturn(List.of(dept("CITE"), dept("CENG")));
        List<DepartmentResponse> result = departmentService.getAllDepartments();
        assertEquals(2, result.size());
    }

    // DEPT-02: create department successfully
    @Test
    void createDepartment_newName_returnsDepartment() {
        when(departmentRepository.existsByName("CITE")).thenReturn(false);
        when(departmentRepository.save(any())).thenReturn(dept("CITE"));

        DepartmentResponse result = departmentService.createDepartment("CITE");
        assertNotNull(result);
        assertEquals("CITE", result.getName());
    }

    // DEPT-03: duplicate name is rejected
    @Test
    void createDepartment_duplicateName_throwsException() {
        when(departmentRepository.existsByName("CITE")).thenReturn(true);
        assertThrows(RuntimeException.class, () -> departmentService.createDepartment("CITE"));
    }

    // DEPT-05: cannot deactivate department with active branches
    @Test
    void deactivateDepartment_hasActiveBranches_throwsException() {
        Department d = dept("CITE");
        Branch b = new Branch();
        b.setBranchId(1L);
        b.setActive(true);
        d.setBranches(List.of(b));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(d));

        assertThrows(RuntimeException.class, () -> departmentService.deactivateDepartment(1L));
    }

    // DEPT-04: update department name
    @Test
    void updateDepartment_newName_returnsUpdated() {
        Department d = dept("CITE");
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(d));
        when(departmentRepository.existsByName("CCS")).thenReturn(false);
        when(departmentRepository.save(any())).thenReturn(dept("CCS"));

        DepartmentResponse result = departmentService.updateDepartment(1L, "CCS");
        assertNotNull(result);
    }
}
