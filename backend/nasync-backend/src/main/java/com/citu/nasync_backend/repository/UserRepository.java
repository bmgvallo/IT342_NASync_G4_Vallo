package com.citu.nasync_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.citu.nasync_backend.entity.User;
import com.citu.nasync_backend.entity.Branch;
import com.citu.nasync_backend.enums.Role;
import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findBySchoolId(String schoolId);
    Optional<User> findByPersonalGmail(String personalGmail);
    Optional<User> findByOauthProviderAndOauthSubject(String oauthProvider, String oauthSubject);
    Optional<User> findByEmail(String email);

    List<User> findByBranchAndRole(Branch branch, Role role);
    List<User> findByBranchAndRoleAndIsActive(Branch branch, Role role, boolean isActive);

    boolean existsBySchoolId(String schoolId);
    boolean existsByEmail(String email);
    boolean existsByPersonalGmail(String personalGmail);

}