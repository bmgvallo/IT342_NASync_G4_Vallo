package com.citu.nasync_backend.shared.repository;

import com.citu.nasync_backend.shared.entity.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Repository
public interface SemesterRepository extends JpaRepository<Semester, Long> {
    Optional<Semester> findByIsActiveTrue();
    boolean existsByLabel(String label);

    @Modifying
    @Transactional
    @Query("UPDATE Semester s SET s.isActive = false WHERE s.isActive = true")
    void deactivateAll();
}
