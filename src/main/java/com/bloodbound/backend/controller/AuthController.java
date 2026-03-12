package com.bloodbound.backend.controller;

import com.bloodbound.backend.dto.ApiResponse;
import com.bloodbound.backend.dto.AuthRequest;
import com.bloodbound.backend.model.User;
import com.bloodbound.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody AuthRequest request) {
        // Null/empty checks using SDD VALID-001 code
        if (request.getEmail() == null || request.getPassword() == null || request.getFullName() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "VALID-001", "All fields are required."));
        }

        // Check if email already taken using SDD 409 Conflict and VALID-001
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse(false, "VALID-001", "Email already exists."));
        }

        // Build user
        User newUser = new User();
        newUser.setEmail(request.getEmail());
        newUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        newUser.setFullName(request.getFullName());
        newUser.setRole(request.getRole());

        // Role-specific fields
        if ("DONOR".equals(request.getRole())) {
            newUser.setBloodType(request.getBloodType());
        } else if ("REQUESTER".equals(request.getRole())) {
            newUser.setHospitalOrOrg(request.getHospitalOrOrg());
            newUser.setContactNumber(request.getContactNumber());
        }

        userRepository.save(newUser);

        // 👈 FIXED: Now using the 3-argument constructor (boolean, data, message)
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, newUser, "Registration successful!"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody AuthRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                // 👈 FIXED: Now using the 3-argument constructor (boolean, data, message)
                return ResponseEntity.ok(new ApiResponse(true, user, "Login successful!"));
            }
        }

        // Error response using SDD AUTH-001 code
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse(false, "AUTH-001", "Invalid credentials."));
    }
}