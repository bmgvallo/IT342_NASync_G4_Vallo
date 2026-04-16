package com.citu.nasync_backend.dto.request;
 
import com.citu.nasync_backend.enums.DutyType;
 
public class ClockInRequest {
    private DutyType dutyType = DutyType.REGULAR;
 
    public DutyType getDutyType() { return dutyType; }
    public void setDutyType(DutyType dutyType) { this.dutyType = dutyType; }
}
