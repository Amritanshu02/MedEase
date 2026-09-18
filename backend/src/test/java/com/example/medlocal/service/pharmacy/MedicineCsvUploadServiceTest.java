package com.example.medlocal.service.pharmacy;

import com.example.medlocal.model.pharmacy.Medicine;
import com.example.medlocal.repository.pharmacy.MedicineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MedicineCsvUploadServiceTest {

    @Mock
    private MedicineRepository medicineRepository;

    @InjectMocks
    private MedicineCsvUploadService medicineCsvUploadService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testParseCsvLine_valid() throws Exception {
        String line = "Paracetamol,Acetaminophen,Tylenol,Pain reliever and fever reducer,Analgesic,,false";
        Medicine medicine = medicineCsvUploadService.parseCsvLine(line, 1);
        assertNotNull(medicine);
        assertEquals("Paracetamol", medicine.getName());
        assertEquals("Acetaminophen", medicine.getGenericName());
        assertEquals("Tylenol", medicine.getBrandName());
        assertEquals("Pain reliever and fever reducer", medicine.getDescription());
        assertEquals("Analgesic", medicine.getCategory());
        assertNull(medicine.getSubCategory());
        assertFalse(medicine.getRequiresPrescription());
        assertTrue(medicine.getIsActive());
    }

    @Test
    void testParseCsvLine_missingRequiredField() {
        String line = ",Acetaminophen,Tylenol,Pain reliever and fever reducer,Analgesic,,false"; // missing name
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> medicineCsvUploadService.parseCsvLine(line, 1));
        assertTrue(exception.getMessage().contains("Medicine name is required"));
    }

    @Test
    void testProcessCsvUpload_success() throws IOException {
        // Mock repository to return empty (no existing medicine)
        when(medicineRepository.findByName(anyString())).thenReturn(java.util.Optional.empty());
        when(medicineRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<Medicine> medicines = invocation.getArgument(0);
            // Simulate saved medicines by setting IDs
            for (int i = 0; i < medicines.size(); i++) {
                medicines.get(i).setId((long) (i + 1));
            }
            return medicines;
        });

        // Create a mock MultipartFile with CSV content
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test.csv");
        String csvContent = "name,genericName,brandName,description,category,subCategory,requiresPrescription\n" +
                "Paracetamol,Acetaminophen,Tylenol,Pain reliever and fever reducer,Analgesic,,false\n" +
                "Amoxicillin,Amoxicillin,Amoxil,Antibiotic for bacterial infections,Antibiotic,,true\n";
        when(file.getInputStream()).thenReturn(new java.io.ByteArrayInputStream(csvContent.getBytes()));

        MedicineCsvUploadService.MedicineCsvUploadResponse response = medicineCsvUploadService.processCsvUpload(file);

        assertEquals(2, response.getSuccessfulCount());
        assertEquals(0, response.getFailedCount());
        assertTrue(response.getErrors().isEmpty());

        verify(medicineRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testProcessCsvUpload_duplicateName() throws IOException {
        // Mock repository to return existing medicine for second row
        when(medicineRepository.findByName(eq("Paracetamol"))).thenReturn(java.util.Optional.of(new Medicine()));
        when(medicineRepository.findByName(eq("Amoxicillin"))).thenReturn(java.util.Optional.empty());

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test.csv");
        String csvContent = "name,genericName,brandName,description,category,subCategory,requiresPrescription\n" +
                "Paracetamol,Acetaminophen,Tylenol,Pain reliever and fever reducer,Analgesic,,false\n" +
                "Amoxicillin,Amoxicillin,Amoxil,Antibiotic for bacterial infections,Antibiotic,,true\n";
        when(file.getInputStream()).thenReturn(new java.io.ByteArrayInputStream(csvContent.getBytes()));

        MedicineCsvUploadService.MedicineCsvUploadResponse response = medicineCsvUploadService.processCsvUpload(file);

        assertEquals(1, response.getSuccessfulCount()); // Amoxicillin only
        assertEquals(1, response.getFailedCount()); // Paracetamol duplicate
        assertEquals(1, response.getErrors().size());
        assertEquals(1, response.getErrors().get(0).getRowNumber());
        assertTrue(response.getErrors().get(0).getMessage().contains("already exists"));

        verify(medicineRepository, times(2)).findByName(anyString());
        verify(medicineRepository, times(1)).saveAll(anyList()); // only one saved
    }
}