package com.citu.nasync_backend.features.duty;
 
import com.citu.nasync_backend.shared.entity.Duty;
import com.citu.nasync_backend.shared.enums.DutyStatus;
import com.citu.nasync_backend.shared.enums.AttendanceStatus;
import com.citu.nasync_backend.shared.enums.DutyType;
import java.time.LocalDate;
import java.time.LocalTime;
 
public class DutyResponse {
    private Long dutyId;
    private Long scholarId;
    private String scholarName;
    private String scholarSchoolId;
    private String branchName;
    private Long semesterId;
    private LocalDate dutyDate;
    private DutyType dutyType;
    private LocalTime timeIn;
    private LocalTime timeOut;
    private LocalTime expectedTimeOut;
    private DutyStatus approvalStatus;
    private AttendanceStatus attendanceStatus;
    private String remarks;
    private String approvedByName;
 
    public static DutyResponse from(Duty d) {
        DutyResponse dto = new DutyResponse();
        dto.dutyId = d.getDutyId();
        dto.scholarId = d.getScholar().getUserId();
        dto.scholarName = d.getScholar().getFirstName() + " " + d.getScholar().getLastName();
        dto.scholarSchoolId = d.getScholar().getSchoolId();
        dto.branchName = d.getScholar().getBranch() != null
            ? d.getScholar().getBranch().getName() : null;
        dto.semesterId = d.getSemester().getSemesterId();
        dto.dutyDate = d.getDutyDate();
        dto.dutyType = d.getDutyType();
        dto.timeIn = d.getTimeIn();
        dto.timeOut = d.getTimeOut();
        dto.expectedTimeOut = d.getExpectedTimeOut();
        dto.approvalStatus = d.getApprovalStatus();
        dto.attendanceStatus = d.getAttendanceStatus();
        dto.remarks = d.getRemarks();
        if (d.getApprovedBy() != null)
            dto.approvedByName = d.getApprovedBy().getFirstName()
                + " " + d.getApprovedBy().getLastName();
        return dto;
    }
 
    public Long getDutyId() { return dutyId; }
    public Long getScholarId() { return scholarId; }
    public String getScholarName() { return scholarName; }
    public String getScholarSchoolId() { return scholarSchoolId; }
    public String getBranchName() { return branchName; }
    public Long getSemesterId() { return semesterId; }
    public LocalDate getDutyDate() { return dutyDate; }
    public DutyType getDutyType() { return dutyType; }
    public LocalTime getTimeIn() { return timeIn; }
    public LocalTime getTimeOut() { return timeOut; }
    public LocalTime getExpectedTimeOut() { return expectedTimeOut; }
    public DutyStatus getApprovalStatus() { return approvalStatus; }
    public AttendanceStatus getAttendanceStatus() { return attendanceStatus; }
    public String getRemarks() { return remarks; }
    public String getApprovedByName() { return approvedByName; }
}