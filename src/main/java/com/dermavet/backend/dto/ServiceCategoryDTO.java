package com.dermavet.backend.dto;

import com.dermavet.backend.model.ServiceCategory;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Kategorija usluga (npr. Dermatologija) sa listom usluga koje sadrži")
public class ServiceCategoryDTO {

    @Schema(description = "ID kategorije", example = "1")
    private Long id;

    @Schema(description = "Naziv kategorije", example = "Dermatologija")
    private String naziv;

    private List<ClinicServiceDTO> usluge;

    public ServiceCategoryDTO(ServiceCategory kategorija) {
        this.id = kategorija.getId();
        this.naziv = kategorija.getNaziv();
        this.usluge = kategorija.getUsluge().stream().map(ClinicServiceDTO::new).toList();
    }

    public Long getId() {
        return id;
    }

    public String getNaziv() {
        return naziv;
    }

    public List<ClinicServiceDTO> getUsluge() {
        return usluge;
    }
}
