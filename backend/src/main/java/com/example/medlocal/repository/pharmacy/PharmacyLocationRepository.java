package com.example.medlocal.repository.pharmacy;

import com.example.medlocal.model.pharmacy.PharmacyLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PharmacyLocationRepository extends JpaRepository<PharmacyLocation, Long> {
    List<PharmacyLocation> findByPharmacyId(Long pharmacyId);
    List<PharmacyLocation> findByCityAndState(String city, String state);
    Optional<PharmacyLocation> findByPharmacyIdAndIsPrimaryTrue(Long pharmacyId);
}