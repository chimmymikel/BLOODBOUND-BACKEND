package com.bloodbound.backend.identity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @JsonIgnore
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "blood_type")
    private String bloodType;

    private String role;

    @Column(name = "hospital_or_org")
    private String hospitalOrOrg;

    @Column(name = "contact_number")
    private String contactNumber;

    // ✅ FIX: Removed @JsonIgnore so profilePicture is included in JSON responses.
    // Stored as bytes in DB, but exposed as a Base64 string via getProfilePicture().
    @JsonIgnore  // hide the raw byte[] from Jackson
    @Lob
    @Column(name = "profile_picture")
    private byte[] profilePictureBytes;

    @Column(name = "last_donation_date")
    private LocalDateTime lastDonationDate;

    @Column(name = "total_donations")
    private Integer totalDonations = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // ── SPRING SECURITY (UserDetails) IMPLEMENTATION ──────────────

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == null) return List.of();
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return this.passwordHash;
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return this.email;
    }

    @Override @JsonIgnore public boolean isAccountNonExpired()     { return true; }
    @Override @JsonIgnore public boolean isAccountNonLocked()      { return true; }
    @Override @JsonIgnore public boolean isCredentialsNonExpired() { return true; }
    @Override @JsonIgnore public boolean isEnabled()               { return true; }

    // ── GETTERS ───────────────────────────────────────────────────

    public Long getId()                        { return id; }
    public String getEmail()                   { return email; }
    public String getPasswordHash()            { return passwordHash; }
    public String getFullName()                { return fullName; }
    public String getBloodType()               { return bloodType; }
    public String getRole()                    { return role; }
    public String getHospitalOrOrg()           { return hospitalOrOrg; }
    public String getContactNumber()           { return contactNumber; }
    public LocalDateTime getLastDonationDate() { return lastDonationDate; }
    public Integer getTotalDonations()         { return totalDonations; }
    public LocalDateTime getCreatedAt()        { return createdAt; }

    // ✅ FIX: Returns Base64 string (with data URI prefix) for the frontend.
    // Jackson will serialize this as "profilePicture": "data:image/jpeg;base64,..."
    public String getProfilePicture() {
        if (profilePictureBytes == null) return null;
        // Detect PNG by magic bytes, otherwise assume JPEG
        String mimeType = "image/jpeg";
        if (profilePictureBytes.length > 3
                && profilePictureBytes[0] == (byte) 0x89
                && profilePictureBytes[1] == (byte) 0x50
                && profilePictureBytes[2] == (byte) 0x4E
                && profilePictureBytes[3] == (byte) 0x47) {
            mimeType = "image/png";
        }
        return "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(profilePictureBytes);
    }

    // ── SETTERS ───────────────────────────────────────────────────

    public void setId(Long id)                                      { this.id = id; }
    public void setEmail(String email)                              { this.email = email; }
    public void setPasswordHash(String passwordHash)                { this.passwordHash = passwordHash; }
    public void setFullName(String fullName)                        { this.fullName = fullName; }
    public void setBloodType(String bloodType)                      { this.bloodType = bloodType; }
    public void setRole(String role)                                { this.role = role; }
    public void setHospitalOrOrg(String hospitalOrOrg)              { this.hospitalOrOrg = hospitalOrOrg; }
    public void setContactNumber(String contactNumber)              { this.contactNumber = contactNumber; }
    public void setProfilePicture(byte[] profilePicture)            { this.profilePictureBytes = profilePicture; }
    public void setLastDonationDate(LocalDateTime lastDonationDate) { this.lastDonationDate = lastDonationDate; }
    public void setTotalDonations(Integer totalDonations)           { this.totalDonations = totalDonations; }
    public void setCreatedAt(LocalDateTime createdAt)               { this.createdAt = createdAt; }
}