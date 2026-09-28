package com.dermavet.backend.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "doctors")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String imePrezime;

    @Column(nullable = false)
    private String uloga;      // npr. "Specijalista dermatolog"

    private String specijalnost; // npr. "Dermatolog, Imunoterapija"

    @Column(length = 1000)
    private String bio;

    private int godinaIskustva;

    // Dani u nedelji kad lekar radi, npr. "PON,UTO,SRE,CET,PET" - ilustrativni raspored,
    // koristi se da forma za zakazivanje ne nudi lekara na dan kad ne radi.
    @Column(nullable = false)
    private String radniDani;

    // Google Calendar colorId (1-11) - koristi se da Make.com oboji termin ovog lekara
    // drugom bojom u kalendaru. Interno polje, ne izlazi kroz javni API.
    private String googleCalendarBoja;

    @ManyToMany
    @OrderBy("id ASC")
    @JoinTable(
            name = "doctor_usluge",
            joinColumns = @JoinColumn(name = "doctor_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id"))
    private List<ClinicService> usluge = new ArrayList<>();

    public Doctor() {
    }

    public Doctor(String imePrezime, String uloga, String specijalnost, String bio, int godinaIskustva,
                  String radniDani, String googleCalendarBoja) {
        this.imePrezime = imePrezime;
        this.uloga = uloga;
        this.specijalnost = specijalnost;
        this.bio = bio;
        this.godinaIskustva = godinaIskustva;
        this.radniDani = radniDani;
        this.googleCalendarBoja = googleCalendarBoja;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImePrezime() {
        return imePrezime;
    }

    public void setImePrezime(String imePrezime) {
        this.imePrezime = imePrezime;
    }

    public String getUloga() {
        return uloga;
    }

    public void setUloga(String uloga) {
        this.uloga = uloga;
    }

    public String getSpecijalnost() {
        return specijalnost;
    }

    public void setSpecijalnost(String specijalnost) {
        this.specijalnost = specijalnost;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public int getGodinaIskustva() {
        return godinaIskustva;
    }

    public void setGodinaIskustva(int godinaIskustva) {
        this.godinaIskustva = godinaIskustva;
    }

    public String getRadniDani() {
        return radniDani;
    }

    public void setRadniDani(String radniDani) {
        this.radniDani = radniDani;
    }

    public String getGoogleCalendarBoja() {
        return googleCalendarBoja;
    }

    public void setGoogleCalendarBoja(String googleCalendarBoja) {
        this.googleCalendarBoja = googleCalendarBoja;
    }

    public List<ClinicService> getUsluge() {
        return usluge;
    }

    public void setUsluge(List<ClinicService> usluge) {
        this.usluge = usluge;
    }
}
