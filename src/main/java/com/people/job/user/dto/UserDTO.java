package com.people.job.user.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    private Long userNo;
    private String userid;
    private String password;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String detailAddress;
    private String zipcode;
    private String userType;
    private String role;
    private Boolean isActive;
    private Boolean isEmailVerified;

    // 새로 추가: 프로필 이미지 관련 필드들
    private String profileImageUrl;
    private String profileImageFilename;

    // 기업회원 전용 필드들
    private String companyName;
    private String businessNumber;
    private String companyPhone;
    private String companyAddress;
    private String ceoName;
    private String companyType;
    private Integer employeeCount;
    private String establishedYear;
    private String website;
    private String companyDescription;

    // 타임스탬프
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 프로필 이미지 관련 헬퍼 메서드
    public boolean hasProfileImage() {
        return profileImageUrl != null && !profileImageUrl.trim().isEmpty();
    }

    // 회원 타입 확인 헬퍼 메서드
    public boolean isCompany() {
        return "COMPANY".equals(userType);
    }

    public boolean isIndividual() {
        return "INDIVIDUAL".equals(userType);
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    // 회원 정보 검증 메서드
    public boolean isValidForUpdate() {
        return name != null && !name.trim().isEmpty() &&
                email != null && !email.trim().isEmpty() &&
                email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    // 기업 정보 검증 메서드
    public boolean isValidCompanyInfo() {
        if (!isCompany()) return true;

        return companyName != null && !companyName.trim().isEmpty() &&
                businessNumber != null && !businessNumber.trim().isEmpty() &&
                ceoName != null && !ceoName.trim().isEmpty();
    }

    // 비밀번호 검증을 위한 별도 DTO
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PasswordChangeRequest {
        private String currentPassword;
        private String newPassword;
        private String confirmPassword;

        public boolean isValid() {
            return currentPassword != null && !currentPassword.trim().isEmpty() &&
                    newPassword != null && !newPassword.trim().isEmpty() &&
                    confirmPassword != null && newPassword.equals(confirmPassword);
        }
    }
}