package com.citu.nasync_backend.features.schedule;

import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;

public class ScheduleEntryRequest {

    @NotNull(message = "dayOfWeek is required")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "primaryTimeIn is required")
    private LocalTime primaryTimeIn;

    @NotNull(message = "primaryTimeOut is required")
    private LocalTime primaryTimeOut;

    private LocalTime secondaryTimeIn;
    private LocalTime secondaryTimeOut;

    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public LocalTime getPrimaryTimeIn() { return primaryTimeIn; }
    public LocalTime getPrimaryTimeOut() { return primaryTimeOut; }
    public LocalTime getSecondaryTimeIn() { return secondaryTimeIn; }
    public LocalTime getSecondaryTimeOut() { return secondaryTimeOut; }

    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public void setPrimaryTimeIn(LocalTime primaryTimeIn) { this.primaryTimeIn = primaryTimeIn; }
    public void setPrimaryTimeOut(LocalTime primaryTimeOut) { this.primaryTimeOut = primaryTimeOut; }
    public void setSecondaryTimeIn(LocalTime secondaryTimeIn) { this.secondaryTimeIn = secondaryTimeIn; }
    public void setSecondaryTimeOut(LocalTime secondaryTimeOut) { this.secondaryTimeOut = secondaryTimeOut; }
}
