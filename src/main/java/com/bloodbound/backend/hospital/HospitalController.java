package com.bloodbound.backend.hospital;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/hospitals")
@CrossOrigin(origins = "*") // Allows your React app to fetch this data
public class HospitalController {

    @Autowired
    private HospitalRepository hospitalRepository;

    @GetMapping
    public ResponseEntity<?> getAllHospitals() {
        List<Hospital> hospitals = hospitalRepository.findAll();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", hospitals
        ));
    }
}