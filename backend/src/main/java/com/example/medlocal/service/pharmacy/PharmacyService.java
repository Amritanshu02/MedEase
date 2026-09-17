package com.example.medlocal.service.pharmacy;

import com.example.medlocal.model.pharmacy.Pharmacy;
import com.example.medlocal.model.User;
import com.example.medlocal.repository.pharmacy.PharmacyRepository;
import com.example.medlocal.repository.UserRepository;
import com.example.medlocal.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PharmacyService {

    private final PharmacyRepository pharmacyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Pharmacy registerPharmacy(Pharmacy pharmacy, User adminUser) {
        // Check if pharmacy name or license number already exists
        if (pharmacyRepository.findByName(pharmacy.getName()).isPresent()) {
            throw new IllegalArgumentException("Pharmacy name is already taken");
        }
        if (pharmacyRepository.findByLicenseNumber(pharmacy.getLicenseNumber()).isPresent()) {
            throw new IllegalArgumentException("Pharmacy license number is already registered");
        }

        // Encode password for admin user
        String encodedPassword = passwordEncoder.encode(adminUser.getPassword());
        adminUser.setPassword(encodedPassword);

        // Save admin user first
        User savedAdminUser = userRepository.save(adminUser);

        // Set the admin user on pharmacy
        pharmacy.setAdminUser(savedAdminUser);

        // Save pharmacy
        return pharmacyRepository.save(pharmacy);
    }

    public Optional<Pharmacy> getPharmacyById(Long pharmacyId) {
        return pharmacyRepository.findById(pharmacyId);
    }

    public Optional<Pharmacy> getPharmacyByName(String name) {
        return pharmacyRepository.findByName(name);
    }

    public Optional<Pharmacy> getPharmacyByLicenseNumber(String licenseNumber) {
        return pharmacyRepository.findByLicenseNumber(licenseNumber);
    }
}