package com.example.medlocal.service.pharmacy;

import com.example.medlocal.model.pharmacy.Medicine;
import com.example.medlocal.repository.pharmacy.MedicineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MedicineService {

    private final MedicineRepository medicineRepository;

    @Transactional
    public Medicine createMedicine(Medicine medicine) {
        // Check if medicine name already exists
        if (medicineRepository.findByName(medicine.getName()).isPresent()) {
            throw new IllegalArgumentException("Medicine name is already taken");
        }
        return medicineRepository.save(medicine);
    }

    public Optional<Medicine> getMedicineById(Long medicineId) {
        return medicineRepository.findById(medicineId);
    }

    public Optional<Medicine> getMedicineByName(String name) {
        return medicineRepository.findByName(name);
    }

    public List<Medicine> getAllMedicines() {
        return medicineRepository.findAll();
    }

    public Page<Medicine> getAllMedicines(Pageable pageable) {
        return medicineRepository.findAll(pageable);
    }

    public List<Medicine> getActiveMedicines() {
        return medicineRepository.findByIsActiveTrue();
    }

    public List<Medicine> getMedicinesByCategory(String category) {
        return medicineRepository.findByCategory(category);
    }

    public List<Medicine> searchMedicinesByGenericName(String genericName) {
        return medicineRepository.findByGenericNameContainingIgnoreCase(genericName);
    }

    @Transactional
    public Medicine updateMedicine(Long medicineId, Medicine medicineDetails) {
        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new IllegalArgumentException("Medicine not found with id: " + medicineId));

        // Update fields
        if (medicineDetails.getName() != null) {
            // Check if new name conflicts with another medicine
            if (!medicineDetails.getName().equals(medicine.getName()) &&
                    medicineRepository.findByName(medicineDetails.getName()).isPresent()) {
                throw new IllegalArgumentException("Medicine name is already taken");
            }
            medicine.setName(medicineDetails.getName());
        }
        if (medicineDetails.getGenericName() != null) {
            medicine.setGenericName(medicineDetails.getGenericName());
        }
        if (medicineDetails.getBrandName() != null) {
            medicine.setBrandName(medicineDetails.getBrandName());
        }
        if (medicineDetails.getDescription() != null) {
            medicine.setDescription(medicineDetails.getDescription());
        }
        if (medicineDetails.getCategory() != null) {
            medicine.setCategory(medicineDetails.getCategory());
        }
        if (medicineDetails.getSubCategory() != null) {
            medicine.setSubCategory(medicineDetails.getSubCategory());
        }
        if (medicineDetails.getRequiresPrescription() != null) {
            medicine.setRequiresPrescription(medicineDetails.getRequiresPrescription());
        }
        if (medicineDetails.getIsActive() != null) {
            medicine.setIsActive(medicineDetails.getIsActive());
        }

        return medicineRepository.save(medicine);
    }

    @Transactional
    public void deleteMedicine(Long medicineId) {
        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new IllegalArgumentException("Medicine not found with id: " + medicineId));
        medicineRepository.delete(medicine);
    }
}