package com.people.job.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userNo;

    @Column(nullable = false, unique = true)
    private String userid;

    @Column(nullable = false)
    private String pwd;

    @Column(nullable = false)
    private String name;

    private String email;
    private String phone;
    private String zipcode;
    private String address;
    private String addressDetail;

    private String userType; // "U" - 개인회원, "C" - 기업회원

    private boolean emailVerified;
    private String emailVerifyCode;

    private String role; // ROLE_USER, ROLE_COMPANY, ROLE_ADMIN
}
