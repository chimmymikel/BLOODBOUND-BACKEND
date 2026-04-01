package com.bloodbound.backend.request;

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

@RestController
@RequestMapping("/api/v1/requests")
public class RequestController {

    @Autowired private RequestService requestService;

    @GetMapping
    public ResponseEntity<ApiResponse> getRequests(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String bloodType,
            @RequestParam(required = false) String urgency,
            @RequestParam(required = false) Long requesterId) {

        List<RequestResponse> data = requestService.getRequests(
                status, bloodType, urgency, requesterId);
        return ResponseEntity.ok(new ApiResponse(true, data, "Requests fetched successfully!"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getRequestById(@PathVariable Long id) {
        return requestService.getById(id)
                .map(dto -> ResponseEntity.ok(new ApiResponse(true, dto, "Request found!")))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "REQ-404", "Request not found.")));
    }

    @PostMapping
    @PreAuthorize("hasRole('REQUESTER')")
    public ResponseEntity<ApiResponse> createRequest(
            @Valid @RequestBody CreateRequestRequest body) {
        RequestResponse dto = requestService.createRequest(body);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, dto, "Request posted successfully!"));
    }

    @PatchMapping("/{id}/fulfill")
    @PreAuthorize("hasRole('REQUESTER')")
    public ResponseEntity<ApiResponse> fulfillRequest(
            @PathVariable Long id,
            Authentication authentication) {

        User loggedInUser = (User) authentication.getPrincipal();
        FulfillResult result = requestService.fulfillRequest(id, loggedInUser.getId());

        return switch (result.getStatus()) {
            case SUCCESS -> ResponseEntity.ok(
                    new ApiResponse(true, result.getDto(), "Request marked as fulfilled!"));
            case NOT_FOUND -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "REQ-404", "Request not found."));
            case FORBIDDEN -> ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(false, "AUTH-403",
                            "You can only fulfill your own requests."));
            case ALREADY_FULFILLED -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "REQ-001",
                            "Request is already fulfilled or archived."));
        };
    }
}