package com.bloodbound.backend.identity;

import com.bloodbound.backend.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private Map<String, Object> buildUserPayload(User user, String token) {
        Map<String, Object> data = new HashMap<>();
        data.put("token",            token);
        data.put("id",               user.getId());
        data.put("fullName",         user.getFullName());
        data.put("email",            user.getEmail());
        data.put("role",             user.getRole());
        data.put("contactNumber",    user.getContactNumber());
        data.put("bloodType",        user.getBloodType());
        data.put("hospitalOrOrg",    user.getHospitalOrOrg());
        data.put("totalDonations",   user.getTotalDonations());
        data.put("lastDonationDate", user.getLastDonationDate());
        data.put("createdAt",        user.getCreatedAt());
        return data;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody AuthRequest request) {
        if (request.getEmail() == null || request.getPassword() == null || request.getFullName() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "VALID-001", "All fields are required."));
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse(false, "VALID-001", "Email already exists."));
        }

        User newUser = new User();
        newUser.setEmail(request.getEmail());
        newUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        newUser.setFullName(request.getFullName());
        newUser.setRole(request.getRole());
        newUser.setContactNumber(request.getContactNumber());

        if ("DONOR".equals(request.getRole())) {
            newUser.setBloodType(request.getBloodType());
        } else if ("REQUESTER".equals(request.getRole())) {
            newUser.setHospitalOrOrg(request.getHospitalOrOrg());
        }

        User saved = userRepository.save(newUser);
        String token = jwtService.generateToken(saved.getId(), saved.getEmail(), saved.getRole());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, buildUserPayload(saved, token), "Registration successful!"));
    }

    @Transactional(readOnly = true)
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody AuthRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole());
                return ResponseEntity.ok(
                        new ApiResponse(true, buildUserPayload(user, token), "Login successful!")
                );
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse(false, "AUTH-001", "Invalid credentials."));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse> me(HttpServletRequest request) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            String token = request.getHeader("Authorization").substring(7);
            return ResponseEntity.ok(new ApiResponse(true, buildUserPayload(user, token), "OK"));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse(false, "AUTH-001", "Not authenticated."));
    }
}