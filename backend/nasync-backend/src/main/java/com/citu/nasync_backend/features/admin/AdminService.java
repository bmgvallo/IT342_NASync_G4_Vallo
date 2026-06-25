package com.citu.nasync_backend.features.admin;

import com.citu.nasync_backend.features.auth.chain.BranchResolutionHandler;
import com.citu.nasync_backend.features.auth.chain.DepartmentResolutionHandler;
import com.citu.nasync_backend.features.auth.chain.EmailDuplicateHandler;
import com.citu.nasync_backend.features.auth.chain.RegistrationContext;
import com.citu.nasync_backend.features.auth.chain.SchoolIdDuplicateHandler;
import com.citu.nasync_backend.features.auth.chain.UserRegistrationHandler;
import com.citu.nasync_backend.features.auth.dto.RegisterUserRequest;
import com.citu.nasync_backend.features.duty.DutyResponse;
import com.citu.nasync_backend.shared.dto.response.UserResponse;
import com.citu.nasync_backend.shared.entity.Branch;
import com.citu.nasync_backend.shared.entity.DailyScheduleEntry;
import com.citu.nasync_backend.shared.entity.Department;
import com.citu.nasync_backend.shared.entity.Duty;
import com.citu.nasync_backend.shared.entity.ScholarSchedule;
import com.citu.nasync_backend.shared.entity.Semester;
import com.citu.nasync_backend.shared.entity.User;
import com.citu.nasync_backend.shared.enums.AttendanceStatus;
import com.citu.nasync_backend.shared.enums.DutyStatus;
import com.citu.nasync_backend.shared.enums.Role;
import com.citu.nasync_backend.shared.repository.BranchRepository;
import com.citu.nasync_backend.shared.repository.DepartmentRepository;
import com.citu.nasync_backend.shared.repository.DutyRepository;
import com.citu.nasync_backend.shared.repository.ScholarScheduleRepository;
import com.citu.nasync_backend.shared.repository.SemesterRepository;
import com.citu.nasync_backend.shared.repository.UserRepository;
import com.citu.nasync_backend.shared.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    @Autowired private UserRepository userRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private BranchRepository branchRepository;
    @Autowired private DutyRepository dutyRepository;
    @Autowired private SemesterRepository semesterRepository;
    @Autowired private ScholarScheduleRepository scholarScheduleRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EmailService emailService;
    @Autowired private Clock clock;


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

        if (user.getPersonalGmail() != null && !user.getPersonalGmail().isBlank()) {
            try {
                String deptName = context.getDepartment() != null ? context.getDepartment().getName() : "N/A";
                String branchName = context.getBranch() != null ? context.getBranch().getName() : "N/A";
                emailService.sendWelcomeEmail(
                    user.getEmail(),
                    user.getPersonalGmail(),
                    user.getFirstName() + " " + user.getLastName(),
                    user.getSchoolId(),
                    user.getSchoolId(),
                    deptName,
                    branchName);
            } catch (Exception e) {
                log.warn("Welcome email failed for {}: {}", user.getSchoolId(), e.getMessage());
            }
        }

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

    public DashboardStatsResponse getDashboardStats() {
        long totalScholars = userRepository.countByRole(Role.SCHOLAR);
        long pendingApprovals = dutyRepository.countByApprovalStatus(DutyStatus.PENDING);

        Semester activeSemester = semesterRepository.findByIsActiveTrue().orElse(null);
        long totalAbsencesThisSemester = activeSemester != null
            ? dutyRepository.countBySemesterAndAttendanceStatus(activeSemester, AttendanceStatus.ABSENT)
            : 0L;

        long activeDutiesToday = dutyRepository.countByDutyDateAndTimeOutIsNull(LocalDate.now(clock));

        return new DashboardStatsResponse(totalScholars, pendingApprovals,
            totalAbsencesThisSemester, activeDutiesToday);
    }

    public ScholarDutySummaryResponse getScholarDutySummary(String schoolId) {
        User scholar = userRepository.findBySchoolId(schoolId)
                .orElseThrow(() -> new RuntimeException("Scholar not found"));

        Semester semester = semesterRepository.findByIsActiveTrue().orElse(null);
        if (semester == null) {
            return new ScholarDutySummaryResponse(0, 0, 0, 0, 0);
        }

        List<Duty> semesterDuties = dutyRepository.findByScholarAndSemesterOrderByDutyDateDesc(scholar, semester);

        double completedHours = semesterDuties.stream()
                .filter(d -> d.getApprovalStatus() != DutyStatus.REJECTED)
                .filter(d -> d.getTimeIn() != null && d.getTimeOut() != null)
                .mapToDouble(d -> Duration.between(d.getTimeIn(), d.getTimeOut()).toMinutes() / 60.0)
                .sum();
        completedHours = Math.round(completedHours * 10.0) / 10.0;

        long totalLates = dutyRepository.countByScholarAndSemesterAndAttendanceStatus(
                scholar, semester, AttendanceStatus.LATE);

        long absentCount = dutyRepository.countByScholarAndSemesterAndAttendanceStatus(
                scholar, semester, AttendanceStatus.ABSENT);
        double missedHours = absentCount * 4.0;

        double totalRequiredHours = computeTotalRequiredHours(scholar, semester);
        double remainingHours = Math.max(0, Math.round((totalRequiredHours - completedHours) * 10.0) / 10.0);

        return new ScholarDutySummaryResponse(
                totalRequiredHours, completedHours, missedHours, totalLates, remainingHours);
    }

    public List<DutyResponse> getScholarDutyHistory(String schoolId) {
        User scholar = userRepository.findBySchoolId(schoolId)
                .orElseThrow(() -> new RuntimeException("Scholar not found"));
        return dutyRepository.findByScholarOrderByDutyDateDesc(scholar)
                .stream().map(DutyResponse::from).toList();
    }

    public void sendDutyReminder(String schoolId) {
        User scholar = userRepository.findBySchoolId(schoolId)
                .orElseThrow(() -> new RuntimeException("Scholar not found"));
        ScholarDutySummaryResponse summary = getScholarDutySummary(schoolId);
        emailService.sendDutyReminderEmail(
                scholar.getEmail(),
                scholar.getPersonalGmail(),
                scholar.getFirstName() + " " + scholar.getLastName(),
                summary.getTotalRequiredHours(),
                summary.getCompletedHours(),
                summary.getRemainingHours(),
                summary.getTotalLates(),
                summary.getMissedHours());
    }

    private double computeTotalRequiredHours(User scholar, Semester semester) {
        List<DailyScheduleEntry> entries = scholarScheduleRepository.findByUser(scholar)
                .map(ScholarSchedule::getEntries)
                .orElse(List.of());
        if (entries.isEmpty()) return 0;

        long weeks = Math.max(1, ChronoUnit.WEEKS.between(
                semester.getStartDate(), semester.getEndDate()));

        double weeklyHours = entries.stream()
                .mapToDouble(e -> {
                    double primary = 0;
                    if (e.getPrimaryTimeIn() != null && e.getPrimaryTimeOut() != null) {
                        primary = Duration.between(e.getPrimaryTimeIn(), e.getPrimaryTimeOut())
                                .toMinutes() / 60.0;
                    }
                    double secondary = 0;
                    if (e.getSecondaryTimeIn() != null && e.getSecondaryTimeOut() != null) {
                        secondary = Duration.between(e.getSecondaryTimeIn(), e.getSecondaryTimeOut())
                                .toMinutes() / 60.0;
                    }
                    return primary + secondary;
                })
                .sum();

        return Math.round(weeklyHours * weeks * 10.0) / 10.0;
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