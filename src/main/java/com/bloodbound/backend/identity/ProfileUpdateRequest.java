package com.bloodbound.backend.identity;

public class ProfileUpdateRequest {
    private String fullName;
    private String bloodType;
    private String hospitalOrOrg;
    private String contactNumber;

    // --- Getters ---
    public String getFullName() { return fullName; }
    public String getBloodType() { return bloodType; }
    public String getHospitalOrOrg() { return hospitalOrOrg; }
    public String getContactNumber() { return contactNumber; }

    // --- Setters ---
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setBloodType(String bloodType) { this.bloodType = bloodType; }
    public void setHospitalOrOrg(String hospitalOrOrg) { this.hospitalOrOrg = hospitalOrOrg; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
}