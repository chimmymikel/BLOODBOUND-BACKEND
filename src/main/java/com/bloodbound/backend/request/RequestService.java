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
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RequestService {

    @Autowired private UserRepository            userRepository;
    @Autowired private RequestRepository         requestRepository;
    @Autowired private HospitalRepository        hospitalRepository;
    @Autowired private CommitmentRepository      commitmentRepository;
    @Autowired private ApplicationEventPublisher eventPublisher;

    // ── Blood type compatibility map ──────────────────────────────────────────
    // Key   = donor's blood type  (MUST match DB/underscore format e.g. "O_POSITIVE")
    // Value = request blood types this donor CAN donate to
    //
    // ✅ FIX: Keys and values now use UNDERSCORE format matching the database.
    //         The old map used short-form keys ("O+", "A-", etc.) so every
    //         lookup for "O_POSITIVE" fell through to the default — returning
    //         only exact blood-type matches instead of compatible ones.
    //         That is why an O+ donor only ever saw O+ requests.
    private static final Map<String, List<String>> COMPATIBILITY = new HashMap<>();
    static {
        // O- (universal donor) can donate to everyone
        COMPATIBILITY.put("O_NEGATIVE", List.of(
                "O_NEGATIVE", "O_POSITIVE",
                "A_NEGATIVE", "A_POSITIVE",
                "B_NEGATIVE", "B_POSITIVE",
                "AB_NEGATIVE", "AB_POSITIVE"
        ));

        // O+ can donate to all positive types
        COMPATIBILITY.put("O_POSITIVE", List.of(
                "O_POSITIVE",
                "A_POSITIVE",
                "B_POSITIVE",
                "AB_POSITIVE"
        ));

        // A- can donate to A and AB (both +/-)
        COMPATIBILITY.put("A_NEGATIVE", List.of(
                "A_NEGATIVE", "A_POSITIVE",
                "AB_NEGATIVE", "AB_POSITIVE"
        ));

        // A+ can donate to A+ and AB+
        COMPATIBILITY.put("A_POSITIVE", List.of(
                "A_POSITIVE",
                "AB_POSITIVE"
        ));

        // B- can donate to B and AB (both +/-)
        COMPATIBILITY.put("B_NEGATIVE", List.of(
                "B_NEGATIVE", "B_POSITIVE",
                "AB_NEGATIVE", "AB_POSITIVE"
        ));

        // B+ can donate to B+ and AB+
        COMPATIBILITY.put("B_POSITIVE", List.of(
                "B_POSITIVE",
                "AB_POSITIVE"
        ));

        // AB- can donate to AB- and AB+
        COMPATIBILITY.put("AB_NEGATIVE", List.of(
                "AB_NEGATIVE", "AB_POSITIVE"
        ));

        // AB+ can only donate to AB+
        COMPATIBILITY.put("AB_POSITIVE", List.of(
                "AB_POSITIVE"
        ));
    }

    // ── Query methods ─────────────────────────────────────────────────────────

    public List<RequestResponse> getRequests(String status, String bloodType,
                                             String urgency, Long requesterId) {
        List<Request> results;

        if (requesterId != null) {
            // Requester fetching their own requests — no compatibility filtering needed
            results = requestRepository.findByRequesterId(requesterId);

        } else if (status != null && bloodType != null && urgency != null) {
            // Donor with bloodType filter + urgency filter
            List<String> compatible = COMPATIBILITY.getOrDefault(bloodType, List.of(bloodType));
            results = requestRepository.findByStatusAndBloodTypeIn(status, compatible)
                    .stream()
                    .filter(r -> r.getUrgency().equalsIgnoreCase(urgency))
                    .collect(Collectors.toList());

        } else if (status != null && bloodType != null) {
            // Donor with bloodType filter only — expand to all compatible request types
            List<String> compatible = COMPATIBILITY.getOrDefault(bloodType, List.of(bloodType));
            results = requestRepository.findByStatusAndBloodTypeIn(status, compatible);

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

        if (requestOpt.isEmpty())                                      return FulfillResult.notFound();
        if (!requestOpt.get().getRequesterId().equals(loggedInUserId)) return FulfillResult.forbidden();
        if (!requestOpt.get().getStatus().equals("ACTIVE"))            return FulfillResult.alreadyFulfilled();

        Request request = requestOpt.get();
        request.setStatus("FULFILLED");
        request.setFulfilledAt(LocalDateTime.now());
        requestRepository.save(request);
        eventPublisher.publishEvent(new RequestFulfilledEvent(requestId));

        return FulfillResult.success(mapToDto(request));
    }

    // ── DTO mapping ───────────────────────────────────────────────────────────

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

        List<Commitment> commitments = commitmentRepository.findByRequestId(req.getId());
        dto.setCommitmentCount(commitments.size());

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

        if (req.getRequesterId() != null) {
            userRepository.findById(req.getRequesterId()).ifPresent(user -> {
                dto.setRequesterName(user.getFullName());
                dto.setRequesterContactNumber(user.getContactNumber());
            });
        }

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