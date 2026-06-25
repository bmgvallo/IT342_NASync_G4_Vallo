package com.citu.nasync_backend.features.duty;
 
import com.citu.nasync_backend.features.duty.ClockInRequest;
import com.citu.nasync_backend.features.duty.ApprovalRequest;
import com.citu.nasync_backend.features.duty.DutyResponse;
import com.citu.nasync_backend.shared.entity.*;
import com.citu.nasync_backend.shared.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.citu.nasync_backend.shared.enums.DayType;
import com.citu.nasync_backend.shared.enums.DutyStatus;
import com.citu.nasync_backend.shared.enums.AttendanceStatus;
import com.citu.nasync_backend.shared.enums.DutyType;
import com.citu.nasync_backend.shared.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import java.time.Clock;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
 
@Service
public class DutyService {

    private static final Logger log = LoggerFactory.getLogger(DutyService.class);

    @Autowired private DutyRepository dutyRepository;
    @Autowired private SemesterRepository semesterRepository;
    @Autowired private DutyDayRepository dutyDayRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ScholarScheduleRepository scholarScheduleRepository;
    @Autowired private EmailService emailService;
    @Autowired private Clock clock;
 
    // 12hr time format helper
    private String formatTo12Hour(LocalTime time) {
        if (time == null) return "unknown time";
        int hour = time.getHour();
        int minute = time.getMinute();
        String ampm = hour >= 12 ? "PM" : "AM";
        int hour12 = hour % 12;
        if (hour12 == 0) hour12 = 12;
        return String.format("%d:%02d %s", hour12, minute, ampm);
    }
    
