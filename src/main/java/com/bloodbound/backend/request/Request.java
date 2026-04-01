package com.bloodbound.backend.request;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "requests")
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String bloodType;

    // ✅ FIXED: Maps the Java 'units' variable to the DB 'units_needed' column
    @Column(name = "units_needed", nullable = false)
    private Integer units;

    @Column(nullable = false)
    private String urgency;

    @Column(nullable = false)
    private String status = "ACTIVE";

    // ✅ FIXED: Maps the Java 'notes' variable to the DB 'patient_details' column
    @Column(name = "patient_details")
    private String notes;

    private String location;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime fulfilledAt;

    @Column(nullable = false)
    private Long requesterId;

    private Long hospitalId;

    // Getters
    public Long getId()                   { return id; }
    public String getBloodType()          { return bloodType; }
    public Integer getUnits()             { return units; }
    public String getUrgency()            { return urgency; }
    public String getStatus()             { return status; }
    public String getNotes()              { return notes; }
    public String getLocation()           { return location; }
    public LocalDateTime getCreatedAt()   { return createdAt; }
    public LocalDateTime getFulfilledAt() { return fulfilledAt; }
    public Long getRequesterId()          { return requesterId; }
    public Long getHospitalId()           { return hospitalId; }

    // Setters
    public void setId(Long id)                          { this.id = id; }
    public void setBloodType(String bloodType)          { this.bloodType = bloodType; }
    public void setUnits(Integer units)                 { this.units = units; }
    public void setUrgency(String urgency)              { this.urgency = urgency; }
    public void setStatus(String status)                { this.status = status; }
    public void setNotes(String notes)                  { this.notes = notes; }
    public void setLocation(String location)            { this.location = location; }
    public void setCreatedAt(LocalDateTime createdAt)   { this.createdAt = createdAt; }
    public void setFulfilledAt(LocalDateTime fulfilledAt){ this.fulfilledAt = fulfilledAt; }
    public void setRequesterId(Long requesterId)        { this.requesterId = requesterId; }
    public void setHospitalId(Long hospitalId)          { this.hospitalId = hospitalId; }
}