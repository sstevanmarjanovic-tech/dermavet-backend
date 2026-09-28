package com.dermavet.backend.controller;

import com.dermavet.backend.dto.ServiceCategoryDTO;
import com.dermavet.backend.service.ClinicServiceCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Usluge", description = "Katalog usluga ambulante, grupisan po kategorijama")
@RestController
@RequestMapping("/api/services")
public class ClinicServiceController {

    private final ClinicServiceCatalogService catalogService;

    public ClinicServiceController(ClinicServiceCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @Operation(summary = "Lista svih kategorija usluga sa uslugama unutar svake kategorije")
    @GetMapping
    public List<ServiceCategoryDTO> sveUsluge() {
        return catalogService.getSveKategorijeSaUslugama();
    }
}
