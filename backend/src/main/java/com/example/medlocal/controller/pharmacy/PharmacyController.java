package com.example.medlocal.controller.pharmacy;

import com.example.medlocal.model.User;
import com.example.medlocal.model.User.Role;
import com.example.medlocal.model.pharmacy.Pharmacy;
import com.example.medlocal.model.pharmacy.PharmacyLocation;
import com.example.medlocal.model.pharmacy.Medicine;
import com.example.medlocal.model.pharmacy.PharmacyInventory;
import com.example.medlocal.repository.UserRepository;
import com.example.medlocal.security.JwtTokenUtil;
import com.example.medlocal.security.dtos.LoginRequest;
import com.example.medlocal.security.dtos.LoginResponse;
import com.example.medlocal.security.dtos.pharmacy.*;
import com.example.medlocal.service.pharmacy.PharmacyInventoryService;
import com.example.medlocal.service.pharmacy.PharmacyLocationService;
import com.example.medlocal.service.pharmacy.PharmacyService;
import com.example.medlocal.service.pharmacy.MedicineService;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.medlocal.service.pharmacy.MedicineCsvUploadService;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pharmacies")
@RequiredArgsConstructor
public class PharmacyController {

    private final PharmacyService pharmacyService;
    private final PharmacyLocationService pharmacyLocationService;
    private final MedicineService medicineService;
    private final PharmacyInventoryService pharmacyInventoryService;
    private final MedicineCsvUploadService medicineCsvUploadService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;

