package com.example.medlocal.repository.pharmacy;

import com.example.medlocal.model.pharmacy.PharmacyLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PharmacyLocationRepository extends JpaRepository<PharmacyLocation, Long> {
    List<PharmacyLocation> findByPharmacyId(Long pharmacyId);
    List<PharmacyLocation> findByCityAndState(String city, String state);
    List<PharmacyLocation> findByPharmacyIdAndIsPrimaryTrue(Long pharmacyId);

    @Query(value = "SELECT * FROM pharmacy_locations WHERE ST_DWithin(coordinates::geography, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography, :distance) = true", nativeQuery = true)
    List<PharmacyLocation> findNearby(@Param("longitude") Double longitude, @Param("latitude") Double latitude, @Param("distance") Double distance);
}