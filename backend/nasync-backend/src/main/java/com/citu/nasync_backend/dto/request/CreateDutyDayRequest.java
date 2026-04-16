package com.citu.nasync_backend.dto.request;
 
import com.citu.nasync_backend.enums.DayType;
import java.time.LocalDate;
 
public class CreateDutyDayRequest {
    private Long semesterId;
    private LocalDate dayDate;
    private DayType dayType;
    private String description;
 
    public Long getSemesterId() { return semesterId; }
    public void setSemesterId(Long id) { this.semesterId = id; }
    public LocalDate getDayDate() { return dayDate; }
    public void setDayDate(LocalDate d) { this.dayDate = d; }
    public DayType getDayType() { return dayType; }
    public void setDayType(DayType t) { this.dayType = t; }
    public String getDescription() { return description; }
    public void setDescription(String d) { this.description = d; }
}
