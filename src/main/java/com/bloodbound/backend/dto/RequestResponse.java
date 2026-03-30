package com.bloodbound.backend.dto;

import java.time.LocalDateTime;

public class RequestResponse {
    private Long id;
    private String bloodType;
    private Integer units;
    private String urgency;
    private String location;
    private LocalDateTime createdAt;

    // The star of the show
    private String hospitalName;

    // --- GETTERS ---
    public Long getId() { return id; }
    public String getBloodType() { return bloodType; }
    public Integer getUnits() { return units; }
    public String getUrgency() { return urgency; }
    public String getLocation() { return location; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getHospitalName() { return hospitalName; }

    // --- SETTERS ---
    public void setId(Long id) { this.id = id; }
    public void setBloodType(String bloodType) { this.bloodType = bloodType; }
    public void setUnits(Integer units) { this.units = units; }
    public void setUrgency(String urgency) { this.urgency = urgency; }
    public void setLocation(String location) { this.location = location; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }
}