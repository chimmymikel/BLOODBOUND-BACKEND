package com.bloodbound.backend.hospital;

import jakarta.persistence.*;

@Entity
@Table(name = "hospitals")
public class Hospital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    private String phone;

    // --- GETTERS ---
    public Long getId()        { return id; }
    public String getName()    { return name; }
    public String getAddress() { return address; }
    public Double getLatitude()  { return latitude; }
    public Double getLongitude() { return longitude; }
    public String getPhone()   { return phone; }

    // --- SETTERS ---
    public void setId(Long id)             { this.id = id; }
    public void setName(String name)       { this.name = name; }
    public void setAddress(String address) { this.address = address; }
    public void setLatitude(Double latitude)   { this.latitude = latitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public void setPhone(String phone)     { this.phone = phone; }
}
