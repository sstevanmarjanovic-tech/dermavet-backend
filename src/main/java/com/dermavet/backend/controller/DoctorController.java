package com.dermavet.backend.controller;

import com.dermavet.backend.dto.DoctorDTO;
import com.dermavet.backend.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Lekari", description = "Pregled tima ambulante")
@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService service;

    public DoctorController(DoctorService service) {
        this.service = service;
    }

    @Operation(summary = "Lista svih lekara ambulante")
    @GetMapping
    public List<DoctorDTO> sviLekari() {
        return service.getSviLekari();
    }
}
