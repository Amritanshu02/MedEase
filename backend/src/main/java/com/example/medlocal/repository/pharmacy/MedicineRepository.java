package com.example.medlocal.repository.pharmacy;

import com.example.medlocal.model.pharmacy.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    Optional<Medicine> findByName(String name);
    List<Medicine> findByCategory(String category);
    List<Medicine> findByGenericNameContainingIgnoreCase(String genericName);
    List<Medicine> findByIsActiveTrue();
    List<Medicine> findByNameContainingIgnoreCase(String name);
}