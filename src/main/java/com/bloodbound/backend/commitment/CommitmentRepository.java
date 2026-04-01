package com.bloodbound.backend.commitment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommitmentRepository extends JpaRepository<Commitment, Long> {

    // Get all commitments for a specific request (Requester sees who committed)
    List<Commitment> findByRequestId(Long requestId);

    // Get all commitments by a specific donor (Donor sees their history)
    List<Commitment> findByDonorId(Long donorId);

    // Check if a donor already committed to a specific request (prevent duplicates)
    Optional<Commitment> findByRequestIdAndDonorId(Long requestId, Long donorId);

    // Get all ACTIVE commitments for a donor (for eligibility + fulfill logic)
    List<Commitment> findByDonorIdAndStatus(Long donorId, String status);

    // Get all commitments for a request with a specific status
    List<Commitment> findByRequestIdAndStatus(Long requestId, String status);
}
