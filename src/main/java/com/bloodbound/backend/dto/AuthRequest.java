package com.bloodbound.backend.dto;

public class AuthRequest {
    private String email;
    private String password;
    private String fullName;
    private String bloodType;
    private String role;
    private String hospitalOrOrg;   // ✅ added for REQUESTER
    private String contactNumber;   // ✅ added for REQUESTER

    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public String getBloodType() { return bloodType; }
    public String getRole() { return role; }
    public String getHospitalOrOrg() { return hospitalOrOrg; }
    public String getContactNumber() { return contactNumber; }

    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setBloodType(String bloodType) { this.bloodType = bloodType; }
    public void setRole(String role) { this.role = role; }
    public void setHospitalOrOrg(String hospitalOrOrg) { this.hospitalOrOrg = hospitalOrOrg; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
}