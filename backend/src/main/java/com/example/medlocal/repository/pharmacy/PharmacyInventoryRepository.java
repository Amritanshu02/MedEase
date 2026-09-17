package com.example.medlocal.repository.pharmacy;

import com.example.medlocal.model.pharmacy.PharmacyInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PharmacyInventoryRepository extends JpaRepository<PharmacyInventory, Long> {
    Optional<PharmacyInventory> findByPharmacyIdAndMedicineId(Long pharmacyId, Long medicineId);
    List<PharmacyInventory> findByPharmacyId(Long pharmacyId);
    List<PharmacyInventory> findByMedicineId(Long medicineId);
    List<PharmacyInventory> findByPharmacyIdAndIsAvailableTrue(Long pharmacyId);

    @Query("SELECT pi FROM PharmacyInventory pi WHERE pi.medicine.id = :medicineId AND pi.isAvailable = true AND pi.stockQuantity > 0")
    List<PharmacyInventory> findAvailableByMedicineId(@Param("medicineId") Long medicineId);
}