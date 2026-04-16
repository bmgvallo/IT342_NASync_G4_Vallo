package com.citu.nasync_backend.service;
 
import com.citu.nasync_backend.dto.request.CreateSemesterRequest;
import com.citu.nasync_backend.dto.response.SemesterResponse;
import com.citu.nasync_backend.entity.Semester;
import com.citu.nasync_backend.repository.SemesterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
 
@Service
public class SemesterService {
 
    @Autowired
    private SemesterRepository semesterRepository;
 
    public SemesterResponse createSemester(CreateSemesterRequest request) {
        if (request.getLabel() == null || request.getLabel().isBlank())
            throw new RuntimeException("Semester label is required");
        if (request.getStartDate() == null || request.getEndDate() == null)
            throw new RuntimeException("Start and end date are required");
        if (request.getEndDate().isBefore(request.getStartDate()))
            throw new RuntimeException("End date must be after start date");
        if (semesterRepository.existsByLabel(request.getLabel()))
            throw new RuntimeException("A semester with this label already exists");
 
        Semester semester = new Semester();
        semester.setLabel(request.getLabel());
        semester.setStartDate(request.getStartDate());
        semester.setEndDate(request.getEndDate());
        semester.setActive(false);
        return SemesterResponse.from(semesterRepository.save(semester));
    }
 
    public List<SemesterResponse> getAllSemesters() {
        return semesterRepository.findAll()
            .stream().map(SemesterResponse::from).toList();
    }
 
    public SemesterResponse activateSemester(Long id) {
        // deactivate all first
        semesterRepository.findAll().forEach(s -> {
            s.setActive(false);
            semesterRepository.save(s);
        });
        Semester semester = semesterRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Semester not found"));
        semester.setActive(true);
        return SemesterResponse.from(semesterRepository.save(semester));
    }
 
    public Optional<SemesterResponse> getActiveSemester() {
        return semesterRepository.findByIsActiveTrue()
            .map(SemesterResponse::from);
    }

    public SemesterResponse deactivateSemester(Long id) {
        Semester semester = semesterRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Semester not found"));
        
        if (!semester.isActive()) {
            throw new RuntimeException("Semester is already inactive");
        }
        
        semester.setActive(false);
        return SemesterResponse.from(semesterRepository.save(semester));
    }

}
