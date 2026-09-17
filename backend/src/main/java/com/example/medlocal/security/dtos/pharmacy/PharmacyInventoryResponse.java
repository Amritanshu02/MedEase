package com.example.medlocal.security.dtos.pharmacy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PharmacyInventoryResponse {
    private Long id;
    private Long pharmacyId;
    private Long medicineId;
    private String pharmacyName;
    private String medicineName;
    private String genericName;
    private Integer stockQuantity;
    private BigDecimal price;
    private Boolean isAvailable;
    private LocalDateTime expiryDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}