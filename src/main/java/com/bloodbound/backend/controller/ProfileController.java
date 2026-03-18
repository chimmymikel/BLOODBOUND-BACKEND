package com.bloodbound.backend.controller;

import com.bloodbound.backend.dto.ApiResponse;
import com.bloodbound.backend.dto.ProfileUpdateRequest;
import com.bloodbound.backend.dto.PasswordUpdateRequest;
import com.bloodbound.backend.model.User;
import com.bloodbound.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ----------------------------------------------------
    // API #3: GET PROFILE (Retrieve User Data)
    // ----------------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getUserProfile(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPasswordHash(null); // Securely hide the password hash
            return ResponseEntity.ok(new ApiResponse(true, user, "Profile retrieved successfully!"));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse(false, "USER-001", "User profile not found."));
    }

    // ----------------------------------------------------
    // API #4: EDIT PROFILE (Update Profile Details)
    // ----------------------------------------------------
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateProfile(
            @PathVariable Long id,
            @RequestBody ProfileUpdateRequest request) {

        Optional<User> userOpt = userRepository.findById(id);

        if (userOpt.isPresent()) {
            User existingUser = userOpt.get();

            if (request.getFullName() != null) existingUser.setFullName(request.getFullName());
            if (request.getBloodType() != null) existingUser.setBloodType(request.getBloodType());
            if (request.getHospitalOrOrg() != null) existingUser.setHospitalOrOrg(request.getHospitalOrOrg());
            if (request.getContactNumber() != null) existingUser.setContactNumber(request.getContactNumber());

            userRepository.save(existingUser);
            existingUser.setPasswordHash(null);

            return ResponseEntity.ok(new ApiResponse(true, existingUser, "Profile updated successfully!"));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse(false, "USER-001", "User profile not found."));
    }

    // ----------------------------------------------------
    // API #5: EDIT PASSWORD (Change User Password)
    // FIX: @Transactional ensures the new password hash is fully
    // committed to the DB before the connection returns to the pool.
    // Without this, Supabase pgBouncer could hand a stale connection
    // to the next login request, causing BCrypt mismatch (AUTH-001).
    // ----------------------------------------------------
    @Transactional
    @PutMapping("/{id}/password")
    public ResponseEntity<ApiResponse> updatePassword(
            @PathVariable Long id,
            @RequestBody PasswordUpdateRequest request) {

        Optional<User> userOpt = userRepository.findById(id);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Check old password
            if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse(false, "AUTH-002", "Incorrect old password."));
            }

            // Encrypt and save new password
            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);

            return ResponseEntity.ok(new ApiResponse(true, null, "Password updated successfully!"));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse(false, "USER-001", "User profile not found."));
    }

    // ----------------------------------------------------
    // API #6: UPLOAD PHOTO (Convert to BLOB and Save)
    // ----------------------------------------------------
    @PostMapping("/{id}/photo")
    public ResponseEntity<ApiResponse> uploadPhoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        Optional<User> userOpt = userRepository.findById(id);

        if (userOpt.isPresent()) {
            try {
                User user = userOpt.get();

                // VALIDATION: Check file types (Assignment requirement)
                String contentType = file.getContentType();
                if (contentType == null || !(contentType.equals("image/jpeg") || contentType.equals("image/png"))) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new ApiResponse(false, "FILE-002", "Only .jpg and .png files are allowed."));
                }

                // CONVERT: File to byte array (BLOB)
                user.setProfilePicture(file.getBytes());
                userRepository.save(user);

                return ResponseEntity.ok(new ApiResponse(true, null, "Photo uploaded successfully!"));
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ApiResponse(false, "FILE-001", "Error processing image file."));
            }
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse(false, "USER-001", "User profile not found."));
    }
}