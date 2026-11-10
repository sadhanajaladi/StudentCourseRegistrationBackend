
package com.studentregistration.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.studentregistration.entity.Role;
import com.studentregistration.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
    
    List<User> findByRoleOrderByNameAsc(Role role);
}
