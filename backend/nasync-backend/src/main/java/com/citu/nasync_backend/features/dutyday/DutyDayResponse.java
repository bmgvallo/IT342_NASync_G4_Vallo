package com.citu.nasync_backend.features.dutyday;
 
import com.citu.nasync_backend.shared.entity.DutyDay;
import com.citu.nasync_backend.shared.enums.DayType;
import java.time.LocalDate;
 
public class DutyDayResponse {
    private Long dutyDayId;
    private Long semesterId;
    private LocalDate dayDate;
    private DayType dayType;
    private String description;
 
    public static DutyDayResponse from(DutyDay d) {
        DutyDayResponse dto = new DutyDayResponse();
        dto.dutyDayId = d.getDutyDayId();
        dto.semesterId = d.getSemester().getSemesterId();
        dto.dayDate = d.getDayDate();
        dto.dayType = d.getDayType();
        dto.description = d.getDescription();
        return dto;
    }
 
    public Long getDutyDayId() { return dutyDayId; }
    public Long getSemesterId() { return semesterId; }
    public LocalDate getDayDate() { return dayDate; }
    public DayType getDayType() { return dayType; }
    public String getDescription() { return description; }
    public void setDutyDayId(Long id) { this.dutyDayId = id; }
    public void setSemesterId(Long id) { this.semesterId = id; }
    public void setDayDate(LocalDate d) { this.dayDate = d; }
    public void setDayType(DayType t) { this.dayType = t; }
    public void setDescription(String d) { this.description = d; }
}
