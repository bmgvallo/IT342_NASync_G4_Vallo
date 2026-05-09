package com.citu.nasync_backend.features.semester;

import com.citu.nasync_backend.shared.entity.Semester;
import com.citu.nasync_backend.shared.repository.SemesterRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SemesterServiceTest {

    @Mock private SemesterRepository semesterRepository;
    @InjectMocks private SemesterService semesterService;

    private Semester semester(String label, boolean active) {
        Semester s = new Semester();
        s.setSemesterId(1L);
        s.setLabel(label);
        s.setStartDate(LocalDate.of(2026, 1, 1));
        s.setEndDate(LocalDate.of(2026, 5, 31));
        s.setActive(active);
        return s;
    }

    private CreateSemesterRequest request(String label, LocalDate start, LocalDate end) {
        CreateSemesterRequest r = new CreateSemesterRequest();
        r.setLabel(label);
        r.setStartDate(start);
        r.setEndDate(end);
        return r;
    }

    // SEM-01: create semester successfully
    @Test
    void createSemester_validInput_returnsSemester() {
        when(semesterRepository.existsByLabel("2S2526")).thenReturn(false);
        when(semesterRepository.save(any())).thenReturn(semester("2S2526", false));

        SemesterResponse result = semesterService.createSemester(
                request("2S2526", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 5, 31)));
        assertNotNull(result);
    }

    // SEM-02: duplicate label is rejected
    @Test
    void createSemester_duplicateLabel_throwsException() {
        when(semesterRepository.existsByLabel("2S2526")).thenReturn(true);
        assertThrows(RuntimeException.class, () -> semesterService.createSemester(
                request("2S2526", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 5, 31))));
    }

    // SEM-03: end date before start date is rejected
    @Test
    void createSemester_endBeforeStart_throwsException() {
        assertThrows(RuntimeException.class, () -> semesterService.createSemester(
                request("2S2526", LocalDate.of(2026, 5, 31), LocalDate.of(2026, 1, 1))));
    }

    // SEM-04 + SEM-05: activating one deactivates others
    @Test
    void activateSemester_deactivatesOthersFirst() {
        Semester other = semester("1S2526", true);
        Semester target = semester("2S2526", false);
        target.setSemesterId(2L);

        when(semesterRepository.findAll()).thenReturn(List.of(other, target));
        when(semesterRepository.findById(2L)).thenReturn(Optional.of(target));
        when(semesterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SemesterResponse result = semesterService.activateSemester(2L);
        assertTrue(result.isActive());
        assertFalse(other.isActive());
    }

    // SEM-08: list all semesters
    @Test
    void getAllSemesters_returnsList() {
        when(semesterRepository.findAll()).thenReturn(List.of(semester("1S2526", false), semester("2S2526", true)));
        assertEquals(2, semesterService.getAllSemesters().size());
    }
}
