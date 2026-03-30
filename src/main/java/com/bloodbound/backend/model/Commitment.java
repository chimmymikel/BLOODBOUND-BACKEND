package com.bloodbound.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "commitments",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"request_id", "donor_id"}
        )
)
public class Commitment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false)
    private Long requestId;

    @Column(name = "donor_id", nullable = false)
    private Long donorId;

    @Column(nullable = false)
    private String status; // PENDING, CONFIRMED, CANCELLED, COMPLETED

    @Column(name = "committed_at", nullable = false)
    private LocalDateTime committedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // --- GETTERS ---
    public Long getId()                      { return id; }
    public Long getRequestId()               { return requestId; }
    public Long getDonorId()                 { return donorId; }
    public String getStatus()               { return status; }
    public LocalDateTime getCommittedAt()    { return committedAt; }
    public LocalDateTime getCompletedAt()    { return completedAt; }

    // --- SETTERS ---
    public void setId(Long id)                           { this.id = id; }
    public void setRequestId(Long requestId)             { this.requestId = requestId; }
    public void setDonorId(Long donorId)                 { this.donorId = donorId; }
    public void setStatus(String status)                 { this.status = status; }
    public void setCommittedAt(LocalDateTime committedAt){ this.committedAt = committedAt; }
    public void setCompletedAt(LocalDateTime completedAt){ this.completedAt = completedAt; }
}