package com.citu.nasync_backend.service;
 
import com.citu.nasync_backend.dto.request.CreateDutyDayRequest;
import com.citu.nasync_backend.dto.response.DutyDayResponse;
import com.citu.nasync_backend.entity.DutyDay;
import com.citu.nasync_backend.entity.Semester;
import com.citu.nasync_backend.entity.User;
import com.citu.nasync_backend.repository.DutyDayRepository;
import com.citu.nasync_backend.repository.SemesterRepository;
import com.citu.nasync_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
 
@Service
public class DutyDayService {
 
    @Autowired private DutyDayRepository dutyDayRepository;
    @Autowired private SemesterRepository semesterRepository;
    @Autowired private UserRepository userRepository;
 
    public DutyDayResponse createDutyDay(CreateDutyDayRequest request, String adminSchoolId) {
        Semester semester = semesterRepository.findById(request.getSemesterId())
            .orElseThrow(() -> new RuntimeException("Semester not found"));
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
