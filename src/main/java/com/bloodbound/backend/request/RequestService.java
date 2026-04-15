package com.bloodbound.backend.request;

import com.bloodbound.backend.commitment.Commitment;
import com.bloodbound.backend.commitment.CommitmentRepository;
import com.bloodbound.backend.common.RequestFulfilledEvent;
import com.bloodbound.backend.hospital.HospitalRepository;
import com.bloodbound.backend.identity.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RequestService {

    @Autowired private UserRepository           userRepository;
    @Autowired private RequestRepository        requestRepository;
    @Autowired private HospitalRepository       hospitalRepository;
    @Autowired private CommitmentRepository     commitmentRepository;
    @Autowired private ApplicationEventPublisher eventPublisher;

    // ── Query methods ─────────────────────────────────────────────────────────

    public List<RequestResponse> getRequests(String status, String bloodType,
                                             String urgency, Long requesterId) {
        List<Request> results;

        if (requesterId != null) {
            results = requestRepository.findByRequesterId(requesterId);
        } else if (status != null && bloodType != null && urgency != null) {
            results = requestRepository.findByStatusAndBloodTypeAndUrgency(status, bloodType, urgency);
        } else if (status != null && bloodType != null) {
            results = requestRepository.findByStatusAndBloodType(status, bloodType);
        } else if (status != null && urgency != null) {
            results = requestRepository.findByStatusAndUrgency(status, urgency);
        } else if (status != null) {
            results = requestRepository.findByStatus(status);
        } else {
            results = requestRepository.findAll();
        }

        return results.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public Optional<RequestResponse> getById(Long id) {
        return requestRepository.findById(id).map(this::mapToDto);
    }

    // ── Mutations ─────────────────────────────────────────────────────────────

    public RequestResponse createRequest(CreateRequestRequest body) {
        Request r = new Request();
        r.setBloodType(body.getBloodType());
        r.setUnits(body.getUnits());
        r.setUrgency(body.getUrgency());
        r.setNotes(body.getNotes());
        r.setLocation(body.getLocation() != null ? body.getLocation() : "Cebu City");
        r.setRequesterId(body.getRequesterId());
        r.setHospitalId(body.getHospitalId());
        r.setStatus("ACTIVE");
        r.setCreatedAt(LocalDateTime.now());
        return mapToDto(requestRepository.save(r));
    }

    @Transactional
    public FulfillResult fulfillRequest(Long requestId, Long loggedInUserId) {
        Optional<Request> requestOpt = requestRepository.findById(requestId);

        if (requestOpt.isEmpty()) {
            return FulfillResult.notFound();
        }

        Request request = requestOpt.get();

        if (!request.getRequesterId().equals(loggedInUserId)) {
            return FulfillResult.forbidden();
        }

        if (!request.getStatus().equals("ACTIVE")) {
            return FulfillResult.alreadyFulfilled();
        }

        request.setStatus("FULFILLED");
        request.setFulfilledAt(LocalDateTime.now());
        requestRepository.save(request);
        eventPublisher.publishEvent(new RequestFulfilledEvent(requestId));

        return FulfillResult.success(mapToDto(request));
    }

    // ── DTO mapping ───────────────────────────────────────────────────────────

    private RequestResponse mapToDto(Request req) {
        RequestResponse dto = new RequestResponse();

        // ── Scalar fields ────────────────────────────────────────────────────
        dto.setId(req.getId());
        dto.setBloodType(req.getBloodType());
        dto.setUnits(req.getUnits());
        dto.setUrgency(req.getUrgency());
        dto.setStatus(req.getStatus());
        dto.setNotes(req.getNotes());
        dto.setLocation(req.getLocation());
        dto.setCreatedAt(req.getCreatedAt());

        // ── Commitments for this specific request ────────────────────────────
        List<Commitment> commitments = commitmentRepository.findByRequestId(req.getId());
        dto.setCommitmentCount(commitments.size());

        // ── Donor contact cards (only PENDING or COMPLETED commitments) ──────
        // Using a plain List<Map<String,String>> avoids any JPA lazy-loading
        // or circular reference issues during Jackson serialisation.
        List<Map<String, String>> donorCards = new ArrayList<>();

        for (Commitment c : commitments) {
            if ("PENDING".equals(c.getStatus()) || "COMPLETED".equals(c.getStatus())) {
                userRepository.findById(c.getDonorId()).ifPresent(donor -> {
                    Map<String, String> card = new HashMap<>();
                    card.put("name",          donor.getFullName()      != null ? donor.getFullName()      : "—");
                    card.put("contactNumber", donor.getContactNumber() != null ? donor.getContactNumber() : "—");
                    card.put("bloodType",     donor.getBloodType()     != null ? donor.getBloodType()     : "—");
                    donorCards.add(card);
                });
            }
        }

        dto.setCommittedDonors(donorCards);

        // ── Requester contact details (shown to committed donors) ────────────
        if (req.getRequesterId() != null) {
            userRepository.findById(req.getRequesterId()).ifPresent(user -> {
                dto.setRequesterName(user.getFullName());
                dto.setRequesterContactNumber(user.getContactNumber());
            });
        }

        // ── Hospital name ────────────────────────────────────────────────────
        if (req.getHospitalId() != null) {
            hospitalRepository.findById(req.getHospitalId())
                    .ifPresentOrElse(
                            h -> dto.setHospitalName(h.getName()),
                            () -> dto.setHospitalName("Unknown Facility")
                    );
        } else {
            dto.setHospitalName("No Hospital Specified");
        }

        return dto;
    }
}