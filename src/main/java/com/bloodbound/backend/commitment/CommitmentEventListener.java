package com.bloodbound.backend.commitment;

import com.bloodbound.backend.common.DonationCompletedEvent;
import com.bloodbound.backend.common.RequestFulfilledEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class CommitmentEventListener {

    @Autowired private CommitmentRepository commitmentRepository;
    @Autowired private ApplicationEventPublisher eventPublisher;

    // The @EventListener tag makes Spring "listen" for this specific letter
    @EventListener
    @Transactional
    public void onRequestFulfilled(RequestFulfilledEvent event) {

        // Find everyone who committed to this specific request
        List<Commitment> commitments = commitmentRepository.findByRequestIdAndStatus(event.requestId(), "PENDING");

        for (Commitment c : commitments) {
            // Update their commitment to completed
            c.setStatus("COMPLETED");
            commitmentRepository.save(c);

            // Send a NEW letter telling the Identity module to update their profile stats!
            eventPublisher.publishEvent(new DonationCompletedEvent(c.getDonorId()));
        }
    }
}