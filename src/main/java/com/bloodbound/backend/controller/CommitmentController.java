package com.bloodbound.backend.controller;

import com.bloodbound.backend.dto.ApiResponse;
import com.bloodbound.backend.dto.CreateCommitmentRequest;
import com.bloodbound.backend.model.Commitment;
import com.bloodbound.backend.model.Hospital;
import com.bloodbound.backend.model.Request;
import com.bloodbound.backend.model.User;
import com.bloodbound.backend.repository.CommitmentRepository;
import com.bloodbound.backend.repository.HospitalRepository;
import com.bloodbound.backend.repository.RequestRepository;
import com.bloodbound.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/commitments")
public class CommitmentController {

    @Autowired
    private CommitmentRepository commitmentRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    // ------------------------------------------------
    // POST /api/v1/commitments
    // Donor commits to donate for a specific request
    // ------------------------------------------------
    @PostMapping
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ResponseEntity<ApiResponse> createCommitment(@RequestBody CreateCommitmentRequest body) {

        // 1. Validate required fields
        if (body.getRequestId() == null || body.getDonorId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "VALID-001", "requestId and donorId are required."));
        }

        // 2. Check donor exists
        Optional<User> donorOpt = userRepository.findById(body.getDonorId());
        if (donorOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "DB-001", "Donor not found."));
        }
        User donor = donorOpt.get();

        // 3. Check request exists
        Optional<Request> requestOpt = requestRepository.findById(body.getRequestId());
        if (requestOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "DB-001", "Blood request not found."));
        }
        Request bloodRequest = requestOpt.get();

        // 4. Check request is still ACTIVE
        if (!bloodRequest.getStatus().equals("ACTIVE")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "REQ-001", "This request is no longer active."));
        }

        // 5. Check donor eligibility (56-day rule)
        if (donor.getLastDonationDate() != null) {
            long daysSinceLastDonation = java.time.temporal.ChronoUnit.DAYS
                    .between(donor.getLastDonationDate(), LocalDateTime.now());
            if (daysSinceLastDonation < 56) {
                long daysRemaining = 56 - daysSinceLastDonation;
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse(false, "ELIG-001",
                                "You are not eligible yet. " + daysRemaining + " days remaining."));
            }
        }

        // 6. Check blood type compatibility (direct match OR donor is O_NEGATIVE universal)
        String donorBloodType = donor.getBloodType();
        String neededBloodType = bloodRequest.getBloodType();
        boolean isCompatible = neededBloodType.equals(donorBloodType)
                || "O_NEGATIVE".equals(donorBloodType);
        if (!isCompatible) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "ELIG-002",
                            "Your blood type (" + donorBloodType + ") is not compatible with this request (" + neededBloodType + ")."));
        }

        // 7. Check for duplicate commitment
        Optional<Commitment> existing = commitmentRepository
                .findByRequestIdAndDonorId(body.getRequestId(), body.getDonorId());
        if (existing.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse(false, "COMMIT-001", "You have already committed to this request."));
        }

        // 8. Get hospital details to return to donor
        Optional<Hospital> hospitalOpt = hospitalRepository.findById(bloodRequest.getHospitalId());
        String hospitalInfo = hospitalOpt.map(h ->
                h.getName() + " — " + h.getAddress() + " — " + h.getPhone()
        ).orElse("Hospital details unavailable");

        // 9. Save commitment
        Commitment commitment = new Commitment();
        commitment.setRequestId(body.getRequestId());
        commitment.setDonorId(body.getDonorId());
        commitment.setStatus("PENDING");
        commitment.setCommittedAt(LocalDateTime.now());

        Commitment saved = commitmentRepository.save(commitment);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, saved,
                        "Commitment confirmed! Please go to: " + hospitalInfo));
    }

    // ------------------------------------------------
    // GET /api/v1/commitments
    // ?requestId=5  → Requester sees who committed
    // ?donorId=3    → Donor sees their history
    // ------------------------------------------------
    @GetMapping
    public ResponseEntity<ApiResponse> getCommitments(
            @RequestParam(required = false) Long requestId,
            @RequestParam(required = false) Long donorId) {

        List<Commitment> results;

        if (requestId != null) {
            results = commitmentRepository.findByRequestId(requestId);
        } else if (donorId != null) {
            results = commitmentRepository.findByDonorId(donorId);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "VALID-001", "Provide requestId or donorId as a query parameter."));
        }

        return ResponseEntity.ok(new ApiResponse(true, results, "Commitments fetched successfully!"));
    }
}