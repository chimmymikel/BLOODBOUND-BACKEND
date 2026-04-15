package com.bloodbound.backend.request;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class RequestResponse {

    private Long            id;
    private String          bloodType;
    private Integer         units;
    private String          urgency;
    private String          status;
    private String          notes;
    private String          location;
    private LocalDateTime   createdAt;
    private String          hospitalName;
    private Integer         commitmentCount;
    private String          requesterName;
    private String          requesterContactNumber;

    /**
     * Each entry is a flat map with keys:
     *   "name"          – donor's full name
     *   "contactNumber" – donor's phone number
     *   "bloodType"     – donor's blood type (raw enum string, e.g. "O_POSITIVE")
     *
     * Using Map<String,String> (not a JPA entity) guarantees zero risk of
     * infinite recursion during JSON serialisation.
     */
    private List<Map<String, String>> committedDonors;

    // ── Getters ──────────────────────────────────────────────────────────────

    public Long getId()                                        { return id; }
    public String getBloodType()                               { return bloodType; }
    public Integer getUnits()                                  { return units; }
    public String getUrgency()                                 { return urgency; }
    public String getStatus()                                  { return status; }
    public String getNotes()                                   { return notes; }
    public String getLocation()                                { return location; }
    public LocalDateTime getCreatedAt()                        { return createdAt; }
    public String getHospitalName()                            { return hospitalName; }
    public Integer getCommitmentCount()                        { return commitmentCount; }
    public String getRequesterName()                           { return requesterName; }
    public String getRequesterContactNumber()                  { return requesterContactNumber; }
    public List<Map<String, String>> getCommittedDonors()      { return committedDonors; }

    // ── Setters ──────────────────────────────────────────────────────────────

    public void setId(Long id)                                 { this.id = id; }
    public void setBloodType(String bloodType)                 { this.bloodType = bloodType; }
    public void setUnits(Integer units)                        { this.units = units; }
    public void setUrgency(String urgency)                     { this.urgency = urgency; }
    public void setStatus(String status)                       { this.status = status; }
    public void setNotes(String notes)                         { this.notes = notes; }
    public void setLocation(String location)                   { this.location = location; }
    public void setCreatedAt(LocalDateTime createdAt)          { this.createdAt = createdAt; }
    public void setHospitalName(String hospitalName)           { this.hospitalName = hospitalName; }
    public void setCommitmentCount(Integer commitmentCount)    { this.commitmentCount = commitmentCount; }
    public void setRequesterName(String requesterName)         { this.requesterName = requesterName; }
    public void setRequesterContactNumber(String num)          { this.requesterContactNumber = num; }
    public void setCommittedDonors(List<Map<String, String>> committedDonors) {
        this.committedDonors = committedDonors;
    }
}