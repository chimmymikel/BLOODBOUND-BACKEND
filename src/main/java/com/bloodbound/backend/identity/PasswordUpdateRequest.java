package com.bloodbound.backend.identity;

public class PasswordUpdateRequest {
    private String oldPassword;
    private String newPassword;

    // --- Getters ---
    public String getOldPassword() { return oldPassword; }
    public String getNewPassword() { return newPassword; }

    // --- Setters ---
    public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}