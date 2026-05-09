package com.citu.nasync_backend.shared.entity;
 
import com.citu.nasync_backend.shared.enums.DayType;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
 
@Entity
@Table(name = "duty_days")
public class DutyDay {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "duty_day_id")
    private Long dutyDayId;
 
    @ManyToOne
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;
 
    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
 
    @Column(name = "day_date", nullable = false)
    private LocalDate dayDate;
 
    @Enumerated(EnumType.STRING)
    @Column(name = "day_type", nullable = false)
    private DayType dayType;
 
    @Column(name = "description")
    private String description;
 
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
 
    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }
 
    public DutyDay() {}
 
    public Long getDutyDayId() { return dutyDayId; }
    public Semester getSemester() { return semester; }
    public User getCreatedBy() { return createdBy; }
    public LocalDate getDayDate() { return dayDate; }
    public DayType getDayType() { return dayType; }
    public String getDescription() { return description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
 
    public void setDutyDayId(Long id) { this.dutyDayId = id; }
    public void setSemester(Semester s) { this.semester = s; }
    public void setCreatedBy(User u) { this.createdBy = u; }
    public void setDayDate(LocalDate d) { this.dayDate = d; }
    public void setDayType(DayType t) { this.dayType = t; }
    public void setDescription(String d) { this.description = d; }
}
