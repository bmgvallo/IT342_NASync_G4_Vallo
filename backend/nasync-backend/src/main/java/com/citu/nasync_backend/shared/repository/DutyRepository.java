package com.citu.nasync_backend.shared.repository;
 
import com.citu.nasync_backend.shared.entity.Branch;
import com.citu.nasync_backend.shared.entity.Duty;
import com.citu.nasync_backend.shared.entity.Semester;
import com.citu.nasync_backend.shared.entity.User;
import com.citu.nasync_backend.shared.enums.DutyStatus;
import com.citu.nasync_backend.shared.enums.AttendanceStatus;
import com.citu.nasync_backend.shared.enums.DutyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
 
@Repository
public interface DutyRepository extends JpaRepository<Duty, Long> {
    
    List<Duty> findByScholarOrderByDutyDateDesc(User scholar);
    
    List<Duty> findByScholarAndSemesterOrderByDutyDateDesc(User scholar, Semester semester);
    
    Optional<Duty> findByScholarAndDutyDateAndTimeOutIsNull(User scholar, LocalDate date);
    
    boolean existsByScholarAndDutyDateAndTimeOutIsNull(User scholar, LocalDate date);

    // excludes absent records (timeIn is null) — used for open duty checks
    boolean existsByScholarAndDutyDateAndTimeInIsNotNullAndTimeOutIsNull(User scholar, LocalDate date);

    Optional<Duty> findByScholarAndDutyDateAndTimeInIsNotNullAndTimeOutIsNull(User scholar, LocalDate date);

    // list form — used for secondary shift eligibility checks
    List<Duty> findByScholarAndDutyDateAndDutyTypeAndApprovalStatusNot(
        User scholar, LocalDate date, DutyType dutyType, DutyStatus approvalStatus);

    List<Duty> findByScholarAndSemesterAndApprovalStatus(User scholar, Semester semester, DutyStatus approvalStatus);
    
    long countByScholarAndSemesterAndApprovalStatus(User scholar, Semester semester, DutyStatus approvalStatus);
    
    List<Duty> findByScholarBranchAndApprovalStatusOrderByDutyDateDesc(Branch branch, DutyStatus approvalStatus);
    
    List<Duty> findByScholarBranchOrderByDutyDateDesc(Branch branch);
    
    List<Duty> findByScholarAndSemesterAndAttendanceStatus(User scholar, Semester semester, AttendanceStatus attendanceStatus);
    
    long countByScholarAndSemesterAndAttendanceStatus(User scholar, Semester semester, AttendanceStatus attendanceStatus);
    
    List<Duty> findByScholarAndSemesterAndDutyTypeAndApprovalStatus(
        User scholar, Semester semester, DutyType dutyType, DutyStatus approvalStatus);
    
    boolean existsByScholarAndDutyDateAndDutyTypeAndApprovalStatusNot(
        User scholar, LocalDate date, DutyType dutyType, DutyStatus approvalStatus);
    
    long countByApprovalStatus(DutyStatus approvalStatus);

    long countBySemesterAndAttendanceStatus(Semester semester, AttendanceStatus attendanceStatus);

    long countByDutyDateAndTimeOutIsNull(LocalDate date);

    List<Duty> findByDutyDateAndTimeInIsNotNullAndTimeOutIsNull(LocalDate date);

    @Query("SELECT d FROM Duty d WHERE d.scholar.branch = :branch AND d.approvalStatus = :status ORDER BY d.dutyDate DESC")
    List<Duty> findPendingByBranch(@Param("branch") Branch branch, @Param("status") DutyStatus status);
}