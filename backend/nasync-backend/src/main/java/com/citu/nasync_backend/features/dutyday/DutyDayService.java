package com.citu.nasync_backend.features.dutyday;
 
import com.citu.nasync_backend.features.dutyday.CreateDutyDayRequest;
import com.citu.nasync_backend.features.dutyday.DutyDayResponse;
import com.citu.nasync_backend.shared.entity.DutyDay;
import com.citu.nasync_backend.shared.entity.Semester;
import com.citu.nasync_backend.shared.entity.User;
import com.citu.nasync_backend.shared.repository.DutyDayRepository;
import com.citu.nasync_backend.shared.repository.SemesterRepository;
import com.citu.nasync_backend.shared.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
 
@Service
public class DutyDayService {
 
    @Autowired private DutyDayRepository dutyDayRepository;
    @Autowired private SemesterRepository semesterRepository;
    @Autowired private UserRepository userRepository;
 
    public DutyDayResponse createDutyDay(CreateDutyDayRequest request, String adminSchoolId) {
        Semester semester = semesterRepository.findById(request.getSemesterId())
            .orElseThrow(() -> new RuntimeException("Semester not found"));
        if (request.getDayDate().isBefore(semester.getStartDate()) ||
                request.getDayDate().isAfter(semester.getEndDate())) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);
            throw new RuntimeException("Duty day date must fall within the semester's date range (" +
                semester.getStartDate().format(fmt) + " to " + semester.getEndDate().format(fmt) + ").");
        }
        if (dutyDayRepository.existsByDayDateAndSemester(request.getDayDate(), semester))
            throw new RuntimeException("A duty day is already marked for this date in this semester");
        User admin = userRepository.findBySchoolId(adminSchoolId)
            .orElseThrow(() -> new RuntimeException("Admin not found"));
 
        DutyDay dd = new DutyDay();
        dd.setSemester(semester);
        dd.setCreatedBy(admin);
        dd.setDayDate(request.getDayDate());
        dd.setDayType(request.getDayType());
        dd.setDescription(request.getDescription());
        return DutyDayResponse.from(dutyDayRepository.save(dd));
    }
 
    public List<DutyDayResponse> getDutyDaysBySemester(Long semesterId) {
        Semester semester = semesterRepository.findById(semesterId)
            .orElseThrow(() -> new RuntimeException("Semester not found"));
        return dutyDayRepository.findBySemester(semester)
            .stream().map(DutyDayResponse::from).toList();
    }
 
    public void deleteDutyDay(Long id) {
        if (!dutyDayRepository.existsById(id))
            throw new RuntimeException("Duty day not found");
        dutyDayRepository.deleteById(id);
    }
}
