package com.dermavet.backend.dto;

import com.dermavet.backend.model.ClinicService;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Pojedinačna usluga u okviru jedne kategorije")
public class ClinicServiceDTO {

    @Schema(description = "ID usluge", example = "1")
    private Long id;

    @Schema(description = "Naziv usluge", example = "Alergo test")
    private String naziv;

    @Schema(description = "Emoji ikonica (može biti null)", example = "🧬")
    private String ikona;

    @Schema(description = "Kratak opis usluge", example = "Intradermalni i iz krvi - jedina metoda koja otkriva uzrok alergija.")
    private String opis;

    @Schema(description = "Detaljan opis usluge, može biti null")
    private String detaljanOpis;

    public ClinicServiceDTO(ClinicService s) {
        this.id = s.getId();
        this.naziv = s.getNaziv();
        this.ikona = s.getIkona();
        this.opis = s.getOpis();
        this.detaljanOpis = s.getDetaljanOpis();
    }

    public Long getId() {
        return id;
    }

    public String getNaziv() {
        return naziv;
    }

    public String getIkona() {
        return ikona;
    }

    public String getOpis() {
        return opis;
    }

    public String getDetaljanOpis() {
        return detaljanOpis;
    }
}
