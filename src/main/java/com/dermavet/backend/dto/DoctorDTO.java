package com.dermavet.backend.dto;

import com.dermavet.backend.model.Doctor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Lekar - podaci za prikaz na sajtu i za formu za zakazivanje")
public class DoctorDTO {

    @Schema(description = "ID lekara", example = "1")
    private Long id;

    private String imePrezime;
    private String uloga;
    private String specijalnost;
    private String bio;
    private int godinaIskustva;

    @Schema(description = "Dani u nedelji kad lekar radi (skraćenice odvojene zarezom)", example = "PON,UTO,SRE,CET,PET")
    private String radniDani;

    @Schema(description = "Usluge koje ovaj lekar može da obavi - koristi ih forma za zakazivanje da filtrira izbor usluge")
    private List<ClinicServiceDTO> usluge;

    public DoctorDTO(Doctor d) {
        this.id = d.getId();
        this.imePrezime = d.getImePrezime();
        this.uloga = d.getUloga();
        this.specijalnost = d.getSpecijalnost();
        this.bio = d.getBio();
        this.godinaIskustva = d.getGodinaIskustva();
        this.radniDani = d.getRadniDani();
        this.usluge = d.getUsluge().stream().map(ClinicServiceDTO::new).toList();
    }

    public Long getId() {
        return id;
    }

    public String getImePrezime() {
        return imePrezime;
    }

    public String getUloga() {
        return uloga;
    }

    public String getSpecijalnost() {
        return specijalnost;
    }

    public String getBio() {
        return bio;
    }

    public int getGodinaIskustva() {
        return godinaIskustva;
    }

    public String getRadniDani() {
        return radniDani;
    }

    public List<ClinicServiceDTO> getUsluge() {
        return usluge;
    }
}
