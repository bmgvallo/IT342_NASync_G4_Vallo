package com.citu.nasync_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.citu.nasync_backend.entity.Branch;
import com.citu.nasync_backend.entity.Department;
import java.util.List;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
    List<Branch> findByDepartment(Department department);
    boolean existsByNameAndDepartment(String name, Department department);
}
