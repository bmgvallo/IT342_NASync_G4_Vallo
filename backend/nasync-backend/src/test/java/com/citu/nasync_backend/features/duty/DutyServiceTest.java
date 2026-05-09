package com.citu.nasync_backend.features.duty;

import com.citu.nasync_backend.shared.entity.*;
import com.citu.nasync_backend.shared.enums.DutyStatus;
import com.citu.nasync_backend.shared.enums.DutyType;
import com.citu.nasync_backend.shared.enums.Role;
import com.citu.nasync_backend.shared.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DutyServiceTest {

    @Mock private DutyRepository dutyRepository;
    @Mock private SemesterRepository semesterRepository;
    @Mock private DutyDayRepository dutyDayRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private DutyService dutyService;

    private User scholar() {
        Branch branch = new Branch();
        branch.setBranchId(1L);
        branch.setName("Main");

        return new User.Builder()
                .schoolId("2021-00001")
                .firstName("Test")
                .lastName("User")
                .email("test@cit.edu")
                .role(Role.SCHOLAR)
                .active(true)
                .firstTimeLogin(false)
                .expectedTimeIn(LocalTime.of(8, 0))
                .expectedTimeOut(LocalTime.of(12, 0))
                .branch(branch)
                .build();
    }

    private User deptHead() {
        Branch branch = new Branch();
        branch.setBranchId(1L);
        branch.setName("Main");

        return new User.Builder()
                .schoolId("2021-DH001")
                .firstName("Dept")
                .lastName("Head")
                .email("depthead@cit.edu")
                .role(Role.DEPARTMENT_HEAD)
                .active(true)
                .firstTimeLogin(false)
                .branch(branch)
                .build();
    }

    private Semester activeSemester() {
        Semester s = new Semester();
        s.setSemesterId(1L);
        s.setLabel("2S2526");
        s.setActive(true);
        return s;
    }

    // SCH-03: clock-in twice in same day is blocked
    @Test
    void clockIn_alreadyHasOpenDuty_throwsException() {
        when(userRepository.findBySchoolId("2021-00001")).thenReturn(Optional.of(scholar()));
        when(semesterRepository.findByIsActiveTrue()).thenReturn(Optional.of(activeSemester()));
        when(dutyDayRepository.findByDayDateAndSemester(any(), any())).thenReturn(Optional.empty());
        when(dutyRepository.existsByScholarAndDutyDateAndTimeOutIsNull(any(), any())).thenReturn(true);

        com.citu.nasync_backend.features.duty.ClockInRequest req =
                new com.citu.nasync_backend.features.duty.ClockInRequest();
        req.setDutyType(DutyType.REGULAR);

        assertThrows(RuntimeException.class, () -> dutyService.clockIn("2021-00001", req));
    }

    // SCH-02: clock-in blocked on no active semester
    @Test
    void clockIn_noActiveSemester_throwsException() {
        when(userRepository.findBySchoolId("2021-00001")).thenReturn(Optional.of(scholar()));
        when(semesterRepository.findByIsActiveTrue()).thenReturn(Optional.empty());

        com.citu.nasync_backend.features.duty.ClockInRequest req =
                new com.citu.nasync_backend.features.duty.ClockInRequest();
        assertThrows(RuntimeException.class, () -> dutyService.clockIn("2021-00001", req));
    }

    // SCH-09: getMyDuties returns list
    @Test
    void getMyDuties_returnsScholarDuties() {
        when(userRepository.findBySchoolId("2021-00001")).thenReturn(Optional.of(scholar()));
        when(dutyRepository.findByScholarOrderByDutyDateDesc(any())).thenReturn(List.of());

        List<DutyResponse> result = dutyService.getMyDuties("2021-00001");
        assertNotNull(result);
    }

    // DH-02: approve pending duty
    @Test
    void approveDuty_pendingDuty_setsApproved() {
        Duty duty = new Duty();
        duty.setApprovalStatus(DutyStatus.PENDING);
        duty.setScholar(scholar());
        duty.setSemester(activeSemester());

        when(userRepository.findBySchoolId("2021-DH001")).thenReturn(Optional.of(deptHead()));
        when(dutyRepository.findById(1L)).thenReturn(Optional.of(duty));
        when(dutyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DutyResponse result = dutyService.approveDuty(1L, "2021-DH001", new ApprovalRequest());
        assertEquals(DutyStatus.APPROVED, result.getApprovalStatus());
    }

    // DH-04: cannot approve non-pending duty
    @Test
    void approveDuty_alreadyApproved_throwsException() {
        Duty duty = new Duty();
        duty.setApprovalStatus(DutyStatus.APPROVED);

        when(userRepository.findBySchoolId("2021-DH001")).thenReturn(Optional.of(deptHead()));
        when(dutyRepository.findById(1L)).thenReturn(Optional.of(duty));

        assertThrows(RuntimeException.class, () ->
                dutyService.approveDuty(1L, "2021-DH001", new ApprovalRequest()));
    }

    // DH-03: reject pending duty
    @Test
    void rejectDuty_pendingDuty_setsRejected() {
        Duty duty = new Duty();
        duty.setApprovalStatus(DutyStatus.PENDING);
        duty.setScholar(scholar());
        duty.setSemester(activeSemester());

        when(userRepository.findBySchoolId("2021-DH001")).thenReturn(Optional.of(deptHead()));
        when(dutyRepository.findById(1L)).thenReturn(Optional.of(duty));
        when(dutyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ApprovalRequest req = new ApprovalRequest();
        req.setRemarks("Not valid");

        DutyResponse result = dutyService.rejectDuty(1L, "2021-DH001", req);
        assertEquals(DutyStatus.REJECTED, result.getApprovalStatus());
    }

    // DH-07: dept head without branch throws
    @Test
    void getPendingDuties_deptHeadNoBranch_throwsException() {
        User dh = new User.Builder()
                .schoolId("2021-DH001")
                .firstName("Maria")
                .lastName("Santos")
                .email("maria@cit.edu")
                .role(Role.DEPARTMENT_HEAD)
                .active(true)
                .firstTimeLogin(false)
                .build();

        when(userRepository.findBySchoolId("2021-DH001")).thenReturn(Optional.of(dh));
        assertThrows(RuntimeException.class, () -> dutyService.getPendingDuties("2021-DH001"));
    }
}
