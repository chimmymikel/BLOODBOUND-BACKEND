package com.bloodbound.backend.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateRequestRequest {

    @NotNull(message = "requesterId is required.")
    private Long requesterId;

    @NotBlank(message = "bloodType is required.")
    private String bloodType;

    @NotNull(message = "units is required.")
    @Min(value = 1, message = "units must be at least 1.")
    private Integer units;

    @NotBlank(message = "urgency is required.")
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