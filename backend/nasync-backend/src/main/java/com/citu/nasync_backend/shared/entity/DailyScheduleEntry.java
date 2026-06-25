package com.citu.nasync_backend.shared.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "daily_schedule_entries")
public class DailyScheduleEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "schedule_id", nullable = false)
    @JsonIgnore
    private ScholarSchedule schedule;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(name = "primary_time_in", nullable = false)
    private LocalTime primaryTimeIn;

    @Column(name = "primary_time_out", nullable = false)
    private LocalTime primaryTimeOut;

    @Column(name = "secondary_time_in")
    private LocalTime secondaryTimeIn;

    @Column(name = "secondary_time_out")
    private LocalTime secondaryTimeOut;

    public Long getId() { return id; }
    public ScholarSchedule getSchedule() { return schedule; }
    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public LocalTime getPrimaryTimeIn() { return primaryTimeIn; }
    public LocalTime getPrimaryTimeOut() { return primaryTimeOut; }
    public LocalTime getSecondaryTimeIn() { return secondaryTimeIn; }
    public LocalTime getSecondaryTimeOut() { return secondaryTimeOut; }

    public void setId(Long id) { this.id = id; }
    public void setSchedule(ScholarSchedule schedule) { this.schedule = schedule; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public void setPrimaryTimeIn(LocalTime primaryTimeIn) { this.primaryTimeIn = primaryTimeIn; }
    public void setPrimaryTimeOut(LocalTime primaryTimeOut) { this.primaryTimeOut = primaryTimeOut; }
    public void setSecondaryTimeIn(LocalTime secondaryTimeIn) { this.secondaryTimeIn = secondaryTimeIn; }
    public void setSecondaryTimeOut(LocalTime secondaryTimeOut) { this.secondaryTimeOut = secondaryTimeOut; }
}
