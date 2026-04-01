package com.bloodbound.backend.identity;

import com.bloodbound.backend.common.DonationCompletedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class UserStatsListener {

    @Autowired private UserRepository userRepository;

    @EventListener
    @Transactional
    public void onDonationCompleted(DonationCompletedEvent event) {

        // Find the user and update their stats
        userRepository.findById(event.donorId()).ifPresent(user -> {

            int currentDonations = (user.getTotalDonations() == null) ? 0 : user.getTotalDonations();
            user.setTotalDonations(currentDonations + 1);
            user.setLastDonationDate(LocalDateTime.now());

            userRepository.save(user);
        });
    }
}