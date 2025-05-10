package com.people.job.user.repository;

import com.people.job.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserid(String userid);
    boolean existsByUserid(String userid);
    Optional<User> findByEmail(String email);
}
