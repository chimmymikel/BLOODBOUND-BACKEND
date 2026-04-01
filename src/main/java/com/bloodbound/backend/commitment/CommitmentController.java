package com.bloodbound.backend.commitment;

import com.bloodbound.backend.common.ApiResponse;
import com.bloodbound.backend.identity.User;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/commitments")
public class CommitmentController {

    @Autowired private CommitmentService commitmentService;

    @PostMapping
    @PreAuthorize("hasRole('DONOR')")
    public ResponseEntity<ApiResponse> createCommitment(
            @Valid @RequestBody CreateCommitmentRequest body,
            Authentication authentication) {  // ✅ FIXED: inject Authentication

        User loggedInUser = (User) authentication.getPrincipal(); // ✅ FIXED: trust the JWT

        CommitmentResult result = commitmentService.createCommitment(
                body.getRequestId(), loggedInUser.getId()); // ✅ FIXED: use real ID from JWT

        if (!result.isSuccess()) {
            HttpStatus status = resolveErrorStatus(result.getErrorCode());
            return ResponseEntity.status(status)
                    .body(new ApiResponse(false, result.getErrorCode(), result.getMessage()));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, result.getCommitment(), result.getMessage()));
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getCommitments(
            @RequestParam(required = false) Long requestId,
            @RequestParam(required = false) Long donorId) {

        if (requestId == null && donorId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "VALID-001",
                            "Provide requestId or donorId as a query parameter."));
        }

        List<Map<String, Object>> tickets = requestId != null
                ? commitmentService.getCommitmentsForRequest(requestId)
                : commitmentService.getCommitmentsForDonor(donorId);

        return ResponseEntity.ok(new ApiResponse(true, tickets, "Commitments fetched successfully!"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DONOR')")
    public ResponseEntity<ApiResponse> cancelCommitment(
            @PathVariable Long id,
            Authentication authentication) {

        User loggedInUser = (User) authentication.getPrincipal();
        CancelResult result = commitmentService.cancelCommitment(id, loggedInUser.getId());

        return switch (result.getStatus()) {
            case SUCCESS -> ResponseEntity.ok(
                    new ApiResponse(true, null, "Commitment cancelled successfully."));
            case NOT_FOUND -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "DB-001", "Commitment not found."));
            case FORBIDDEN -> ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(false, "AUTH-403",
                            "You can only cancel your own commitments."));
            case NOT_CANCELLABLE -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "COMMIT-003",
                            "Only PENDING commitments can be cancelled."));
        };
    }

    private HttpStatus resolveErrorStatus(String code) {
        return switch (code) {
            case "DB-001"     -> HttpStatus.NOT_FOUND;
            case "COMMIT-001" -> HttpStatus.CONFLICT;
            default           -> HttpStatus.BAD_REQUEST;
        };
    }
}