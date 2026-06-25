package com.citu.nasync_backend.features.schedule;

import com.citu.nasync_backend.shared.entity.DailyScheduleEntry;
import com.citu.nasync_backend.shared.entity.ScholarSchedule;
import com.citu.nasync_backend.shared.entity.User;
import com.citu.nasync_backend.shared.repository.ScholarScheduleRepository;
import com.citu.nasync_backend.shared.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ScholarScheduleService {

    @Autowired private ScholarScheduleRepository scheduleRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private Clock clock;

    @Transactional
    public void setSchedule(String schoolId, List<ScheduleEntryRequest> entries) {
        User scholar = userRepository.findBySchoolId(schoolId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ScholarSchedule schedule = scheduleRepository.findByUser(scholar)
                .orElseGet(() -> {
                    ScholarSchedule s = new ScholarSchedule();
                    s.setUser(scholar);
                    return s;
                });

        schedule.getEntries().clear();
        for (ScheduleEntryRequest req : entries) {
            DailyScheduleEntry entry = new DailyScheduleEntry();
            entry.setSchedule(schedule);
            entry.setDayOfWeek(req.getDayOfWeek());
            entry.setPrimaryTimeIn(req.getPrimaryTimeIn());
            entry.setPrimaryTimeOut(req.getPrimaryTimeOut());
            entry.setSecondaryTimeIn(req.getSecondaryTimeIn());
            entry.setSecondaryTimeOut(req.getSecondaryTimeOut());
            schedule.getEntries().add(entry);
        }

        scheduleRepository.save(schedule);
    }

    public List<DailyScheduleEntry> getSchedule(String schoolId) {
        User scholar = userRepository.findBySchoolId(schoolId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return scheduleRepository.findByUser(scholar)
                .map(ScholarSchedule::getEntries)
                .orElse(List.of());
    }

    public Map<String, Object> getTodayEntry(String schoolId) {
        User scholar = userRepository.findBySchoolId(schoolId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        DayOfWeek today = LocalDate.now(clock).getDayOfWeek();
        Optional<DailyScheduleEntry> entryOpt = scheduleRepository.findByUser(scholar)
                .flatMap(schedule -> schedule.getEntries().stream()
                        .filter(e -> e.getDayOfWeek() == today)
                        .findFirst());
        if (entryOpt.isEmpty()) return null;
        DailyScheduleEntry entry = entryOpt.get();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("primaryTimeIn",    entry.getPrimaryTimeIn()    != null ? entry.getPrimaryTimeIn().toString()    : null);
        m.put("primaryTimeOut",   entry.getPrimaryTimeOut()   != null ? entry.getPrimaryTimeOut().toString()   : null);
        m.put("secondaryTimeIn",  entry.getSecondaryTimeIn()  != null ? entry.getSecondaryTimeIn().toString()  : null);
        m.put("secondaryTimeOut", entry.getSecondaryTimeOut() != null ? entry.getSecondaryTimeOut().toString() : null);
        return m;
    }
}
