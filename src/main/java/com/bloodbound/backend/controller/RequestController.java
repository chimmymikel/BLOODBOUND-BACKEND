package com.bloodbound.backend.controller;

import com.bloodbound.backend.dto.ApiResponse;
import com.bloodbound.backend.dto.CreateRequestRequest;
import com.bloodbound.backend.dto.RequestResponse;
import com.bloodbound.backend.model.Commitment;
import com.bloodbound.backend.model.Request;
import com.bloodbound.backend.repository.CommitmentRepository;
import com.bloodbound.backend.repository.HospitalRepository;
import com.bloodbound.backend.repository.RequestRepository;
import com.bloodbound.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/requests")
public class RequestController {

    @Autowired private RequestRepository    requestRepository;
    @Autowired private CommitmentRepository commitmentRepository;
    @Autowired private UserRepository       userRepository;

    // ✅ ADDED: We need this to look up the hospital names
    @Autowired private HospitalRepository   hospitalRepository;

    // ── GET /api/v1/requests ─────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<ApiResponse> getRequests(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String bloodType,
            @RequestParam(required = false) Long requesterId) {

        List<Request> results;

        if (requesterId != null) {
            results = requestRepository.findByRequesterId(requesterId);
        } else if (status != null && bloodType != null) {
            results = requestRepository.findByStatusAndBloodType(status, bloodType);
        } else if (status != null) {
            results = requestRepository.findByStatus(status);
        } else {
            results = requestRepository.findAll();
        }

        // ✅ CONVERT: Map the raw database entities to our DTO with the hospital name
        List<RequestResponse> responseData = results.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse(true, responseData, "Requests fetched successfully!"));
    }

    // ── GET /api/v1/requests/{id} ────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getRequestById(@PathVariable Long id) {
        Optional<Request> requestOpt = requestRepository.findById(id);

        if (requestOpt.isPresent()) {
            // ✅ CONVERT: Return the DTO instead of the raw entity
            RequestResponse dto = mapToResponseDto(requestOpt.get());
            return ResponseEntity.ok(new ApiResponse(true, dto, "Request found!"));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse(false, "REQ-404", "Request not found."));
    }

    // ── POST /api/v1/requests ────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<ApiResponse> createRequest(@RequestBody CreateRequestRequest body) {

        // Validate required fields
        if (body.getBloodType() == null || body.getUnits() == null
                || body.getUrgency() == null || body.getRequesterId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "VALID-001", "Missing required fields."));
        }

        Request newRequest = new Request();
        newRequest.setBloodType(body.getBloodType());
        newRequest.setUnits(body.getUnits());
        newRequest.setUrgency(body.getUrgency());
        newRequest.setNotes(body.getNotes());
        newRequest.setLocation(body.getLocation() != null ? body.getLocation() : "Cebu City");
        newRequest.setRequesterId(body.getRequesterId());
        newRequest.setHospitalId(body.getHospitalId());
        newRequest.setStatus("ACTIVE");
        newRequest.setCreatedAt(LocalDateTime.now());

        Request saved = requestRepository.save(newRequest);

        // ✅ CONVERT: Return the DTO so the frontend immediately gets the full data
        RequestResponse dto = mapToResponseDto(saved);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, dto, "Request posted successfully!"));
    }

    // ── PATCH /api/v1/requests/{id}/fulfill ──────────────────────────────────
    @PatchMapping("/{id}/fulfill")
    @Transactional
    public ResponseEntity<ApiResponse> fulfillRequest(@PathVariable Long id) {
        Optional<Request> requestOpt = requestRepository.findById(id);

        if (requestOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "REQ-404", "Request not found."));
        }

        Request request = requestOpt.get();

        if (!request.getStatus().equals("ACTIVE")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "REQ-001", "Request is already fulfilled or archived."));
        }

        // Mark fulfilled
        request.setStatus("FULFILLED");
        request.setFulfilledAt(LocalDateTime.now());
        requestRepository.save(request);

        // Update every committed donor's stats
        List<Commitment> commitments = commitmentRepository.findByRequestIdAndStatus(id, "PENDING");
        for (Commitment commitment : commitments) {
            commitment.setStatus("COMPLETED");
            commitment.setCompletedAt(LocalDateTime.now());
            commitmentRepository.save(commitment);

            userRepository.findById(commitment.getDonorId()).ifPresent(donor -> {
                donor.setLastDonationDate(LocalDateTime.now());
                donor.setTotalDonations(
                        (donor.getTotalDonations() == null ? 0 : donor.getTotalDonations()) + 1
                );
                userRepository.save(donor);
            });
        }

        // ✅ CONVERT: Return the updated DTO
        RequestResponse dto = mapToResponseDto(request);
        return ResponseEntity.ok(new ApiResponse(true, dto, "Request marked as fulfilled!"));
    }

    // ── HELPER METHOD ────────────────────────────────────────────────────────

    // ✅ ADDED: This handles the repetitive work of mapping data and looking up the hospital
    private RequestResponse mapToResponseDto(Request req) {
        RequestResponse dto = new RequestResponse();
        dto.setId(req.getId());
        dto.setBloodType(req.getBloodType());
        dto.setUnits(req.getUnits());
        dto.setUrgency(req.getUrgency());
        dto.setLocation(req.getLocation());
        dto.setCreatedAt(req.getCreatedAt());

        if (req.getHospitalId() != null) {
            hospitalRepository.findById(req.getHospitalId()).ifPresentOrElse(
                    hospital -> dto.setHospitalName(hospital.getName()),
                    () -> dto.setHospitalName("Unknown Facility")
            );
        } else {
            dto.setHospitalName("No Hospital Specified");
        }

        return dto;
    }
}