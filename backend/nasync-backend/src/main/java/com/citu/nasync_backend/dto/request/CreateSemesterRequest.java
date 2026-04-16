package com.citu.nasync_backend.dto.request;
 
import java.time.LocalDate;
 
public class CreateSemesterRequest {
    private String label;
    private LocalDate startDate;
    private LocalDate endDate;
 
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
