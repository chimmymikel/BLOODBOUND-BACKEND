package com.bloodbound.backend.hospital;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private HospitalRepository hospitalRepository;

    @Override
    public void run(String... args) {
        // Only seed if the table is empty
        if (hospitalRepository.count() > 0) return;

        hospitalRepository.save(createHospital("Cebu City Medical Center",     "Osmena Blvd, Cebu City",          10.3103, 123.8931, "(032) 255-1111"));
        hospitalRepository.save(createHospital("Vicente Sotto Memorial Medical Center", "B. Rodriguez St, Cebu City", 10.3157, 123.9050, "(032) 253-9891"));
        hospitalRepository.save(createHospital("Chong Hua Hospital",            "Don Mariano Cui St, Cebu City",   10.3221, 123.9000, "(032) 255-8000"));
        hospitalRepository.save(createHospital("Cebu Doctors' University Hospital", "Osmena Blvd, Cebu City",     10.3068, 123.8911, "(032) 255-5555"));
        hospitalRepository.save(createHospital("Perpetual Succour Hospital",    "Gorordo Ave, Cebu City",          10.3280, 123.9120, "(032) 233-8620"));
        hospitalRepository.save(createHospital("Sugbo Medical Center",          "Ouano Ave, Mandaue City",         10.3494, 123.9397, "(032) 505-6000"));

        System.out.println("✅ Hospital data seeded successfully!");
    }

    private Hospital createHospital(String name, String address,
                                    double lat, double lng, String phone) {
        Hospital h = new Hospital();
        h.setName(name);
        h.setAddress(address);
        h.setLatitude(lat);
        h.setLongitude(lng);
        h.setPhone(phone);
        return h;
    }
}
