package com.bloodbound.backend.common;

// This message carries the ID of the request that was just finished
public record RequestFulfilledEvent(Long requestId) {}