package com.bloodbound.backend.dto;

public class CreateCommitmentRequest {

    private Long requestId;  // which blood request to commit to
    private Long donorId;    // who is committing (the logged-in donor)

    // --- GETTERS ---
    public Long getRequestId() { return requestId; }
    public Long getDonorId()   { return donorId; }

    // --- SETTERS ---
    public void setRequestId(Long requestId) { this.requestId = requestId; }
    public void setDonorId(Long donorId)     { this.donorId = donorId; }
}
