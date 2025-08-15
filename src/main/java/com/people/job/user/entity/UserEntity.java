package com.people.job.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userNo;

    @Column(unique = true, nullable = false)
    private String userid;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    private String phone;
    private String address;
    private String detailAddress;
    private String zipcode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType userType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private Boolean isEmailVerified = false;

    private String emailVerificationCode;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // 새로 추가: 프로필 이미지 관련 필드들
    private String profileImageUrl;        // 프로필 이미지 URL
    private String profileImageFilename;   // 원본 파일명
    private String profileImageStoredName; // 저장된 파일명

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

    // 이메일 인증 관련 필드들
    private LocalDateTime emailVerificationExpiry;

    // 비밀번호 재설정 관련 필드들
    private String passwordResetToken;
    private LocalDateTime passwordResetExpiry;

    public enum UserType {
        INDIVIDUAL, COMPANY
    }

    public enum UserRole {
        USER, ADMIN, COMPANY
    }

    // ========== UserDetails 인터페이스 구현 ==========

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email; // 이메일을 username으로 사용
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isActive; // 활성 상태가 계정 잠금과 연관
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive && isEmailVerified; // 활성화되고 이메일 인증된 경우만 활성
    }

    // ========== 기존 메서드들 ==========

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 프로필 이미지 관련 헬퍼 메서드들
    public boolean hasProfileImage() {
        return profileImageUrl != null && !profileImageUrl.trim().isEmpty();
    }

    public void updateProfileImage(String imageUrl, String filename, String storedName) {
        this.profileImageUrl = imageUrl;
        this.profileImageFilename = filename;
        this.profileImageStoredName = storedName;
    }

    public void clearProfileImage() {
        this.profileImageUrl = null;
        this.profileImageFilename = null;
        this.profileImageStoredName = null;
    }

    // 회원 정보 업데이트 헬퍼 메서드
    public void updateBasicInfo(String name, String email, String phone, String address,
                                String detailAddress, String zipcode) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.detailAddress = detailAddress;
        this.zipcode = zipcode;
    }

    // 기업 정보 업데이트 헬퍼 메서드
    public void updateCompanyInfo(String companyName, String businessNumber, String companyPhone,
                                  String companyAddress, String ceoName, String companyType,
                                  Integer employeeCount, String establishedYear, String website,
                                  String companyDescription) {
        this.companyName = companyName;
        this.businessNumber = businessNumber;
        this.companyPhone = companyPhone;
        this.companyAddress = companyAddress;
        this.ceoName = ceoName;
        this.companyType = companyType;
        this.employeeCount = employeeCount;
        this.establishedYear = establishedYear;
        this.website = website;
        this.companyDescription = companyDescription;
    }

    // 헬퍼 메서드들
    public boolean isEmailVerificationExpired() {
        return emailVerificationExpiry != null &&
                LocalDateTime.now().isAfter(emailVerificationExpiry);
    }

    public boolean isPasswordResetExpired() {
        return passwordResetExpiry != null &&
                LocalDateTime.now().isAfter(passwordResetExpiry);
    }

    public void setEmailVerificationCode(String code, int expirationMinutes) {
        this.emailVerificationCode = code;
        this.emailVerificationExpiry = LocalDateTime.now().plusMinutes(expirationMinutes);
    }

    public void setPasswordResetToken(String token, int expirationMinutes) {
        this.passwordResetToken = token;
        this.passwordResetExpiry = LocalDateTime.now().plusMinutes(expirationMinutes);
    }
}