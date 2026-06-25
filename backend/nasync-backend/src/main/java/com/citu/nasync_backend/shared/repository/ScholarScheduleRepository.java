package com.citu.nasync_backend.shared.repository;

import com.citu.nasync_backend.shared.entity.ScholarSchedule;
import com.citu.nasync_backend.shared.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ScholarScheduleRepository extends JpaRepository<ScholarSchedule, Long> {
    Optional<ScholarSchedule> findByUser(User user);
}
