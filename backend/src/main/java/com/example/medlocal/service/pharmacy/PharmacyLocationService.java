package com.example.medlocal.service.pharmacy;

import com.example.medlocal.model.pharmacy.Pharmacy;
import com.example.medlocal.model.pharmacy.PharmacyLocation;
import com.example.medlocal.repository.pharmacy.PharmacyLocationRepository;
import com.example.medlocal.repository.pharmacy.PharmacyRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PharmacyLocationService {

    private final PharmacyLocationRepository locationRepository;
    private final PharmacyRepository pharmacyRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory();

    @Transactional
    public PharmacyLocation addPharmacyLocation(Long pharmacyId, PharmacyLocation location) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new IllegalArgumentException("Pharmacy not found with id: " + pharmacyId));

        location.setPharmacy(pharmacy);

        // Coordinates should already be set by the controller if latitude/longitude were provided
        // No need to check for latitude/longitude here as the entity uses a Point for coordinates

        // If this is set as primary location, unset any existing primary location for this pharmacy
        if (location.getIsPrimary() != null && location.getIsPrimary()) {
            List<PharmacyLocation> existingPrimaryLocations = locationRepository.findByPharmacyIdAndIsPrimaryTrue(pharmacyId);
            for (PharmacyLocation existingPrimary : existingPrimaryLocations) {
                existingPrimary.setIsPrimary(false);
                locationRepository.save(existingPrimary);
            }
        }

        return locationRepository.save(location);
    }

    public List<PharmacyLocation> getPharmacyLocations(Long pharmacyId) {
        return locationRepository.findByPharmacyId(pharmacyId);
    }

    public Optional<PharmacyLocation> getPharmacyLocationById(Long locationId) {
        return locationRepository.findById(locationId);
    }

    public List<PharmacyLocation> getPrimaryPharmacyLocation(Long pharmacyId) {
        return locationRepository.findByPharmacyIdAndIsPrimaryTrue(pharmacyId);
    }

    @Transactional
    public PharmacyLocation updatePharmacyLocation(Long locationId, PharmacyLocation locationDetails) {
        PharmacyLocation location = locationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("PharmacyLocation not found with id: " + locationId));

        // Update fields
        if (locationDetails.getLocationName() != null) {
            location.setLocationName(locationDetails.getLocationName());
        }
        if (locationDetails.getAddressLine1() != null) {
            location.setAddressLine1(locationDetails.getAddressLine1());
        }
        if (locationDetails.getAddressLine2() != null) {
            location.setAddressLine2(locationDetails.getAddressLine2());
        }
        if (locationDetails.getCity() != null) {
            location.setCity(locationDetails.getCity());
        }
        if (locationDetails.getState() != null) {
            location.setState(locationDetails.getState());
        }
        if (locationDetails.getPostalCode() != null) {
            location.setPostalCode(locationDetails.getPostalCode());
        }
        if (locationDetails.getCountry() != null) {
            location.setCountry(locationDetails.getCountry());
        }
        if (locationDetails.getPhone() != null) {
            location.setPhone(locationDetails.getPhone());
        }
        if (locationDetails.getIsPrimary() != null) {
            location.setIsPrimary(locationDetails.getIsPrimary());
            // Handle primary location logic if needed
        }
        if (locationDetails.getIsActive() != null) {
            location.setIsActive(locationDetails.getIsActive());
        }

        // Coordinates should already be set by the controller if latitude/longitude were provided
        // No need to check for latitude/longitude here as the entity uses a Point for coordinates

        return locationRepository.save(location);
    }

    @Transactional
    public void deletePharmacyLocation(Long locationId) {
        PharmacyLocation location = locationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("PharmacyLocation not found with id: " + locationId));
        locationRepository.delete(location);
    }
}