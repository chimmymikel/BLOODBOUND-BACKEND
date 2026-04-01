package com.bloodbound.backend.commitment;

import jakarta.validation.constraints.NotNull;

public class CreateCommitmentRequest {

    @NotNull(message = "requestId is required.")
    private Long requestId;

    // ✅ donorId REMOVED — always taken from the authenticated JWT, never trusted from client

    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }
}