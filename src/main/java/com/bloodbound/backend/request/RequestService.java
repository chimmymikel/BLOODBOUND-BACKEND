package com.bloodbound.backend.request;

import com.bloodbound.backend.commitment.CommitmentRepository;
import com.bloodbound.backend.common.RequestFulfilledEvent;
import com.bloodbound.backend.hospital.HospitalRepository;
import com.bloodbound.backend.identity.UserRepository; // Added specific import
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RequestService {

    @Autowired private UserRepository        userRepository;
    @Autowired private RequestRepository     requestRepository;
    @Autowired private HospitalRepository    hospitalRepository;
    @Autowired private CommitmentRepository  commitmentRepository;
    @Autowired private ApplicationEventPublisher eventPublisher;

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

        return results.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public Optional<RequestResponse> getById(Long id) {
        return requestRepository.findById(id).map(this::mapToDto);
    }

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

    private RequestResponse mapToDto(Request req) {
        RequestResponse dto = new RequestResponse();
        dto.setId(req.getId());
        dto.setBloodType(req.getBloodType());
        dto.setUnits(req.getUnits());
        dto.setUrgency(req.getUrgency());
        dto.setStatus(req.getStatus());
        dto.setNotes(req.getNotes());
        dto.setLocation(req.getLocation());
        dto.setCreatedAt(req.getCreatedAt());

        // Count existing donor commitments for this request
        dto.setCommitmentCount(commitmentRepository.findByRequestId(req.getId()).size());

        // ✅ NEW: Map Requester Details (Contact Number and Name)
        if (req.getRequesterId() != null) {
            userRepository.findById(req.getRequesterId()).ifPresent(user -> {
                dto.setRequesterName(user.getFullName());
                dto.setRequesterContactNumber(user.getContactNumber());
            });
        }

        // Map Hospital Name
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