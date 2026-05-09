package com.citu.nasync_backend.features.dutyday;

import com.citu.nasync_backend.shared.entity.DutyDay;
import com.citu.nasync_backend.shared.entity.Semester;
import com.citu.nasync_backend.shared.entity.User;
import com.citu.nasync_backend.shared.enums.DayType;
import com.citu.nasync_backend.shared.repository.DutyDayRepository;
import com.citu.nasync_backend.shared.repository.SemesterRepository;
import com.citu.nasync_backend.shared.repository.UserRepository;
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
class DutyDayServiceTest {

    @Mock private DutyDayRepository dutyDayRepository;
    @Mock private SemesterRepository semesterRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private DutyDayService dutyDayService;

    private Semester semester() {
        Semester s = new Semester();
        s.setSemesterId(1L);
        s.setLabel("2S2526");
        return s;
    }

    private CreateDutyDayRequest request(LocalDate date) {
        CreateDutyDayRequest r = new CreateDutyDayRequest();
        r.setSemesterId(1L);
        r.setDayDate(date);
        r.setDayType(DayType.REGULAR);
        return r;
    }

    // DD-01: create duty day successfully
    @Test
    void createDutyDay_validInput_returnsDutyDay() {
        LocalDate date = LocalDate.of(2026, 3, 10);
        when(semesterRepository.findById(1L)).thenReturn(Optional.of(semester()));
        when(dutyDayRepository.existsByDayDateAndSemester(any(), any())).thenReturn(false);
        when(userRepository.findBySchoolId("2021-ADMIN")).thenReturn(
                Optional.of(new User.Builder().schoolId("2021-ADMIN").firstName("OAS").lastName("Admin")
                .email("admin@cit.edu").role(com.citu.nasync_backend.shared.enums.Role.ADMIN)
                .active(true).firstTimeLogin(false).build()));

        DutyDay dd = new DutyDay();
        dd.setDayDate(date);
        dd.setSemester(semester());
        when(dutyDayRepository.save(any())).thenReturn(dd);

        DutyDayResponse result = dutyDayService.createDutyDay(request(date), "2021-ADMIN");
        assertNotNull(result);
    }

    // DD-02: duplicate date in same semester is rejected
    @Test
    void createDutyDay_duplicateDate_throwsException() {
        when(semesterRepository.findById(1L)).thenReturn(Optional.of(semester()));
        when(dutyDayRepository.existsByDayDateAndSemester(any(), any())).thenReturn(true);

        assertThrows(RuntimeException.class, () ->
                dutyDayService.createDutyDay(request(LocalDate.of(2026, 3, 10)), "2021-ADMIN"));
    }

    // DD-03: list duty days by semester
    @Test
    void getDutyDaysBySemester_returnsList() {
        when(semesterRepository.findById(1L)).thenReturn(Optional.of(semester()));
        DutyDay dd1 = new DutyDay();
        dd1.setSemester(semester());
        DutyDay dd2 = new DutyDay();
        dd2.setSemester(semester());
        when(dutyDayRepository.findBySemester(any())).thenReturn(List.of(dd1, dd2));

        List<DutyDayResponse> result = dutyDayService.getDutyDaysBySemester(1L);
        assertEquals(2, result.size());
    }

    // DD-04: delete existing duty day
    @Test
    void deleteDutyDay_exists_succeeds() {
        when(dutyDayRepository.existsById(1L)).thenReturn(true);
        assertDoesNotThrow(() -> dutyDayService.deleteDutyDay(1L));
        verify(dutyDayRepository).deleteById(1L);
    }

    // DD-04: delete non-existent duty day throws
    @Test
    void deleteDutyDay_notFound_throwsException() {
        when(dutyDayRepository.existsById(99L)).thenReturn(false);
        assertThrows(RuntimeException.class, () -> dutyDayService.deleteDutyDay(99L));
    }
}
