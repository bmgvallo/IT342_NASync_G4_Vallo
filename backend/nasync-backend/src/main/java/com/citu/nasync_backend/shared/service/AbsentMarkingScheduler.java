package com.citu.nasync_backend.shared.service;

import com.citu.nasync_backend.shared.entity.*;
import com.citu.nasync_backend.shared.enums.*;
import com.citu.nasync_backend.shared.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Component
public class AbsentMarkingScheduler {

    @Autowired private UserRepository userRepository;
    @Autowired private ScholarScheduleRepository scholarScheduleRepository;
    @Autowired private DutyRepository dutyRepository;
    @Autowired private SemesterRepository semesterRepository;
    @Autowired private DutyDayRepository dutyDayRepository;
    @Autowired private Clock clock;

    @Transactional
    @Scheduled(fixedDelay = 30000)
    public void markAbsentScholarsForToday() {
        System.out.println("[AbsentScheduler] Running at " + LocalTime.now(clock));

        Semester semester = semesterRepository.findByIsActiveTrue().orElse(null);
        if (semester == null) {
            System.out.println("[AbsentScheduler] No active semester — skipping.");
            return;
        }

        LocalDate today = LocalDate.now(clock);
        LocalTime now = LocalTime.now(clock);

        if (today.isBefore(semester.getStartDate()) || today.isAfter(semester.getEndDate())) {
            System.out.println("[AbsentScheduler] Today " + today + " is outside semester range " +
                semester.getStartDate() + " to " + semester.getEndDate() + " — skipping.");
            return;
        }

        // skip holidays and suspended days
        Optional<DutyDay> dutyDay = dutyDayRepository.findByDayDateAndSemester(today, semester);
        if (dutyDay.isPresent()) {
            DayType type = dutyDay.get().getDayType();
            if (type == DayType.HOLIDAY || type == DayType.SUSPENDED) {
                System.out.println("[AbsentScheduler] Today is " + type + " — skipping.");
                return;
            }
        }

        List<User> scholars = userRepository.findByRoleAndIsActive(Role.SCHOLAR, true);
        System.out.println("[AbsentScheduler] Checking " + scholars.size() + " active scholars...");

        for (User scholar : scholars) {
            LocalTime expectedIn = resolveExpectedTimeIn(scholar, today);
            if (expectedIn == null) {
                System.out.println("[AbsentScheduler] " + scholar.getSchoolId() + " — no expectedTimeIn for today, skipping.");
                continue;
            }

            long minutesLate = Duration.between(expectedIn, now).toMinutes();
            System.out.println("[AbsentScheduler] " + scholar.getSchoolId() +
                " expectedIn=" + expectedIn + " now=" + now + " minutesLate=" + minutesLate);

            if (minutesLate <= 60) continue;

            // already has a REGULAR record for today (present, late, or absent) — skip
            boolean alreadyHasRegular = dutyRepository
                .existsByScholarAndDutyDateAndDutyTypeAndApprovalStatusNot(
                    scholar, today, DutyType.REGULAR, DutyStatus.REJECTED);
            if (alreadyHasRegular) {
                System.out.println("[AbsentScheduler] " + scholar.getSchoolId() + " already has a REGULAR duty today — skipping.");
                continue;
            }

            Duty absentDuty = new Duty();
            absentDuty.setScholar(scholar);
            absentDuty.setSemester(semester);
            absentDuty.setDutyDate(today);
            absentDuty.setDutyType(DutyType.REGULAR);
            absentDuty.setApprovalStatus(DutyStatus.APPROVED);
            absentDuty.setAttendanceStatus(AttendanceStatus.ABSENT);
            absentDuty.setRemarks("Auto-marked absent: Did not clock in by " + formatTo12Hour(expectedIn));
            dutyRepository.save(absentDuty);
            System.out.println("[AbsentScheduler] Marked " + scholar.getSchoolId() + " ABSENT for today.");
        }
    }

    @Transactional
    @Scheduled(fixedDelay = 60000)
    public void autoMarkAbsentForOpenDuties() {
        LocalDate today = LocalDate.now(clock);
        LocalTime now = LocalTime.now(clock);

        List<Duty> openDuties = dutyRepository.findByDutyDateAndTimeInIsNotNullAndTimeOutIsNull(today);
        for (Duty duty : openDuties) {
            if (duty.getExpectedTimeOut() == null) continue;
            if (!now.isAfter(duty.getExpectedTimeOut().plusHours(1))) continue;
            if (duty.getApprovalStatus() == DutyStatus.APPROVED
                    || duty.getApprovalStatus() == DutyStatus.REJECTED) continue;

            duty.setTimeOut(duty.getExpectedTimeOut());
            duty.setAttendanceStatus(AttendanceStatus.ABSENT);
            duty.setApprovalStatus(DutyStatus.APPROVED);
            duty.setRemarks("Auto-marked absent: no clock-out recorded within 1 hour after expected time out ("
                    + formatTo12Hour(duty.getExpectedTimeOut()) + ").");
            dutyRepository.save(duty);
            System.out.println("[AbsentScheduler] Duty " + duty.getDutyId()
                + " auto-marked ABSENT (no clock-out 1hr past " + duty.getExpectedTimeOut() + ").");
        }
    }

    private LocalTime resolveExpectedTimeIn(User scholar, LocalDate date) {
        Optional<ScholarSchedule> scheduleOpt = scholarScheduleRepository.findByUser(scholar);
        if (scheduleOpt.isPresent()) {
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            return scheduleOpt.get().getEntries().stream()
                .filter(e -> e.getDayOfWeek() == dayOfWeek)
                .map(DailyScheduleEntry::getPrimaryTimeIn)
                .findFirst()
                .orElse(null); // no schedule for today — not a scheduled day, skip
        }
        return scholar.getExpectedTimeIn();
    }

    private String formatTo12Hour(LocalTime time) {
        if (time == null) return "unknown time";
        int hour = time.getHour();
        int minute = time.getMinute();
        String ampm = hour >= 12 ? "PM" : "AM";
        int hour12 = hour % 12;
        if (hour12 == 0) hour12 = 12;
        return String.format("%d:%02d %s", hour12, minute, ampm);
    }
}
