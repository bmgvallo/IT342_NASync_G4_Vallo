package com.citu.nasync_backend.features.duty;
 
import com.citu.nasync_backend.features.duty.ClockInRequest;
import com.citu.nasync_backend.features.duty.ApprovalRequest;
import com.citu.nasync_backend.features.duty.DutyResponse;
import com.citu.nasync_backend.shared.entity.*;
import com.citu.nasync_backend.shared.enums.DayType;
import com.citu.nasync_backend.shared.enums.DutyStatus;
import com.citu.nasync_backend.shared.enums.AttendanceStatus;
import com.citu.nasync_backend.shared.enums.DutyType;
import com.citu.nasync_backend.shared.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
 
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
 
@Service
public class DutyService {
 
    @Autowired private DutyRepository dutyRepository;
    @Autowired private SemesterRepository semesterRepository;
    @Autowired private DutyDayRepository dutyDayRepository;
    @Autowired private UserRepository userRepository;
 
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
            .orElseThrow(() -> new RuntimeException("No active semester. Contact OAS Admin."));

        LocalDate today = LocalDate.now();

        // no clock in for holiday/suspended days
        dutyDayRepository.findByDayDateAndSemester(today, semester)
            .ifPresent(dd -> {
                if (dd.getDayType() == DayType.HOLIDAY || dd.getDayType() == DayType.SUSPENDED) {
                    throw new RuntimeException("Clock-in not allowed. Today is marked as " + dd.getDayType().name() + ".");
                }
            });

        // ensure no open duty already exists for today
        if (dutyRepository.existsByScholarAndDutyDateAndTimeOutIsNull(scholar, today)) {
            throw new RuntimeException("You already have an open duty today. Clock out first.");
        }

        DutyType dutyType = request.getDutyType() != null ? request.getDutyType() : DutyType.REGULAR;

        // regular duty max 1 per day, makeup/overtime can have multiple
        if (dutyType == DutyType.REGULAR) {
            boolean hasRegular = dutyRepository.existsByScholarAndDutyDateAndDutyTypeAndApprovalStatusNot(
                scholar, today, DutyType.REGULAR, DutyStatus.REJECTED);
            if (hasRegular) {
                throw new RuntimeException("You already have a Regular duty today. Choose Makeup or Overtime instead.");
            }
        }

        LocalTime now = LocalTime.now();
        LocalTime expectedIn = scholar.getExpectedTimeIn();
        AttendanceStatus attendanceStatus = AttendanceStatus.PRESENT;

        // regular duty late rules only apply if expected time in is set
        if (dutyType == DutyType.REGULAR && expectedIn != null) {
            // calculate minutes late
            long minutesLate = Duration.between(expectedIn, now).toMinutes();
            
            // 60 minutes or more late = ABSENT, cannot clock in
            if (minutesLate >= 60) {
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
        User scholar = userRepository.findBySchoolId(schoolId)
            .orElseThrow(() -> new RuntimeException("User not found"));
 
        Duty duty = dutyRepository
            .findByScholarAndDutyDateAndTimeOutIsNull(scholar, LocalDate.now())
            .orElseThrow(() -> new RuntimeException(
                "No open duty found for today."));
 
        LocalTime now = LocalTime.now();
 
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
 
        long present = dutyRepository.countByScholarAndSemesterAndAttendanceStatus(
            scholar, semester, AttendanceStatus.PRESENT);
 
        long late = dutyRepository.countByScholarAndSemesterAndAttendanceStatus(
            scholar, semester, AttendanceStatus.LATE);
 
        long absent = dutyRepository.countByScholarAndSemesterAndAttendanceStatus(
            scholar, semester, AttendanceStatus.ABSENT);

        long makeupHoursOwed = absent * 4L;
 
        List<Duty> approvedMakeups = dutyRepository.findByScholarAndSemesterAndDutyTypeAndApprovalStatus(
            scholar, semester, DutyType.MAKEUP, DutyStatus.APPROVED);
 
        long makeupHoursRendered = approvedMakeups.stream()
            .filter(d -> d.getTimeIn() != null && d.getTimeOut() != null)
            .mapToLong(d -> Duration.between(d.getTimeIn(), d.getTimeOut())
                .toHours())
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
        if (duty.getApprovalStatus() != DutyStatus.PENDING)
            throw new RuntimeException("Only PENDING duties can be approved.");
        duty.setApprovalStatus(DutyStatus.APPROVED);
        duty.setApprovedBy(deptHead);
        duty.setRemarks(request != null ? request.getRemarks() : null);
        return DutyResponse.from(dutyRepository.save(duty));
    }

    public DutyResponse rejectDuty(Long dutyId, String deptHeadSchoolId,
                                   ApprovalRequest request) {
        User deptHead = userRepository.findBySchoolId(deptHeadSchoolId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        Duty duty = dutyRepository.findById(dutyId)
            .orElseThrow(() -> new RuntimeException("Duty not found"));
        if (duty.getApprovalStatus() != DutyStatus.PENDING)
            throw new RuntimeException("Only PENDING duties can be rejected.");
        duty.setApprovalStatus(DutyStatus.REJECTED);
        duty.setApprovedBy(deptHead);
        duty.setRemarks(request != null && request.getRemarks() != null
            ? request.getRemarks() : "Rejected by Department Head");
        return DutyResponse.from(dutyRepository.save(duty));
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
                absentDuty.setDutyDate(LocalDate.now());
                absentDuty.setDutyType(DutyType.REGULAR);
                absentDuty.setApprovalStatus(DutyStatus.APPROVED);
                absentDuty.setAttendanceStatus(AttendanceStatus.ABSENT);
                absentDuty.setRemarks("Auto-converted from 3 lates (conversion at " + formatTo12Hour(LocalTime.now()) + ")");
                dutyRepository.save(absentDuty);
            }
        }
    }
}