package com.citu.nasync_backend.shared.repository;
 
import com.citu.nasync_backend.shared.entity.DutyDay;
import com.citu.nasync_backend.shared.entity.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
 
@Repository
public interface DutyDayRepository extends JpaRepository<DutyDay, Long> {
    List<DutyDay> findBySemester(Semester semester);
    Optional<DutyDay> findByDayDateAndSemester(LocalDate dayDate, Semester semester);
    boolean existsByDayDateAndSemester(LocalDate dayDate, Semester semester);
}
