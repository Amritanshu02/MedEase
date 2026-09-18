package com.example.medlocal.service.pharmacy;

import com.example.medlocal.model.pharmacy.Medicine;
import com.example.medlocal.repository.pharmacy.MedicineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for handling CSV upload of medicine data.
 */
@Service
@RequiredArgsConstructor
public class MedicineCsvUploadService {

    private final MedicineRepository medicineRepository;

    /**
     * Process CSV file upload for medicines.
     *
     * @param file CSV file containing medicine data
     * @return Upload result with success/error counts and details
     * @throws IOException if there's an error reading the file
     */
    public MedicineCsvUploadResponse processCsvUpload(MultipartFile file) throws IOException {
        MedicineCsvUploadResponse response = new MedicineCsvUploadResponse();
        List<MedicineCsvUploadError> errors = new ArrayList<>();
        List<Medicine> medicinesToSave = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isHeader = true;
            int rowNumber = 0;

            while ((line = br.readLine()) != null) {
                rowNumber++;

                // Skip header row
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                // Skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }

                try {
                    Medicine medicine = parseCsvLine(line, rowNumber);
                    if (medicine != null) {
                        medicinesToSave.add(medicine);
                    }
                } catch (IllegalArgumentException e) {
                    errors.add(new MedicineCsvUploadError(rowNumber, e.getMessage()));
                }
            }
        }

        // Save all valid medicines
        if (!medicinesToSave.isEmpty()) {
            List<Medicine> savedMedicines = medicineRepository.saveAll(medicinesToSave);
            response.setSuccessfulCount(savedMedicines.size());
        }

        response.setFailedCount(errors.size());
        response.setErrors(errors);

        return response;
    }

    /**
     * Parse a CSV line into a Medicine object.
     * Expected CSV format: name,genericName,brandName,description,category,subCategory,requiresPrescription
     *
     * @param line CSV line to parse
     * @param rowNumber Row number for error reporting
     * @return Medicine object
     * @throws IllegalArgumentException if the line is invalid
     */
    Medicine parseCsvLine(String line, int rowNumber) {
        String[] values = line.split(",", -1); // -1 to preserve empty trailing values

        if (values.length < 7) {
            throw new IllegalArgumentException("Invalid CSV format: expected 7 columns, found " + values.length);
        }

        // Trim values
        String name = values[0].trim();
        String genericName = values[1].trim();
        String brandName = values[2].trim();
        String description = values[3].trim();
        String category = values[4].trim();
        String subCategory = values[5].trim();
        String requiresPrescriptionStr = values[6].trim();

        // Validate required fields
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Medicine name is required");
        }
        if (genericName.isEmpty()) {
            throw new IllegalArgumentException("Generic name is required");
        }
        if (category.isEmpty()) {
            throw new IllegalArgumentException("Category is required");
        }

        // Check if medicine already exists
        if (medicineRepository.findByName(name).isPresent()) {
            throw new IllegalArgumentException("Medicine with name '" + name + "' already exists");
        }

        // Parse requiresPrescription
        Boolean requiresPrescription;
        if (requiresPrescriptionStr.equalsIgnoreCase("true") ||
            requiresPrescriptionStr.equalsIgnoreCase("yes") ||
            requiresPrescriptionStr.equals("1")) {
            requiresPrescription = true;
        } else if (requiresPrescriptionStr.equalsIgnoreCase("false") ||
                   requiresPrescriptionStr.equalsIgnoreCase("no") ||
                   requiresPrescriptionStr.equals("0")) {
            requiresPrescription = false;
        } else {
            throw new IllegalArgumentException("Invalid value for requiresPrescription: '" + requiresPrescriptionStr +
                                             "'. Expected true/false, yes/no, or 1/0");
        }

        return Medicine.builder()
                .name(name)
                .genericName(genericName)
                .brandName(brandName.isEmpty() ? null : brandName)
                .description(description.isEmpty() ? null : description)
                .category(category)
                .subCategory(subCategory.isEmpty() ? null : subCategory)
                .requiresPrescription(requiresPrescription)
                .isActive(true) // Default to active for uploaded medicines
                .build();
    }

    /**
     * Response object for CSV upload operation.
     */
    public static class MedicineCsvUploadResponse {
        private int successfulCount;
        private int failedCount;
        private List<MedicineCsvUploadError> errors;

        public int getSuccessfulCount() {
            return successfulCount;
        }

        public void setSuccessfulCount(int successfulCount) {
            this.successfulCount = successfulCount;
        }

        public int getFailedCount() {
            return failedCount;
        }

        public void setFailedCount(int failedCount) {
            this.failedCount = failedCount;
        }

        public List<MedicineCsvUploadError> getErrors() {
            return errors;
        }

        public void setErrors(List<MedicineCsvUploadError> errors) {
            this.errors = errors;
        }
    }

    /**
     * Error object for CSV upload operation.
     */
    public static class MedicineCsvUploadError {
        private int rowNumber;
        private String message;

        public MedicineCsvUploadError(int rowNumber, String message) {
            this.rowNumber = rowNumber;
            this.message = message;
        }

        public int getRowNumber() {
            return rowNumber;
        }

        public void setRowNumber(int rowNumber) {
            this.rowNumber = rowNumber;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}