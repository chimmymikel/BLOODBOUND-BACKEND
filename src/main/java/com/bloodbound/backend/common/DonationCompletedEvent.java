package com.bloodbound.backend.common;

// This message carries the ID of the donor who successfully donated
public record DonationCompletedEvent(Long donorId) {}