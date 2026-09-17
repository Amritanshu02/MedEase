package com.example.medlocal.security.dtos.pharmacy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PharmacyLocationResponse {
    private Long id;
    private Long pharmacyId;
    private String pharmacyName;
    private String locationName;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String phone;
    private Double latitude;  // Extracted from PostGIS Point
    private Double longitude; // Extracted from PostGIS Point
    private Boolean isPrimary;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}