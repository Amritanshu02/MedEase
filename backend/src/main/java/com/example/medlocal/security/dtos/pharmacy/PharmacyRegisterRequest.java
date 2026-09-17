package com.example.medlocal.security.dtos.pharmacy;

import com.example.medlocal.model.User.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PharmacyRegisterRequest {
    // Pharmacy details
    private String name;
    private String licenseNumber;
    private String phone;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    // Admin user details
    private String adminUsername;
    private String adminEmail;
    private String adminPassword;
    private Role adminRole = Role.PHARMACY_ADMIN;

    // Primary location details (optional)
    private String locationName;
    private String locationAddressLine1;
    private String locationAddressLine2;
    private String locationCity;
    private String locationState;
    private String locationPostalCode;
    private String locationCountry;
    private String locationPhone;
    private Double latitude;  // For PostGIS point
    private Double longitude; // For PostGIS point
}