package com.citu.nasync_backend.shared.entity;
 
import com.citu.nasync_backend.shared.enums.DutyStatus;
import com.citu.nasync_backend.shared.enums.AttendanceStatus;
import com.citu.nasync_backend.shared.enums.DutyType;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
 
@Entity
@Table(name = "duties")
public class Duty {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "duty_id")
    private Long dutyId;
 
    @ManyToOne
    @JoinColumn(name = "scholar_id", nullable = false)
    private User scholar;
 
    @ManyToOne
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;
 
    @ManyToOne
    @JoinColumn(name = "approved_by")
    private User approvedBy;
 
    @Column(name = "duty_date", nullable = false)
    private LocalDate dutyDate;
 
    @Enumerated(EnumType.STRING)
    @Column(name = "duty_type", nullable = false)
    private DutyType dutyType = DutyType.REGULAR;
 
    @Column(name = "time_in")
    private LocalTime timeIn;
 
    @Column(name = "time_out")
    private LocalTime timeOut;
 
    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false)
    private DutyStatus approvalStatus = DutyStatus.PENDING;
 
    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_status")
    private AttendanceStatus attendanceStatus;
 
    @Column(name = "remarks")
    private String remarks;
 
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
 
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
 
    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }
 
    @PreUpdate
    protected void onUpdate() { this.updatedAt = LocalDateTime.now(); }
 
    public Duty() {}
 
    // Getters
    public Long getDutyId() { return dutyId; }
    public User getScholar() { return scholar; }
    public Semester getSemester() { return semester; }
    public User getApprovedBy() { return approvedBy; }
    public LocalDate getDutyDate() { return dutyDate; }
    public DutyType getDutyType() { return dutyType; }
    public LocalTime getTimeIn() { return timeIn; }
    public LocalTime getTimeOut() { return timeOut; }
    public DutyStatus getApprovalStatus() { return approvalStatus; }
    public AttendanceStatus getAttendanceStatus() { return attendanceStatus; }
    public String getRemarks() { return remarks; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
 
    // Setters
    public void setDutyId(Long id) { this.dutyId = id; }
    public void setScholar(User u) { this.scholar = u; }
    public void setSemester(Semester s) { this.semester = s; }
    public void setApprovedBy(User u) { this.approvedBy = u; }
    public void setDutyDate(LocalDate d) { this.dutyDate = d; }
    public void setDutyType(DutyType t) { this.dutyType = t; }
    public void setTimeIn(LocalTime t) { this.timeIn = t; }
    public void setTimeOut(LocalTime t) { this.timeOut = t; }
    public void setApprovalStatus(DutyStatus s) { this.approvalStatus = s; }
    public void setAttendanceStatus(AttendanceStatus s) { this.attendanceStatus = s; }
    public void setRemarks(String r) { this.remarks = r; }
}