    // clockin logic:
    public DutyResponse clockIn(String schoolId, ClockInRequest request) {
        User scholar = userRepository.findBySchoolId(schoolId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Semester semester = semesterRepository.findByIsActiveTrue()
            .orElseThrow(() -> new RuntimeException("SEM-001: No active semester available. Duty recording is temporarily disabled."));

        LocalDate today = LocalDate.now(clock);

        if (today.isBefore(semester.getStartDate()) || today.isAfter(semester.getEndDate()))
            throw new RuntimeException("SEM-002: Clock-in is not allowed outside the active semester's date range (" +
                semester.getStartDate() + " to " + semester.getEndDate() + ").");

        // no clock in for holiday/suspended days
        dutyDayRepository.findByDayDateAndSemester(today, semester)
            .ifPresent(dd -> {
                if (dd.getDayType() == DayType.HOLIDAY || dd.getDayType() == DayType.SUSPENDED) {
                    throw new RuntimeException("Clock-in not allowed. Today is marked as " + dd.getDayType().name() + ".");
                }
            });

        // ensure no open duty already exists for today (absent records have no timeIn — excluded)
        if (dutyRepository.existsByScholarAndDutyDateAndTimeInIsNotNullAndTimeOutIsNull(scholar, today)) {
            throw new RuntimeException("You already have an open duty today. Clock out first.");
        }

        DutyType dutyType = request.getDutyType() != null ? request.getDutyType() : DutyType.REGULAR;

        // regular duty: max 1 per day unless scholar has secondary schedule and primary is done
        if (dutyType == DutyType.REGULAR) {
            List<Duty> regularDuties = dutyRepository.findByScholarAndDutyDateAndDutyTypeAndApprovalStatusNot(
                scholar, today, DutyType.REGULAR, DutyStatus.REJECTED);

            if (!regularDuties.isEmpty()) {
                boolean primaryCompleted = regularDuties.stream()
                    .anyMatch(d -> d.getTimeIn() != null && d.getTimeOut() != null);
                boolean hasSecondary = hasSecondaryScheduleForDate(scholar, today);

                if (regularDuties.size() >= 2) {
                    throw new RuntimeException("You have completed both duty shifts for today.");
                }
                if (!primaryCompleted || !hasSecondary) {
                    throw new RuntimeException("You already have a Regular duty today. Choose Makeup or Overtime instead.");
                }
                // primary is done and secondary schedule exists — allow secondary clock-in
            }
        }

        LocalTime now = LocalTime.now(clock);
        // late/early rules only apply to REGULAR; bypass the schedule check for MAKEUP/OVERTIME
        LocalTime expectedIn = (dutyType == DutyType.REGULAR) ? resolveExpectedTimeIn(scholar, today) : null;
        AttendanceStatus attendanceStatus = AttendanceStatus.PRESENT;

        // regular duty late rules only apply if expected time in is set
        if (dutyType == DutyType.REGULAR && expectedIn != null) {
            // calculate minutes late (negative = early)
            long minutesLate = Duration.between(expectedIn, now).toMinutes();

            // too early: more than 15 minutes before scheduled start
            if (minutesLate < -15) {
                throw new RuntimeException(
                    "Clock-in is too early. You may only clock in up to 15 minutes before your scheduled start time (" +
                    formatTo12Hour(expectedIn) + ").");
            }

            // more than 60 minutes late = ABSENT, cannot clock in
            if (minutesLate > 60) {
                // create absent record
                Duty absentDuty = new Duty();
                absentDuty.setScholar(scholar);
                absentDuty.setSemester(semester);
                absentDuty.setDutyDate(today);
                absentDuty.setDutyType(DutyType.REGULAR);
                absentDuty.setApprovalStatus(DutyStatus.APPROVED);
                absentDuty.setAttendanceStatus(AttendanceStatus.ABSENT);
                absentDuty.setRemarks("Marked absent: Clock-in attempted more than 1 hour late at " + formatTo12Hour(now));
                dutyRepository.save(absentDuty);
                
                throw new RuntimeException(
                    "You are more than 1 hour late for your Regular duty. " +
                    "This has been marked as ABSENT. Please contact your Department Head for a makeup schedule."
                );
            }
            // late status only, allow clockin
            else if (minutesLate >= 10) {
                attendanceStatus = AttendanceStatus.LATE;
            }
            // grace period of 10 minutes
            else {
                attendanceStatus = AttendanceStatus.PRESENT;
            }
        }

        Duty duty = new Duty();
        duty.setScholar(scholar);
        duty.setSemester(semester);
        duty.setDutyDate(today);
        duty.setDutyType(dutyType);
        duty.setTimeIn(now);
        duty.setExpectedTimeOut(resolveExpectedTimeOut(scholar, today));
        duty.setApprovalStatus(DutyStatus.PENDING);
        duty.setAttendanceStatus(attendanceStatus);

        DutyResponse response = DutyResponse.from(dutyRepository.save(duty));

        // late to absent check for late duties
        if (dutyType == DutyType.REGULAR && attendanceStatus == AttendanceStatus.LATE) {
            checkAndApplyLateToAbsent(scholar, semester);
        }

        return response;
    }
 
    // clockout logic
    public DutyResponse clockOut(String schoolId) {
        semesterRepository.findByIsActiveTrue()
            .orElseThrow(() -> new RuntimeException("SEM-001: No active semester available. Duty recording is temporarily disabled."));

        User scholar = userRepository.findBySchoolId(schoolId)
            .orElseThrow(() -> new RuntimeException("User not found"));
 
        Duty duty = dutyRepository
            .findByScholarAndDutyDateAndTimeInIsNotNullAndTimeOutIsNull(scholar, LocalDate.now(clock))
            .orElseThrow(() -> new RuntimeException(
                "No open duty found for today."));

        LocalTime now = LocalTime.now(clock);
 
        // min 1 hr for makeup/overtime
        if (duty.getDutyType() == DutyType.MAKEUP
                || duty.getDutyType() == DutyType.OVERTIME) {
            long minutes = Duration.between(duty.getTimeIn(), now).toMinutes();
            if (minutes < 60) {
                long remaining = 60 - minutes;
                throw new RuntimeException(
                    duty.getDutyType().name() + " duty requires a minimum of "
                    + "1 hour. You need " + remaining
                    + " more minute(s) before you can clock out.");
            }
        }
 
        duty.setTimeOut(now);
        return DutyResponse.from(dutyRepository.save(duty));
    }

    public List<DutyResponse> getMyDuties(String schoolId) {
        User scholar = userRepository.findBySchoolId(schoolId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return dutyRepository.findByScholarOrderByDutyDateDesc(scholar)
            .stream().map(DutyResponse::from).toList();
    }

    public Map<String, Object> getMySummary(String schoolId) {
        User scholar = userRepository.findBySchoolId(schoolId)
            .orElseThrow(() -> new RuntimeException("User not found"));
 
        Semester semester = semesterRepository.findByIsActiveTrue().orElse(null);
        if (semester == null) {
            return Map.of(
                "presentCount", 0L, "lateCount", 0L,
                "absentCount", 0L, "makeupHoursOwed", 0L,
                "makeupHoursRendered", 0L, "makeupHoursRemaining", 0L);
        }
 
        List<Duty> semesterDuties = dutyRepository.findByScholarAndSemesterOrderByDutyDateDesc(scholar, semester);

        // Present = distinct days where REGULAR or OVERTIME duty is PRESENT or LATE (not REJECTED)
        long present = semesterDuties.stream()
            .filter(d -> d.getDutyType() == DutyType.REGULAR || d.getDutyType() == DutyType.OVERTIME)
            .filter(d -> d.getAttendanceStatus() == AttendanceStatus.PRESENT
                      || d.getAttendanceStatus() == AttendanceStatus.LATE)
            .filter(d -> d.getApprovalStatus() != DutyStatus.REJECTED)
            .map(Duty::getDutyDate)
            .distinct()
            .count();

        long late = dutyRepository.countByScholarAndSemesterAndAttendanceStatus(
            scholar, semester, AttendanceStatus.LATE);

        long absent = dutyRepository.countByScholarAndSemesterAndAttendanceStatus(
            scholar, semester, AttendanceStatus.ABSENT);

        long makeupHoursOwed = absent * 4L;

        long makeupHoursRendered = semesterDuties.stream()
            .filter(d -> d.getDutyType() == DutyType.MAKEUP)
            .filter(d -> d.getApprovalStatus() == DutyStatus.APPROVED)
            .filter(d -> d.getTimeIn() != null && d.getTimeOut() != null)
            .mapToLong(d -> Duration.between(d.getTimeIn(), d.getTimeOut()).toHours())
            .sum();

        long makeupHoursRemaining = Math.max(0L, makeupHoursOwed - makeupHoursRendered);
 
        return Map.of(
            "presentCount", present,
            "lateCount", late,
            "absentCount", absent,
            "makeupHoursOwed", makeupHoursOwed,
            "makeupHoursRendered", makeupHoursRendered,
            "makeupHoursRemaining", makeupHoursRemaining
        );
    }
 
    public List<DutyResponse> getPendingDuties(String deptHeadSchoolId) {
        User deptHead = userRepository.findBySchoolId(deptHeadSchoolId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        System.out.println("Dept Head ID: " + deptHead.getUserId());
        System.out.println("Dept Head Branch: " + (deptHead.getBranch() != null ? deptHead.getBranch().getBranchId() : "NULL"));
        System.out.println("Dept Head Dept: " + (deptHead.getDepartment() != null ? deptHead.getDepartment().getDepartmentId() : "NULL"));
        
        Branch branch = deptHead.getBranch();
        if (branch == null) {
            throw new RuntimeException("Department head has no branch assigned. Please contact admin.");
        }
        
        List<Duty> duties = dutyRepository.findByScholarBranchAndApprovalStatusOrderByDutyDateDesc(branch, DutyStatus.PENDING);
        System.out.println("Found duties: " + duties.size());
        
        return duties.stream().map(DutyResponse::from).toList();
    }

    public List<DutyResponse> getBranchDuties(String deptHeadSchoolId) {
        User deptHead = userRepository.findBySchoolId(deptHeadSchoolId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        Branch branch = deptHead.getBranch();
        if (branch == null)
            throw new RuntimeException("No branch assigned.");
        return dutyRepository
            .findByScholarBranchOrderByDutyDateDesc(branch)
            .stream().map(DutyResponse::from).toList();
    }

    public DutyResponse approveDuty(Long dutyId, String deptHeadSchoolId,
                                    ApprovalRequest request) {
        User deptHead = userRepository.findBySchoolId(deptHeadSchoolId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        Duty duty = dutyRepository.findById(dutyId)
            .orElseThrow(() -> new RuntimeException("Duty not found"));
        if (!duty.getScholar().getBranch().getBranchId().equals(deptHead.getBranch().getBranchId()))
            throw new AccessDeniedException("You are not authorized to manage this duty.");
        if (duty.getApprovalStatus() != DutyStatus.PENDING)
            throw new RuntimeException("Only PENDING duties can be approved.");
        duty.setApprovalStatus(DutyStatus.APPROVED);
        duty.setApprovedBy(deptHead);
        duty.setRemarks(request != null ? request.getRemarks() : null);
        DutyResponse saved = DutyResponse.from(dutyRepository.save(duty));
        User scholar = duty.getScholar();
        emailService.sendDutyApprovedEmail(
            scholar.getEmail(),
            scholar.getPersonalGmail(),
            scholar.getFirstName() + " " + scholar.getLastName(),
            duty.getDutyDate().toString(),
            duty.getDutyType().name(),
            duty.getRemarks());
        return saved;
    }

    public DutyResponse rejectDuty(Long dutyId, String deptHeadSchoolId,
                                   ApprovalRequest request) {
        User deptHead = userRepository.findBySchoolId(deptHeadSchoolId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        Duty duty = dutyRepository.findById(dutyId)
            .orElseThrow(() -> new RuntimeException("Duty not found"));
        if (!duty.getScholar().getBranch().getBranchId().equals(deptHead.getBranch().getBranchId()))
            throw new AccessDeniedException("You are not authorized to manage this duty.");
        if (duty.getApprovalStatus() != DutyStatus.PENDING)
            throw new RuntimeException("Only PENDING duties can be rejected.");
        duty.setApprovalStatus(DutyStatus.REJECTED);
        duty.setAttendanceStatus(AttendanceStatus.ABSENT);
        duty.setApprovedBy(deptHead);
        duty.setRemarks(request != null && request.getRemarks() != null
            ? request.getRemarks() : "Rejected by Department Head");
        DutyResponse saved = DutyResponse.from(dutyRepository.save(duty));
        User scholar = duty.getScholar();
        emailService.sendDutyRejectedEmail(
            scholar.getEmail(),
            scholar.getPersonalGmail(),
            scholar.getFirstName() + " " + scholar.getLastName(),
            duty.getDutyDate().toString(),
            duty.getDutyType().name(),
            duty.getRemarks());
        return saved;
    }

    public DutyResponse changeDecision(Long dutyId, String deptHeadSchoolId,
                                       DecisionRequest request) {
        User deptHead = userRepository.findBySchoolId(deptHeadSchoolId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        Duty duty = dutyRepository.findById(dutyId)
            .orElseThrow(() -> new RuntimeException("Duty not found"));
        if (!duty.getScholar().getBranch().getBranchId().equals(deptHead.getBranch().getBranchId()))
            throw new AccessDeniedException("You are not authorized to manage this duty.");
        if (duty.getApprovalStatus() == DutyStatus.PENDING)
            throw new RuntimeException("PENDING duties must use the approve or reject actions.");

        DutyStatus newStatus = request.getNewStatus();
        String remarks = request.getRemarks();
        User scholar = duty.getScholar();

        if (newStatus == DutyStatus.APPROVED) {
            duty.setApprovalStatus(DutyStatus.APPROVED);
            duty.setApprovedBy(deptHead);
            duty.setRemarks(remarks);
            duty.setAttendanceStatus(deriveAttendanceStatus(duty));
            DutyResponse saved = DutyResponse.from(dutyRepository.save(duty));
            emailService.sendDutyApprovedEmail(
                scholar.getEmail(), scholar.getPersonalGmail(),
                scholar.getFirstName() + " " + scholar.getLastName(),
                duty.getDutyDate().toString(), duty.getDutyType().name(), remarks);
            return saved;
        } else if (newStatus == DutyStatus.REJECTED) {
            duty.setApprovalStatus(DutyStatus.REJECTED);
            duty.setAttendanceStatus(AttendanceStatus.ABSENT);
            duty.setApprovedBy(deptHead);
            duty.setRemarks(remarks != null && !remarks.isBlank() ? remarks : "Rejected by Department Head");
            DutyResponse saved = DutyResponse.from(dutyRepository.save(duty));
            emailService.sendDutyRejectedEmail(
                scholar.getEmail(), scholar.getPersonalGmail(),
                scholar.getFirstName() + " " + scholar.getLastName(),
                duty.getDutyDate().toString(), duty.getDutyType().name(), duty.getRemarks());
            return saved;
        } else {
            throw new RuntimeException("Invalid new status. Must be APPROVED or REJECTED.");
        }
    }

    private AttendanceStatus deriveAttendanceStatus(Duty duty) {
        if (duty.getDutyType() != DutyType.REGULAR || duty.getTimeIn() == null) {
            return AttendanceStatus.PRESENT;
        }
        try {
            LocalTime expectedIn = resolveExpectedTimeIn(duty.getScholar(), duty.getDutyDate());
            if (expectedIn == null) return AttendanceStatus.PRESENT;
            long minutesLate = Duration.between(expectedIn, duty.getTimeIn()).toMinutes();
            return minutesLate >= 10 ? AttendanceStatus.LATE : AttendanceStatus.PRESENT;
        } catch (RuntimeException e) {
            LocalTime fallback = duty.getScholar().getExpectedTimeIn();
            if (fallback == null) return AttendanceStatus.PRESENT;
            long minutesLate = Duration.between(fallback, duty.getTimeIn()).toMinutes();
            return minutesLate >= 10 ? AttendanceStatus.LATE : AttendanceStatus.PRESENT;
        }
    }

    public List<com.citu.nasync_backend.shared.dto.response.UserResponse>
            getScholarsByBranch(String deptHeadSchoolId) {
        User deptHead = userRepository.findBySchoolId(deptHeadSchoolId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        Branch branch = deptHead.getBranch();
        if (branch == null)
            throw new RuntimeException("No branch assigned.");
        return userRepository
            .findByBranchAndRole(branch,
                com.citu.nasync_backend.shared.enums.Role.SCHOLAR)
            .stream()
            .map(com.citu.nasync_backend.shared.dto.response.UserResponse::from)
            .toList();
    }

    public Map<String, Object> getScholarDetails(String deptHeadSchoolId, String scholarSchoolId) {
        User deptHead = userRepository.findBySchoolId(deptHeadSchoolId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        Branch branch = deptHead.getBranch();
        if (branch == null)
            throw new RuntimeException("No branch assigned.");

        User scholar = userRepository.findBySchoolId(scholarSchoolId)
            .orElseThrow(() -> new RuntimeException("Scholar not found"));

        if (scholar.getBranch() == null || !scholar.getBranch().getBranchId().equals(branch.getBranchId()))
            throw new AccessDeniedException("Scholar is not in your branch.");

        List<Map<String, Object>> scheduleEntries = scholarScheduleRepository.findByUser(scholar)
            .map(schedule -> schedule.getEntries().stream()
                .sorted(java.util.Comparator.comparing(DailyScheduleEntry::getDayOfWeek))
                .<Map<String, Object>>map(e -> {
                    java.util.LinkedHashMap<String, Object> m = new java.util.LinkedHashMap<>();
                    m.put("dayOfWeek", e.getDayOfWeek().name());
                    m.put("primaryTimeIn", e.getPrimaryTimeIn() != null ? e.getPrimaryTimeIn().toString() : null);
                    m.put("primaryTimeOut", e.getPrimaryTimeOut() != null ? e.getPrimaryTimeOut().toString() : null);
                    m.put("secondaryTimeIn", e.getSecondaryTimeIn() != null ? e.getSecondaryTimeIn().toString() : null);
                    m.put("secondaryTimeOut", e.getSecondaryTimeOut() != null ? e.getSecondaryTimeOut().toString() : null);
                    return m;
                })
                .toList())
            .orElse(List.of());

        List<DutyResponse> dutyHistory = dutyRepository.findByScholarOrderByDutyDateDesc(scholar)
            .stream().map(DutyResponse::from).toList();

        java.util.LinkedHashMap<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("schoolId", scholar.getSchoolId());
        result.put("firstName", scholar.getFirstName());
        result.put("lastName", scholar.getLastName());
        result.put("email", scholar.getEmail());
        result.put("schedule", scheduleEntries);
        result.put("dutyHistory", dutyHistory);
        return result;
    }

    private LocalTime resolveExpectedTimeIn(User scholar, LocalDate date) {
        Optional<ScholarSchedule> scheduleOpt = scholarScheduleRepository.findByUser(scholar);
        if (scheduleOpt.isPresent()) {
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            Optional<DailyScheduleEntry> entryOpt = scheduleOpt.get().getEntries().stream()
                    .filter(e -> e.getDayOfWeek() == dayOfWeek)
                    .findFirst();
            if (entryOpt.isPresent()) {
                DailyScheduleEntry entry = entryOpt.get();
                if (isPrimaryShiftCompleted(scholar, date) && entry.getSecondaryTimeIn() != null) {
                    return entry.getSecondaryTimeIn();
                }
                return entry.getPrimaryTimeIn();
            }
            String dayName = dayOfWeek.name().charAt(0) + dayOfWeek.name().substring(1).toLowerCase();
            throw new RuntimeException(
                "No duty scheduled for " + dayName + ". Clock-in is only allowed on your scheduled days.");
        }
        return scholar.getExpectedTimeIn();
    }

    private LocalTime resolveExpectedTimeOut(User scholar, LocalDate date) {
        Optional<ScholarSchedule> scheduleOpt = scholarScheduleRepository.findByUser(scholar);
        if (scheduleOpt.isPresent()) {
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            Optional<DailyScheduleEntry> entryOpt = scheduleOpt.get().getEntries().stream()
                    .filter(e -> e.getDayOfWeek() == dayOfWeek)
                    .findFirst();
            if (entryOpt.isPresent()) {
                DailyScheduleEntry entry = entryOpt.get();
                if (isPrimaryShiftCompleted(scholar, date) && entry.getSecondaryTimeOut() != null) {
                    return entry.getSecondaryTimeOut();
                }
                return entry.getPrimaryTimeOut();
            }
            return scholar.getExpectedTimeOut();
        }
        return scholar.getExpectedTimeOut();
    }

    private boolean isPrimaryShiftCompleted(User scholar, LocalDate date) {
        return dutyRepository.findByScholarAndDutyDateAndDutyTypeAndApprovalStatusNot(
                scholar, date, DutyType.REGULAR, DutyStatus.REJECTED)
            .stream()
            .anyMatch(d -> d.getTimeIn() != null && d.getTimeOut() != null);
    }

    private boolean hasSecondaryScheduleForDate(User scholar, LocalDate date) {
        return scholarScheduleRepository.findByUser(scholar)
            .map(schedule -> schedule.getEntries().stream()
                .filter(e -> e.getDayOfWeek() == date.getDayOfWeek())
                .anyMatch(e -> e.getSecondaryTimeIn() != null && e.getSecondaryTimeOut() != null))
            .orElse(false);
    }

    private void checkAndApplyLateToAbsent(User scholar, Semester semester) {
        long lateCount = dutyRepository.countByScholarAndSemesterAndAttendanceStatus(
            scholar, semester, com.citu.nasync_backend.shared.enums.AttendanceStatus.LATE);
        
        // 3 lates = 1 absent
        long absentEquivalent = lateCount / 3;
        
        // get current absent count
        long currentAbsent = dutyRepository.countByScholarAndSemesterAndAttendanceStatus(
            scholar, semester, AttendanceStatus.ABSENT);
        
        long targetAbsent = absentEquivalent;
        if (targetAbsent > currentAbsent) {
            long toAdd = targetAbsent - currentAbsent;
            for (int i = 0; i < toAdd; i++) {
                Duty absentDuty = new Duty();
                absentDuty.setScholar(scholar);
                absentDuty.setSemester(semester);
                absentDuty.setDutyDate(LocalDate.now(clock));
                absentDuty.setDutyType(DutyType.REGULAR);
                absentDuty.setApprovalStatus(DutyStatus.APPROVED);
                absentDuty.setAttendanceStatus(AttendanceStatus.ABSENT);
                absentDuty.setRemarks("Auto-converted from 3 lates (conversion at " + formatTo12Hour(LocalTime.now(clock)) + ")");
                dutyRepository.save(absentDuty);
            }
        }
    }
}