package com.bloodbound.backend.dto;

public class CreateRequestRequest {

    private Long requesterId;
    private String bloodType;
    private Integer units;
    private String urgency;
    private String notes;
    private String location;
    private Long hospitalId;

    // Getters
    public Long getRequesterId()    { return requesterId; }
    public String getBloodType()    { return bloodType; }
    public Integer getUnits()       { return units; }
    public String getUrgency()      { return urgency; }
    public String getNotes()        { return notes; }
    public String getLocation()     { return location; }
    public Long getHospitalId()     { return hospitalId; }

    // Setters
    public void setRequesterId(Long requesterId)    { this.requesterId = requesterId; }
    public void setBloodType(String bloodType)      { this.bloodType = bloodType; }
    public void setUnits(Integer units)             { this.units = units; }
    public void setUrgency(String urgency)          { this.urgency = urgency; }
    public void setNotes(String notes)              { this.notes = notes; }
    public void setLocation(String location)        { this.location = location; }
    public void setHospitalId(Long hospitalId)      { this.hospitalId = hospitalId; }
}