    // Pharmacy Onboarding Endpoint
    @PostMapping("/register")
    public ResponseEntity<?> registerPharmacy(@RequestBody PharmacyRegisterRequest request) {
        try {
            // Create pharmacy entity from request
            Pharmacy pharmacy = Pharmacy.builder()
                    .name(request.getName())
                    .licenseNumber(request.getLicenseNumber())
                    .phone(request.getPhone())
                    .addressLine1(request.getAddressLine1())
                    .addressLine2(request.getAddressLine2())
                    .city(request.getCity())
                    .state(request.getState())
                    .postalCode(request.getPostalCode())
                    .country(request.getCountry())
                    .isActive(true)
                    .build();

            // Create admin user entity
            User adminUser = User.builder()
                    .username(request.getAdminUsername())
                    .email(request.getAdminEmail())
                    .password(request.getAdminPassword()) // Will be encoded in service
                    .role(request.getAdminRole())
                    .build();

            // Register pharmacy
            Pharmacy registeredPharmacy = pharmacyService.registerPharmacy(pharmacy, adminUser);

            // If primary location is provided, create it
            if (request.getLocationName() != null && request.getLatitude() != null && request.getLongitude() != null) {
                PharmacyLocation location = PharmacyLocation.builder()
                        .locationName(request.getLocationName())
                        .addressLine1(request.getLocationAddressLine1() != null ? request.getLocationAddressLine1() : request.getAddressLine1())
                        .addressLine2(request.getLocationAddressLine2())
                        .city(request.getLocationCity() != null ? request.getLocationCity() : request.getCity())
                        .state(request.getLocationState() != null ? request.getLocationState() : request.getState())
                        .postalCode(request.getLocationPostalCode() != null ? request.getLocationPostalCode() : request.getPostalCode())
                        .country(request.getLocationCountry() != null ? request.getLocationCountry() : request.getCountry())
                        .phone(request.getLocationPhone() != null ? request.getLocationPhone() : request.getPhone())
                        .isPrimary(true)
                        .isActive(true)
                        .build();

                // Set coordinates using GeometryFactory since we have separate latitude/longitude
                GeometryFactory geometryFactory = new GeometryFactory();
                Point point = geometryFactory.createPoint(new Coordinate(request.getLongitude(), request.getLatitude()));
                location.setCoordinates(point);

                pharmacyLocationService.addPharmacyLocation(registeredPharmacy.getId(), location);
            }

            PharmacyResponse response = PharmacyResponse.builder()
                    .id(registeredPharmacy.getId())
                    .name(registeredPharmacy.getName())
                    .licenseNumber(registeredPharmacy.getLicenseNumber())
                    .phone(registeredPharmacy.getPhone())
                    .addressLine1(registeredPharmacy.getAddressLine1())
                    .addressLine2(registeredPharmacy.getAddressLine2())
                    .city(registeredPharmacy.getCity())
                    .state(registeredPharmacy.getState())
                    .postalCode(registeredPharmacy.getPostalCode())
                    .country(registeredPharmacy.getCountry())
                    .isActive(registeredPharmacy.getIsActive())
                    .createdAt(registeredPharmacy.getCreatedAt())
                    .updatedAt(registeredPharmacy.getUpdatedAt())
                    .adminUsername(registeredPharmacy.getAdminUser().getUsername())
                    .adminEmail(registeredPharmacy.getAdminUser().getEmail())
                    .build();

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    // Get pharmacy by ID
    @GetMapping("/{pharmacyId}")
    public ResponseEntity<?> getPharmacyById(@PathVariable Long pharmacyId) {
        return pharmacyService.getPharmacyById(pharmacyId)
                .map(pharmacy -> ResponseEntity.ok(PharmacyResponse.builder()
                        .id(pharmacy.getId())
                        .name(pharmacy.getName())
                        .licenseNumber(pharmacy.getLicenseNumber())
                        .phone(pharmacy.getPhone())
                        .addressLine1(pharmacy.getAddressLine1())
                        .addressLine2(pharmacy.getAddressLine2())
                        .city(pharmacy.getCity())
                        .state(pharmacy.getState())
                        .postalCode(pharmacy.getPostalCode())
                        .country(pharmacy.getCountry())
                        .isActive(pharmacy.getIsActive())
                        .createdAt(pharmacy.getCreatedAt())
                        .updatedAt(pharmacy.getUpdatedAt())
                        .adminUsername(pharmacy.getAdminUser().getUsername())
                        .adminEmail(pharmacy.getAdminUser().getEmail())
                        .build()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Login endpoint for pharmacy admin (similar to auth controller but returns pharmacy info)
    @PostMapping("/login")
    public ResponseEntity<?> loginPharmacyAdmin(@RequestBody LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.badRequest().body("Incorrect username or password");
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
        final String token = jwtTokenUtil.generateToken(userDetails);

        // Get user info
        Optional<User> optionalUser = userRepository.findByUsername(loginRequest.getUsername());
        String role = optionalUser.map(user -> user.getRole().name()).orElse("UNKNOWN");

        LoginResponse loginResponse = LoginResponse.builder()
                .token(token)
                .username(userDetails.getUsername())
                .role(role)
                .build();

        return ResponseEntity.ok(loginResponse);
    }

    // Pharmacy Location Endpoints
    @PostMapping("/{pharmacyId}/locations")
    public ResponseEntity<?> addPharmacyLocation(@PathVariable Long pharmacyId, @RequestBody PharmacyLocationRequest request) {
        try {
            PharmacyLocation location = PharmacyLocation.builder()
                    .locationName(request.getLocationName())
                    .addressLine1(request.getAddressLine1())
                    .addressLine2(request.getAddressLine2())
                    .city(request.getCity())
                    .state(request.getState())
                    .postalCode(request.getPostalCode())
                    .country(request.getCountry())
                    .phone(request.getPhone())
                    .isPrimary(request.getIsPrimary())
                    .isActive(request.getIsActive())
                    .build();

            // Set coordinates using GeometryFactory since we have separate latitude/longitude
            if (request.getLatitude() != null && request.getLongitude() != null) {
                GeometryFactory geometryFactory = new GeometryFactory();
                Point point = geometryFactory.createPoint(new Coordinate(request.getLongitude(), request.getLatitude()));
                location.setCoordinates(point);
            }

            PharmacyLocation savedLocation = pharmacyLocationService.addPharmacyLocation(pharmacyId, location);

            PharmacyLocationResponse response = PharmacyLocationResponse.builder()
                    .id(savedLocation.getId())
                    .pharmacyId(savedLocation.getPharmacy().getId())
                    .pharmacyName(savedLocation.getPharmacy().getName())
                    .locationName(savedLocation.getLocationName())
                    .addressLine1(savedLocation.getAddressLine1())
                    .addressLine2(savedLocation.getAddressLine2())
                    .city(savedLocation.getCity())
                    .state(savedLocation.getState())
                    .postalCode(savedLocation.getPostalCode())
                    .country(savedLocation.getCountry())
                    .phone(savedLocation.getPhone())
                    .latitude(savedLocation.getCoordinates() != null ? savedLocation.getCoordinates().getY() : null) // latitude
                    .longitude(savedLocation.getCoordinates() != null ? savedLocation.getCoordinates().getX() : null) // longitude
                    .isPrimary(savedLocation.getIsPrimary())
                    .isActive(savedLocation.getIsActive())
                    .createdAt(savedLocation.getCreatedAt())
                    .updatedAt(savedLocation.getUpdatedAt())
                    .build();

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/{pharmacyId}/locations")
    public ResponseEntity<?> getPharmacyLocations(@PathVariable Long pharmacyId) {
        try {
            List<PharmacyLocation> locations = pharmacyLocationService.getPharmacyLocations(pharmacyId);
            List<PharmacyLocationResponse> responses = locations.stream()
                    .map(location -> PharmacyLocationResponse.builder()
                            .id(location.getId())
                            .pharmacyId(location.getPharmacy().getId())
                            .pharmacyName(location.getPharmacy().getName())
                            .locationName(location.getLocationName())
                            .addressLine1(location.getAddressLine1())
                            .addressLine2(location.getAddressLine2())
                            .city(location.getCity())
                            .state(location.getState())
                            .postalCode(location.getPostalCode())
                            .country(location.getCountry())
                            .phone(location.getPhone())
                            .latitude(location.getCoordinates() != null ? location.getCoordinates().getY() : null) // latitude
                            .longitude(location.getCoordinates() != null ? location.getCoordinates().getX() : null) // longitude
                            .isPrimary(location.getIsPrimary())
                            .isActive(location.getIsActive())
                            .createdAt(location.getCreatedAt())
                            .updatedAt(location.getUpdatedAt())
                            .build())
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    // Medicine Management Endpoints
    @PostMapping("/medicines")
    public ResponseEntity<?> createMedicine(@RequestBody MedicineRequest request) {
        try {
            Medicine medicine = Medicine.builder()
                    .name(request.getName())
                    .genericName(request.getGenericName())
                    .brandName(request.getBrandName())
                    .description(request.getDescription())
                    .category(request.getCategory())
                    .subCategory(request.getSubCategory())
                    .requiresPrescription(request.getRequiresPrescription())
                    .isActive(true)
                    .build();

            Medicine savedMedicine = medicineService.createMedicine(medicine);

            MedicineResponse response = MedicineResponse.builder()
                    .id(savedMedicine.getId())
                    .name(savedMedicine.getName())
                    .genericName(savedMedicine.getGenericName())
                    .brandName(savedMedicine.getBrandName())
                    .description(savedMedicine.getDescription())
                    .category(savedMedicine.getCategory())
                    .subCategory(savedMedicine.getSubCategory())
                    .requiresPrescription(savedMedicine.getRequiresPrescription())
                    .isActive(savedMedicine.getIsActive())
                    .createdAt(savedMedicine.getCreatedAt())
                    .updatedAt(savedMedicine.getUpdatedAt())
                    .build();

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/medicines")
    public ResponseEntity<?> getAllMedicines(Pageable pageable) {
        try {
            Page<Medicine> medicinePage = medicineService.getAllMedicines(pageable);
            Page<MedicineResponse> responsePage = medicinePage.map(medicine -> MedicineResponse.builder()
                    .id(medicine.getId())
                    .name(medicine.getName())
                    .genericName(medicine.getGenericName())
                    .brandName(medicine.getBrandName())
                    .description(medicine.getDescription())
                    .category(medicine.getCategory())
                    .subCategory(medicine.getSubCategory())
                    .requiresPrescription(medicine.getRequiresPrescription())
                    .isActive(medicine.getIsActive())
                    .createdAt(medicine.getCreatedAt())
                    .updatedAt(medicine.getUpdatedAt())
                    .build());
            return ResponseEntity.ok(responsePage);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/medicines/search")
    public ResponseEntity<?> searchMedicineAcrossPharmacies(@RequestParam String name) {
        try {
            List<PharmacyInventory> inventoryList = pharmacyInventoryService.searchMedicineAcrossPharmacies(name);
            List<PharmacyInventoryResponse> responses = inventoryList.stream()
                    .map(inventory -> PharmacyInventoryResponse.builder()
                            .id(inventory.getId())
                            .pharmacyId(inventory.getPharmacy().getId())
                            .pharmacyName(inventory.getPharmacy().getName())
                            .medicineId(inventory.getMedicine().getId())
                            .medicineName(inventory.getMedicine().getName())
                            .genericName(inventory.getMedicine().getGenericName())
                            .stockQuantity(inventory.getStockQuantity())
                            .price(inventory.getPrice())
                            .isAvailable(inventory.getIsAvailable())
                            .expiryDate(inventory.getExpiryDate())
                            .createdAt(inventory.getCreatedAt())
                            .updatedAt(inventory.getUpdatedAt())
                            .build())
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    // Pharmacy Inventory Endpoints
    @PostMapping("/{pharmacyId}/inventory")
    public ResponseEntity<?> addOrUpdateInventory(@PathVariable Long pharmacyId, @RequestBody PharmacyInventoryRequest request) {
        try {
            PharmacyInventory inventory = pharmacyInventoryService.addOrUpdateInventory(
                    pharmacyId,
                    request.getMedicineId(),
                    request.getStockQuantity(),
                    request.getPrice()
            );

            PharmacyInventoryResponse response = PharmacyInventoryResponse.builder()
                    .id(inventory.getId())
                    .pharmacyId(inventory.getPharmacy().getId())
                    .pharmacyName(inventory.getPharmacy().getName())
                    .medicineId(inventory.getMedicine().getId())
                    .medicineName(inventory.getMedicine().getName())
                    .genericName(inventory.getMedicine().getGenericName())
                    .stockQuantity(inventory.getStockQuantity())
                    .price(inventory.getPrice())
                    .isAvailable(inventory.getIsAvailable())
                    .expiryDate(inventory.getExpiryDate())
                    .createdAt(inventory.getCreatedAt())
                    .updatedAt(inventory.getUpdatedAt())
                    .build();

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/{pharmacyId}/inventory")
    public ResponseEntity<?> getPharmacyInventory(@PathVariable Long pharmacyId) {
        try {
            List<PharmacyInventory> inventoryList = pharmacyInventoryService.getPharmacyInventory(pharmacyId);
            List<PharmacyInventoryResponse> responses = inventoryList.stream()
                    .map(inventory -> PharmacyInventoryResponse.builder()
                            .id(inventory.getId())
                            .pharmacyId(inventory.getPharmacy().getId())
                            .pharmacyName(inventory.getPharmacy().getName())
                            .medicineId(inventory.getMedicine().getId())
                            .medicineName(inventory.getMedicine().getName())
                            .genericName(inventory.getMedicine().getGenericName())
                            .stockQuantity(inventory.getStockQuantity())
                            .price(inventory.getPrice())
                            .isAvailable(inventory.getIsAvailable())
                            .expiryDate(inventory.getExpiryDate())
                            .createdAt(inventory.getCreatedAt())
                            .updatedAt(inventory.getUpdatedAt())
                            .build())
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @PutMapping("/{pharmacyId}/inventory/{medicineId}/stock")
    public ResponseEntity<?> updateStock(@PathVariable Long pharmacyId, @PathVariable Long medicineId, @RequestParam Integer quantityChange) {
        try {
            boolean success = pharmacyInventoryService.updateStock(pharmacyId, medicineId, quantityChange);
            if (success) {
                return ResponseEntity.ok().body("Stock updated successfully");
            } else {
                return ResponseEntity.badRequest().body("Failed to update stock");
            }
        } catch (IllegalArgumentException e) {
             return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/nearby")
    public ResponseEntity<?> getNearbyPharmacies(@RequestParam Double latitude, @RequestParam Double longitude, @RequestParam Double radius) {
        try {
            List<PharmacyLocation> nearbyLocations = pharmacyLocationService.findNearby(longitude, latitude, radius);
            List<PharmacyLocationResponse> responses = nearbyLocations.stream()
                    .map(location -> PharmacyLocationResponse.builder()
                            .id(location.getId())
                            .pharmacyId(location.getPharmacy().getId())
                            .pharmacyName(location.getPharmacy().getName())
                            .locationName(location.getLocationName())
                            .addressLine1(location.getAddressLine1())
                            .addressLine2(location.getAddressLine2())
                            .city(location.getCity())
                            .state(location.getState())
                            .postalCode(location.getPostalCode())
                            .country(location.getCountry())
                            .phone(location.getPhone())
                            .latitude(location.getCoordinates() != null ? location.getCoordinates().getY() : null)
                            .longitude(location.getCoordinates() != null ? location.getCoordinates().getX() : null)
                            .isPrimary(location.getIsPrimary())
                            .isActive(location.getIsActive())
                            .createdAt(location.getCreatedAt())
                            .updatedAt(location.getUpdatedAt())
                            .build())
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    /**
     * Upload medicines via CSV file.
     * Expected CSV format: name,genericName,brandName,description,category,subCategory,requiresPrescription
     *
     * @param file CSV file containing medicine data
     * @return Upload result with success/error counts and details
     */
    @PostMapping("/medicines/upload")
    public ResponseEntity<?> uploadMedicinesCsv(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Please upload a CSV file");
            }

            if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
                return ResponseEntity.badRequest().body("Please upload a CSV file");
            }

            MedicineCsvUploadService.MedicineCsvUploadResponse response = medicineCsvUploadService.processCsvUpload(file);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    // DTO classes for requests (since we don't have them yet)
    public static class PharmacyLocationRequest {
        private String locationName;
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String postalCode;
        private String country;
        private String phone;
        private Boolean isPrimary;
        private Boolean isActive;
        private Double latitude;
        private Double longitude;

        // Getters and setters
        public String getLocationName() { return locationName; }
        public void setLocationName(String locationName) { this.locationName = locationName; }
        public String getAddressLine1() { return addressLine1; }
        public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }
        public String getAddressLine2() { return addressLine2; }
        public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
        public String getPostalCode() { return postalCode; }
        public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public Boolean getIsPrimary() { return isPrimary; }
        public void setIsPrimary(Boolean isPrimary) { this.isPrimary = isPrimary; }
        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }
        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }
    }

    public static class MedicineRequest {
        private String name;
        private String genericName;
        private String brandName;
        private String description;
        private String category;
        private String subCategory;
        private Boolean requiresPrescription;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getGenericName() { return genericName; }
        public void setGenericName(String genericName) { this.genericName = genericName; }
        public String getBrandName() { return brandName; }
        public void setBrandName(String brandName) { this.brandName = brandName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getSubCategory() { return subCategory; }
        public void setSubCategory(String subCategory) { this.subCategory = subCategory; }
        public Boolean getRequiresPrescription() { return requiresPrescription; }
        public void setRequiresPrescription(Boolean requiresPrescription) { this.requiresPrescription = requiresPrescription; }
    }

    public static class PharmacyInventoryRequest {
        private Long medicineId;
        private Integer stockQuantity;
        private Double price;

        // Getters and setters
        public Long getMedicineId() { return medicineId; }
        public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }
        public Integer getStockQuantity() { return stockQuantity; }
        public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
    }
}