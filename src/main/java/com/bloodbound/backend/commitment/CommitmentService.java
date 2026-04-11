package com.bloodbound.backend.commitment;

import com.bloodbound.backend.hospital.HospitalRepository;
import com.bloodbound.backend.identity.UserRepository;
import com.bloodbound.backend.request.RequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CommitmentService {

    @Autowired private CommitmentRepository commitmentRepository;
    @Autowired private RequestRepository    requestRepository;
    @Autowired private UserRepository       userRepository;
    @Autowired private HospitalRepository   hospitalRepository;

    // ✅ Full WHO-standard blood compatibility chart
    private static final Map<String, List<String>> COMPATIBLE_DONORS = Map.of(
            "O_NEGATIVE",  List.of("O_NEGATIVE"),
            "O_POSITIVE",  List.of("O_NEGATIVE", "O_POSITIVE"),
            "A_NEGATIVE",  List.of("O_NEGATIVE", "A_NEGATIVE"),
            "A_POSITIVE",  List.of("O_NEGATIVE", "O_POSITIVE", "A_NEGATIVE", "A_POSITIVE"),
            "B_NEGATIVE",  List.of("O_NEGATIVE", "B_NEGATIVE"),
            "B_POSITIVE",  List.of("O_NEGATIVE", "O_POSITIVE", "B_NEGATIVE", "B_POSITIVE"),
            "AB_NEGATIVE", List.of("O_NEGATIVE", "A_NEGATIVE", "B_NEGATIVE", "AB_NEGATIVE"),
            "AB_POSITIVE", List.of("O_NEGATIVE", "O_POSITIVE", "A_NEGATIVE", "A_POSITIVE",
                    "B_NEGATIVE", "B_POSITIVE", "AB_NEGATIVE", "AB_POSITIVE")
    );

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public CommitmentResult createCommitment(Long requestId, Long donorId) {

        var donorOpt = userRepository.findById(donorId);
        if (donorOpt.isEmpty()) {
            return CommitmentResult.error("DB-001", "Donor not found.");
        }
        var donor = donorOpt.get();

        var requestOpt = requestRepository.findById(requestId);
        if (requestOpt.isEmpty()) {
            return CommitmentResult.error("DB-001", "Blood request not found.");
        }
        var bloodRequest = requestOpt.get();

        if (!bloodRequest.getStatus().equals("ACTIVE")) {
            return CommitmentResult.error("REQ-001", "This request is no longer active.");
        }

        // ✅ Eligibility: 56-day cooldown check
        if (donor.getLastDonationDate() != null) {
            long daysSince = java.time.temporal.ChronoUnit.DAYS
                    .between(donor.getLastDonationDate(), LocalDateTime.now());
            if (daysSince < 56) {
                long remaining = 56 - daysSince;
                return CommitmentResult.error("ELIG-001",
                        "You are not eligible yet. " + remaining + " days remaining.");
            }
        }

        // ✅ Compatibility check
        String donorBT  = donor.getBloodType();
        String neededBT = bloodRequest.getBloodType();
        List<String> compatibleDonors = COMPATIBLE_DONORS.getOrDefault(neededBT, List.of());
        if (!compatibleDonors.contains(donorBT)) {
            return CommitmentResult.error("ELIG-002",
                    "Your blood type (" + donorBT + ") is not compatible with this request (" + neededBT + ").");
        }

        // Prevent duplicate commitment to same request
        Optional<Commitment> existing = commitmentRepository
                .findByRequestIdAndDonorId(requestId, donorId);
        if (existing.isPresent()) {
            return CommitmentResult.error("COMMIT-001", "You have already committed to this request.");
        }

        // Prevent multiple active commitments
        List<Commitment> active = commitmentRepository.findByDonorIdAndStatus(donorId, "PENDING");
        if (!active.isEmpty()) {
            return CommitmentResult.error("COMMIT-002",
                    "You already have an active commitment. Please fulfill or cancel it first.");
        }

        // Build hospital info for the confirmation message
        String hospitalInfo = "Hospital details unavailable";
        if (bloodRequest.getHospitalId() != null) {
            hospitalInfo = hospitalRepository.findById(bloodRequest.getHospitalId())
                    .map(h -> h.getName() + " — " + h.getAddress() + " — " + h.getPhone())
                    .orElse("Hospital details unavailable");
        }

        Commitment commitment = new Commitment();
        commitment.setRequestId(requestId);
        commitment.setDonorId(donorId);
        commitment.setStatus("PENDING");
        commitment.setCommittedAt(LocalDateTime.now());
        Commitment saved = commitmentRepository.save(commitment);

        return CommitmentResult.success(saved, "Commitment confirmed! Please go to: " + hospitalInfo);
    }

    public List<Map<String, Object>> getCommitmentsForRequest(Long requestId) {
        return buildTickets(commitmentRepository.findByRequestId(requestId));
    }

    public List<Map<String, Object>> getCommitmentsForDonor(Long donorId) {
        return buildTickets(commitmentRepository.findByDonorId(donorId));
    }

    private List<Map<String, Object>> buildTickets(List<Commitment> commitments) {
        List<Map<String, Object>> tickets = new ArrayList<>();
        for (Commitment c : commitments) {
            Map<String, Object> ticket = new HashMap<>();
            ticket.put("id",              c.getId());
            ticket.put("status",          c.getStatus());
            ticket.put("committedAt",     c.getCommittedAt());
            ticket.put("referenceNumber", "DON-" + (1000 + c.getId()));
            ticket.put("requestId",       c.getRequestId());

            requestRepository.findById(c.getRequestId()).ifPresent(req -> {
                ticket.put("bloodTypeNeeded", req.getBloodType());

                // ✅ NEW: Map Requester Details for the Donor's Ticket
                userRepository.findById(req.getRequesterId()).ifPresent(requester -> {
                    ticket.put("requesterName", requester.getFullName());
                    ticket.put("requesterContactNumber", requester.getContactNumber());
                });

                if (req.getHospitalId() != null) {
                    hospitalRepository.findById(req.getHospitalId())
                            .ifPresent(h -> ticket.put("hospitalName", h.getName()));
                } else {
                    ticket.put("hospitalName", "No Hospital Specified");
                }
            });

            tickets.add(ticket);
        }
        return tickets;
    }

    public CancelResult cancelCommitment(Long commitmentId, Long loggedInUserId) {

        Optional<Commitment> opt = commitmentRepository.findById(commitmentId);

        if (opt.isEmpty()) {
            return CancelResult.notFound();
        }

        Commitment commitment = opt.get();

        if (!commitment.getDonorId().equals(loggedInUserId)) {
            return CancelResult.forbidden();
        }

        if (!commitment.getStatus().equals("PENDING")) {
            return CancelResult.notCancellable();
        }

        // Soft-delete — preserve history
        commitment.setStatus("CANCELLED");
        commitmentRepository.save(commitment);

        return CancelResult.success();
    }
}