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
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getUserProfile(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPasswordHash(null);
            return ResponseEntity.ok(new ApiResponse(true, user, "Profile retrieved successfully!"));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse(false, "USER-001", "User profile not found."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateProfile(
            @PathVariable Long id,
            @RequestBody ProfileUpdateRequest request) {

        Optional<User> userOpt = userRepository.findById(id);

        if (userOpt.isPresent()) {
            User existingUser = userOpt.get();

            if (request.getFullName() != null)       existingUser.setFullName(request.getFullName());
            if (request.getBloodType() != null)      existingUser.setBloodType(request.getBloodType());
            if (request.getHospitalOrOrg() != null)  existingUser.setHospitalOrOrg(request.getHospitalOrOrg());
            if (request.getContactNumber() != null)  existingUser.setContactNumber(request.getContactNumber());

            userRepository.save(existingUser);
            existingUser.setPasswordHash(null);

            return ResponseEntity.ok(new ApiResponse(true, existingUser, "Profile updated successfully!"));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse(false, "USER-001", "User profile not found."));
    }

    @Transactional
    @PutMapping("/{id}/password")
    public ResponseEntity<ApiResponse> updatePassword(
            @PathVariable Long id,
            @RequestBody PasswordUpdateRequest request) {

        Optional<User> userOpt = userRepository.findById(id);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse(false, "AUTH-002", "Incorrect old password."));
            }

            if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse(false, "AUTH-003",
                                "New password must be different from your current password."));
            }

            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);

            return ResponseEntity.ok(new ApiResponse(true, null, "Password updated successfully!"));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse(false, "USER-001", "User profile not found."));
    }

    @PostMapping("/{id}/photo")
    public ResponseEntity<ApiResponse> uploadPhoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        Optional<User> userOpt = userRepository.findById(id);

        if (userOpt.isPresent()) {
            try {
                User user = userOpt.get();

                String contentType = file.getContentType();
                if (contentType == null ||
                        !(contentType.equals("image/jpeg") || contentType.equals("image/png"))) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new ApiResponse(false, "FILE-002", "Only .jpg and .png files are allowed."));
                }

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

    // ------------------------------------------------
    // API: GET /api/v1/profile/{id}/eligibility
    // Check if a donor is eligible to donate (56-day rule)
    // ------------------------------------------------
    @GetMapping("/{id}/eligibility")
    public ResponseEntity<ApiResponse> checkEligibility(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "DB-001", "User not found."));
        }

        User user = userOpt.get();

        // No donation history — fully eligible
        if (user.getLastDonationDate() == null) {
            return ResponseEntity.ok(new ApiResponse(true,
                    Map.of(
                            "isEligible", true,
                            "daysUntilEligible", 0,
                            "nextEligibleDate", "Now",
                            "message", "Ready to Donate"
                    ), "Eligibility checked."));
        }

        long daysSince = ChronoUnit.DAYS.between(user.getLastDonationDate(), LocalDateTime.now());
        long daysRemaining = 56 - daysSince;
        boolean isEligible = daysRemaining <= 0;

        return ResponseEntity.ok(new ApiResponse(true,
                Map.of(
                        "isEligible", isEligible,
                        "daysUntilEligible", isEligible ? 0 : daysRemaining,
                        "nextEligibleDate", isEligible ? "Now" :
                                user.getLastDonationDate().plusDays(56).toLocalDate().toString(),
                        "message", isEligible ? "Ready to Donate" : "Eligible in " + daysRemaining + " days"
                ), "Eligibility checked."));
    }
}