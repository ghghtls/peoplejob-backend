package com.people.job.user.dto;

// import lombok.*; // 일단 주석처리

public class UserDTO {
    private String userid;
    private String password;
    private String name;
    private String email;
    private String phone;
    private String zipcode;
    private String address;
    private String addressDetail;
    private String userType;

    // 수동 생성자
    public UserDTO() {}

    // 수동 getter/setter
    public String getUserid() { return userid; }
    public void setUserid(String userid) { this.userid = userid; }

    public String getPassword() {
        System.out.println("getPassword 호출: " + password);
        return password;
    }
    public void setPassword(String password) {
        System.out.println("setPassword 호출: " + password);
        this.password = password;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getZipcode() { return zipcode; }
    public void setZipcode(String zipcode) { this.zipcode = zipcode; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getAddressDetail() { return addressDetail; }
    public void setAddressDetail(String addressDetail) { this.addressDetail = addressDetail; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    @Override
    public String toString() {
        return "UserDTO{userid='" + userid + "', password='" + password + "', name='" + name + "'}";
    }
}