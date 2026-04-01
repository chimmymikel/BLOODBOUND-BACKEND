package com.bloodbound.backend.request;

import java.time.LocalDateTime;

public class RequestResponse {
    private Long id;
    private String bloodType;
    private Integer units;
    private String urgency;
    private String status;         // ← MISSING — frontend shows ACTIVE/FULFILLED badge
    private String notes;          // ← MISSING — frontend displays notes
    private String location;
    private LocalDateTime createdAt;
    private String hospitalName;
    private Integer commitmentCount; // ← MISSING — frontend shows donor count badge

    // Getters
    public Long getId()                   { return id; }
    public String getBloodType()          { return bloodType; }
    public Integer getUnits()             { return units; }
    public String getUrgency()            { return urgency; }
    public String getStatus()             { return status; }
    public String getNotes()              { return notes; }
    public String getLocation()           { return location; }
    public LocalDateTime getCreatedAt()   { return createdAt; }
    public String getHospitalName()       { return hospitalName; }
    public Integer getCommitmentCount()   { return commitmentCount; }

    // Setters
    public void setId(Long id)                        { this.id = id; }
    public void setBloodType(String bloodType)        { this.bloodType = bloodType; }
    public void setUnits(Integer units)               { this.units = units; }
    public void setUrgency(String urgency)            { this.urgency = urgency; }
    public void setStatus(String status)              { this.status = status; }
    public void setNotes(String notes)                { this.notes = notes; }
    public void setLocation(String location)          { this.location = location; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setHospitalName(String hospitalName)  { this.hospitalName = hospitalName; }
    public void setCommitmentCount(Integer count)     { this.commitmentCount = count; }
}