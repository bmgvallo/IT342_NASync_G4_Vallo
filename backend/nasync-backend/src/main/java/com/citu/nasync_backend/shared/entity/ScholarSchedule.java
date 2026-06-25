package com.citu.nasync_backend.shared.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "scholar_schedules")
public class ScholarSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnore
    private User user;

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DailyScheduleEntry> entries = new ArrayList<>();

    public Long getId() { return id; }
    public User getUser() { return user; }
    public List<DailyScheduleEntry> getEntries() { return entries; }

    public void setId(Long id) { this.id = id; }
    public void setUser(User user) { this.user = user; }
    public void setEntries(List<DailyScheduleEntry> entries) { this.entries = entries; }
}
