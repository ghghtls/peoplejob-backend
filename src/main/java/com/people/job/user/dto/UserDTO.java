package com.people.job.user.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private String userid;
    private String pwd;
    private String name;
    private String email;
    private String phone;
    private String zipcode;
    private String address;
    private String addressDetail;
    private String userType; // U or C
}
