package com.example.medlocal.repository.pharmacy;

import com.example.medlocal.model.pharmacy.Pharmacy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PharmacyRepository extends JpaRepository<Pharmacy, Long> {
    Optional<Pharmacy> findByName(String name);
    Optional<Pharmacy> findByLicenseNumber(String licenseNumber);
}