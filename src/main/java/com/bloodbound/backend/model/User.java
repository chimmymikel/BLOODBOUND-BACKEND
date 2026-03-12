package com.bloodbound.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "blood_type")
    private String bloodType;

    private String role; // 'DONOR' or 'REQUESTER'

    // 👇 ADDED THESE TWO MISSING FIELDS 👇
    @Column(name = "hospital_or_org")
    private String hospitalOrOrg;

    @Column(name = "contact_number")
    private String contactNumber;
    // 👆 ============================== 👆

    @Column(name = "last_donation_date")
    private LocalDateTime lastDonationDate;

    @Column(name = "total_donations")
    private Integer totalDonations = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();


    // --- MANUAL GETTERS ---
    public String getHospitalOrOrg() { return hospitalOrOrg; }
    public String getContactNumber() { return contactNumber; }
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getFullName() { return fullName; }
    public String getBloodType() { return bloodType; }
    public String getRole() { return role; }
    public LocalDateTime getLastDonationDate() { return lastDonationDate; }
    public Integer getTotalDonations() { return totalDonations; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // --- MANUAL SETTERS ---
    public void setHospitalOrOrg(String hospitalOrOrg) { this.hospitalOrOrg = hospitalOrOrg; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public void setId(Long id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setBloodType(String bloodType) { this.bloodType = bloodType; }
    public void setRole(String role) { this.role = role; }
    public void setLastDonationDate(LocalDateTime lastDonationDate) { this.lastDonationDate = lastDonationDate; }
    public void setTotalDonations(Integer totalDonations) { this.totalDonations = totalDonations; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}