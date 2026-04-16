package com.citu.nasync_backend.dto.response;
 
import com.citu.nasync_backend.entity.Semester;
import java.time.LocalDate;
 
public class SemesterResponse {
    private Long semesterId;
    private String label;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isActive;
 
    public static SemesterResponse from(Semester s) {
        SemesterResponse dto = new SemesterResponse();
        dto.semesterId = s.getSemesterId();
        dto.label = s.getLabel();
        dto.startDate = s.getStartDate();
        dto.endDate = s.getEndDate();
        dto.isActive = s.isActive();
        return dto;
    }
 
    public Long getSemesterId() { return semesterId; }
    public String getLabel() { return label; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public boolean isActive() { return isActive; }
    public void setSemesterId(Long id) { this.semesterId = id; }
    public void setLabel(String l) { this.label = l; }
    public void setStartDate(LocalDate d) { this.startDate = d; }
    public void setEndDate(LocalDate d) { this.endDate = d; }
    public void setActive(boolean a) { this.isActive = a; }
}
