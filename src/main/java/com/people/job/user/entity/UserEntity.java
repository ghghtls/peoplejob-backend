package com.people.job.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.people.job.job.entity.JobopeningEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
    @Column(name = "user_no")
    private Long userNo;

    @Column(nullable = false, unique = true)
    private String userid;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    private String email;
    private String phone;
    private String zipcode;
    private String address;
    @Column(name = "address_detail")
    private String addressDetail;

    @Column(name="user_type")
    private String userType; // "U" - 개인회원, "C" - 기업회원

    private boolean emailVerified;
    private String emailVerifyCode;

    private String role; // ROLE_USER, ROLE_COMPANY, ROLE_ADMIN

    // 기업회원이 작성한 채용공고 리스트
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobopeningEntity> jobopenings = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> this.role);  // role이 "ROLE_USER" 형태일 경우
    }

    @Override
    public String getUsername() {
        return this.userid;

    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
