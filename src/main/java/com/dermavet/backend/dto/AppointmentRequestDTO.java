package com.dermavet.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

// Polja su nazvana isto kao ključevi koje forma na sajtu već šalje (ime, telefon, email...)
// tako da na frontendu ne treba menjati imena, samo URL na koji se šalje zahtev.
@Schema(description = "Podaci potrebni za zakazivanje termina")
public class AppointmentRequestDTO {

    @Schema(description = "Ime i prezime klijenta", example = "Marko Marković")
    @NotBlank(message = "Ime i prezime su obavezni")
    private String ime;

    @Schema(description = "Kontakt telefon", example = "0641234567")
    @NotBlank(message = "Telefon je obavezan")
    private String telefon;

    @Schema(description = "Email adresa - na nju stiže potvrda termina", example = "marko@primer.com")
    @NotBlank(message = "Email je obavezan")
    @Email(message = "Email nije validan")
    private String email;

    @Schema(description = "Ime ljubimca", example = "Reks")
    @NotBlank(message = "Ime ljubimca je obavezno")
    private String ljubimac;

    @Schema(description = "Vrsta ljubimca", example = "Pas")
    private String vrsta;

    @Schema(description = "Razlog dolaska / tražena usluga", example = "Opšti pregled ljubimca")
    @NotBlank(message = "Razlog dolaska je obavezan")
    private String usluga;

    @Schema(description = "ID izabranog veterinara", example = "1")
    @NotNull(message = "Veterinar je obavezan")
    private Long veterinarId;

    @Schema(description = "Željeni datum termina (mora biti danas ili u budućnosti)", example = "2026-09-25")
    @NotNull(message = "Datum je obavezan")
    private LocalDate datum;

    @Schema(description = "Željeno vreme termina - mora biti jedan od slobodnih termina za dati datum", example = "11:00")
    @NotNull(message = "Vreme je obavezno")
    private LocalTime vreme;

    @Schema(description = "Dodatna napomena, opcionalno", example = "Ljubimac je jako uplašen na pregledima.")
    private String napomena;

    public String getIme() {
        return ime;
    }

    public void setIme(String ime) {
        this.ime = ime;
    }

    public String getTelefon() {
        return telefon;
    }

    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLjubimac() {
        return ljubimac;
    }

    public void setLjubimac(String ljubimac) {
        this.ljubimac = ljubimac;
    }

    public String getVrsta() {
        return vrsta;
    }

    public void setVrsta(String vrsta) {
        this.vrsta = vrsta;
    }

    public String getUsluga() {
        return usluga;
    }

    public void setUsluga(String usluga) {
        this.usluga = usluga;
    }

    public Long getVeterinarId() {
        return veterinarId;
    }

    public void setVeterinarId(Long veterinarId) {
        this.veterinarId = veterinarId;
    }

    public LocalDate getDatum() {
        return datum;
    }

    public void setDatum(LocalDate datum) {
        this.datum = datum;
    }

    public LocalTime getVreme() {
        return vreme;
    }

    public void setVreme(LocalTime vreme) {
        this.vreme = vreme;
    }

    public String getNapomena() {
        return napomena;
    }

    public void setNapomena(String napomena) {
        this.napomena = napomena;
    }
}
