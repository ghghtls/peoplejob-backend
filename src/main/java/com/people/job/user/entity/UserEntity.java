package com.people.job.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDate;
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

    @Column(length = 50, unique = true, nullable = false)
    private String userid;

    @Column(length = 255, nullable = false)
    private String password;

    @Column(length = 50, nullable = false)
    private String username; // DB 스키마의 username 필드와 맞춤

    @Column(length = 100, nullable = false)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 200)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType userType;

    @Column(nullable = false)
    private LocalDate regdate; // DB 스키마의 DATE 타입과 맞춤

    @Column(nullable = false)
    private Boolean isActive = true;

    // 추가 필드들 (실제 서비스에서 필요한 것들)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserRole role = UserRole.USER;

    @Builder.Default
    private Boolean isEmailVerified = false;

    private String emailVerificationCode;
    private LocalDateTime emailVerificationExpiry;
    private String passwordResetToken;
    private LocalDateTime passwordResetExpiry;

    // 프로필 이미지 관련
    private String profileImageUrl;
    private String profileImageFilename;
    private String profileImageStoredName;

    // 주소 상세 정보
    private String detailAddress;
    private String zipcode;

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

    // Timestamp 필드들
    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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
        return email; // 로그인에는 email 사용
    }

    // 실제 사용자 이름은 getUserRealName() 메서드로 제공
    public String getUserRealName() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isActive;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive && isEmailVerified;
    }

    // ========== JPA 콜백 메서드 ==========

    @PrePersist
    protected void onCreate() {
        this.regdate = LocalDate.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.isActive == null) {
            this.isActive = true;
        }
        if (this.isEmailVerified == null) {
            this.isEmailVerified = false;
        }
        if (this.role == null) {
            this.role = UserRole.USER;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ========== 헬퍼 메서드들 ==========

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

    public void updateBasicInfo(String username, String email, String phone, String address,
                                String detailAddress, String zipcode) {
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.detailAddress = detailAddress;
        this.zipcode = zipcode;
    }

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