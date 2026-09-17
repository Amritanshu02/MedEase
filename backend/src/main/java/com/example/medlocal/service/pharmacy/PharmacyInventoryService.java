package com.example.medlocal.service.pharmacy;

import com.example.medlocal.model.pharmacy.Medicine;
import com.example.medlocal.model.pharmacy.Pharmacy;
import com.example.medlocal.model.pharmacy.PharmacyInventory;
import com.example.medlocal.repository.pharmacy.MedicineRepository;
import com.example.medlocal.repository.pharmacy.PharmacyInventoryRepository;
import com.example.medlocal.repository.pharmacy.PharmacyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PharmacyInventoryService {

    private final PharmacyInventoryRepository inventoryRepository;
    private final PharmacyRepository pharmacyRepository;
    private final MedicineRepository medicineRepository;

    @Transactional
    public PharmacyInventory addOrUpdateInventory(Long pharmacyId, Long medicineId, Integer stockQuantity, Double price) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new IllegalArgumentException("Pharmacy not found with id: " + pharmacyId));

        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new IllegalArgumentException("Medicine not found with id: " + medicineId));

        Optional<PharmacyInventory> existingInventory = inventoryRepository.findByPharmacyIdAndMedicineId(pharmacyId, medicineId);

        PharmacyInventory inventory;
        if (existingInventory.isPresent()) {
            inventory = existingInventory.get();
            inventory.setStockQuantity(stockQuantity);
            // Update price if provided
            if (price != null) {
                inventory.setPrice(BigDecimal.valueOf(price));
            }
        } else {
            inventory = PharmacyInventory.builder()
                    .pharmacy(pharmacy)
                    .medicine(medicine)
                    .stockQuantity(stockQuantity)
                    .price(BigDecimal.valueOf(price))
                    .isAvailable(true)
                    .build();
        }

        return inventoryRepository.save(inventory);
    }

    @Transactional
    public boolean updateStock(Long pharmacyId, Long medicineId, Integer quantityChange) {
        PharmacyInventory inventory = inventoryRepository.findByPharmacyIdAndMedicineId(pharmacyId, medicineId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found for pharmacyId: " + pharmacyId + " and medicineId: " + medicineId));

        int newStock = inventory.getStockQuantity() + quantityChange;
        if (newStock < 0) {
            throw new IllegalArgumentException("Insufficient stock. Available: " + inventory.getStockQuantity());
        }

        inventory.setStockQuantity(newStock);
        inventory.setIsAvailable(newStock > 0);
        inventoryRepository.save(inventory);

        return true;
    }

    public List<PharmacyInventory> getPharmacyInventory(Long pharmacyId) {
        return inventoryRepository.findByPharmacyId(pharmacyId);
    }

    public List<PharmacyInventory> getMedicineInventory(Long medicineId) {
        return inventoryRepository.findByMedicineId(medicineId);
    }

    public List<PharmacyInventory> getAvailableMedicineInventory(Long medicineId) {
        return inventoryRepository.findAvailableByMedicineId(medicineId);
    }

    public Optional<PharmacyInventory> getInventoryByPharmacyAndMedicine(Long pharmacyId, Long medicineId) {
        return inventoryRepository.findByPharmacyIdAndMedicineId(pharmacyId, medicineId);
    }
}