package com.bloodbound.backend.dto;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ApiResponse {
    private boolean success;
    private Object data;
    private ApiError error;
    private String message; // 👈 Restored the root-level message required by SDD
    private String timestamp;

    // ─── Nested class to exactly match the SDD "error" object structure ───
    public static class ApiError {
        private String code;
        private String message;
        private Object details;

        public ApiError(String code, String message, Object details) {
            this.code = code;
            this.message = message;
            this.details = details;
        }

        public String getCode() { return code; }
        public String getMessage() { return message; }
        public Object getDetails() { return details; }
    }

    // ─── Constructor 1: For SUCCESS responses ──────────────────────────────
    // Example: new ApiResponse(true, user, "Login successful!")
    public ApiResponse(boolean success, Object data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.error = null;
        this.timestamp = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
    }

    // ─── Constructor 2: For ERROR responses ────────────────────────────────
    // Example: new ApiResponse(false, "AUTH-001", "Invalid credentials.")
    public ApiResponse(boolean success, String errorCode, String errorMessage) {
        this.success = success;
        this.data = null;
        this.message = errorMessage; // Populates the root message
        this.error = new ApiError(errorCode, errorMessage, null); // Populates nested error
        this.timestamp = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
    }

    // ─── Manual Getters ────────────────────────────────────────────────────
    public boolean isSuccess() { return success; }
    public Object getData() { return data; }
    public ApiError getError() { return error; }
    public String getMessage() { return message; }
    public String getTimestamp() { return timestamp; }
}