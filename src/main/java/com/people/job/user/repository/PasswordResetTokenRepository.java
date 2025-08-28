package com.people.job.user.repository;

import com.people.job.user.entity.PasswordResetTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, Long> {

    /** 토큰으로 찾기 */
    Optional<PasswordResetTokenEntity> findByToken(String token);

    /** 이메일로 찾기 (최신순) */
    List<PasswordResetTokenEntity> findByEmailOrderByCreatedAtDesc(String email);

    /** 사용자 ID로 찾기 (최신순) */
    List<PasswordResetTokenEntity> findByUserIdOrderByCreatedAtDesc(String userId);

    /** 만료된 토큰들 삭제 */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM PasswordResetTokenEntity p WHERE p.expiryDate < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    /** 특정 사용자의 사용되지 않은 토큰들을 사용됨으로 표시 */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE PasswordResetTokenEntity p SET p.used = true WHERE p.email = :email AND p.used = false")
    void markAllTokensAsUsedByEmail(@Param("email") String email);

    /** 이메일+토큰으로 유효한(미사용/미만료) 토큰 찾기 */
    @Query("""
           SELECT p FROM PasswordResetTokenEntity p
           WHERE p.email = :email AND p.token = :token
             AND p.used = false AND p.expiryDate > :now
           """)
    Optional<PasswordResetTokenEntity> findValidTokenByEmailAndToken(
            @Param("email") String email,
            @Param("token") String token,
            @Param("now") LocalDateTime now
    );

    // (선택) 토큰만으로 유효성 체크하고 싶을 때 쓰는 편의 메서드
    Optional<PasswordResetTokenEntity> findByTokenAndUsedFalseAndExpiryDateAfter(String token, LocalDateTime now);
}
