package com.citu.nasync_backend.shared.entity;
 
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
 
@Entity
@Table(name = "semesters")
public class Semester {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "semester_id")
    private Long semesterId;
 
    @Column(name = "label", nullable = false)
    private String label;
 
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
 
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
 
    @Column(name = "is_active", nullable = false)
    private boolean isActive = false;
 
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
 
    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }
 
    public Semester() {}
 
    public Long getSemesterId() { return semesterId; }
    public String getLabel() { return label; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public boolean isActive() { return isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
 
    public void setSemesterId(Long id) { this.semesterId = id; }
    public void setLabel(String label) { this.label = label; }
    public void setStartDate(LocalDate d) { this.startDate = d; }
    public void setEndDate(LocalDate d) { this.endDate = d; }
    public void setActive(boolean active) { this.isActive = active; }
}
