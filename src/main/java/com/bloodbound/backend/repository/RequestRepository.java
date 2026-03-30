package com.bloodbound.backend.repository;

import com.bloodbound.backend.model.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    // Get all requests by status (e.g. ACTIVE)
    List<Request> findByStatus(String status);

    // Get all requests by status AND blood type (for donor filtering)
    List<Request> findByStatusAndBloodType(String status, String bloodType);

    // Get all requests posted by a specific requester
    List<Request> findByRequesterId(Long requesterId);
}
