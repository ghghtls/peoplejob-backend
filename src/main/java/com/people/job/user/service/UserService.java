package com.people.job.user.service;

import com.people.job.user.dto.UserDTO;
import com.people.job.user.entity.UserEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface UserService {

    // 기존 메서드들
    Map<String, String> register(UserDTO dto);
    Map<String, Object> login(String userid, String password);
    void verifyEmail(String userid, String code);
    UserEntity findByUserid(String userid);

    // 새로 추가: 회원 정보 관리 메서드들

    /**
     * 회원 정보 조회
     * @param userNo 회원 번호
     * @return 회원 정보 DTO
     */
    UserDTO getUserProfile(Long userNo);

    /**
     * 회원 정보 수정
     * @param userNo 회원 번호
     * @param dto 수정할 회원 정보
     * @return 수정된 회원 정보 DTO
     */
    UserDTO updateUserProfile(Long userNo, UserDTO dto);

    /**
     * 비밀번호 변경
     * @param userNo 회원 번호
     * @param currentPassword 현재 비밀번호
     * @param newPassword 새 비밀번호
     */
    void changePassword(Long userNo, String currentPassword, String newPassword);

    /**
     * 프로필 이미지 업로드
     * @param userNo 회원 번호
     * @param file 업로드할 이미지 파일
     * @return 업로드된 이미지 URL
     */
    String uploadProfileImage(Long userNo, MultipartFile file);

    /**
     * 프로필 이미지 삭제
     * @param userNo 회원 번호
     */
    void deleteProfileImage(Long userNo);

    /**
     * 회원 탈퇴
     * @param userNo 회원 번호
     */
    void deleteUser(Long userNo);
}