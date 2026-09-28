package com.dermavet.backend.service;

import com.dermavet.backend.dto.ServiceCategoryDTO;
import com.dermavet.backend.repository.ServiceCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClinicServiceCatalogService {

    private final ServiceCategoryRepository categoryRepository;

    public ClinicServiceCatalogService(ServiceCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // readOnly = true jer samo citamo podatke - Hibernate moze da optimizuje transakciju
    @Transactional(readOnly = true)
    public List<ServiceCategoryDTO> getSveKategorijeSaUslugama() {
        return categoryRepository.findAll().stream()
                .map(ServiceCategoryDTO::new)
                .toList();
    }
}
