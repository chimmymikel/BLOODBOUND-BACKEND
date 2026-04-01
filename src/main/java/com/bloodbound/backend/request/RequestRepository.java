package com.bloodbound.backend.request;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findByStatus(String status);

    List<Request> findByStatusAndBloodType(String status, String bloodType);

    List<Request> findByStatusAndUrgency(String status, String urgency);

    List<Request> findByStatusAndBloodTypeAndUrgency(String status, String bloodType, String urgency);

    List<Request> findByRequesterId(Long requesterId);
}