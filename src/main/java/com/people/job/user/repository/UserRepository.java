package com.people.job.user.repository;

import com.people.job.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUserid(String userid);
    boolean existsByUserid(String userid);
    Optional<UserEntity> findByEmail(String email);
}
