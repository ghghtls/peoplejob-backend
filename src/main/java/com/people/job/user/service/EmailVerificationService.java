package com.people.job.user.service;

/**
 * 이메일 인증 및 비밀번호 재설정 서비스 인터페이스
 */
public interface EmailVerificationService {

    /**
     * 이메일 인증코드 생성 및 발송
     * @param email 사용자 이메일
     * @return 발송 성공 여부
     */
    boolean sendEmailVerification(String email);

    /**
     * 이메일 인증 확인
     * @param code 인증 코드
     * @return 인증 성공 여부
     */
    boolean verifyEmail(String code);

    /**
     * 비밀번호 재설정 토큰 생성 및 발송
     * @param email 사용자 이메일
     * @return 발송 성공 여부
     */
    boolean sendPasswordResetEmail(String email);

    /**
     * 비밀번호 재설정
     * @param token 재설정 토큰
     * @param newPassword 새 비밀번호
     * @return 재설정 성공 여부
     */
    boolean resetPassword(String token, String newPassword);
}