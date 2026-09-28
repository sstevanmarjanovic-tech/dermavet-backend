package com.dermavet.backend.model;

import jakarta.persistence.*;

// Zove se ClinicService (ne "Service") da se ne poklopi imenom sa @Service anotacijom iz Springa.
@Entity
@Table(name = "clinic_services")
public class ClinicService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String naziv;

    // Emoji ikonica prikazana uz uslugu (kao na sajtu) - moze biti null za usluge koje na sajtu
    // nemaju svoju karticu (npr. stavke iz liste kod Hirurgije/Interne/Stomatologije).
    private String ikona;

    @Column(length = 500)
    private String opis;

    // Detaljan opis - odgovara "zadnjoj strani" kartice na sajtu (sub-back). Moze biti null.
    @Column(length = 2000)
    private String detaljanOpis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private ServiceCategory kategorija;

    public ClinicService() {
    }

    public ClinicService(String naziv, String ikona, String opis, String detaljanOpis, ServiceCategory kategorija) {
        this.naziv = naziv;
        this.ikona = ikona;
        this.opis = opis;
        this.detaljanOpis = detaljanOpis;
        this.kategorija = kategorija;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public String getIkona() {
        return ikona;
    }

    public void setIkona(String ikona) {
        this.ikona = ikona;
    }

    public String getOpis() {
        return opis;
    }

    public void setOpis(String opis) {
        this.opis = opis;
    }

    public String getDetaljanOpis() {
        return detaljanOpis;
    }

    public void setDetaljanOpis(String detaljanOpis) {
        this.detaljanOpis = detaljanOpis;
    }

    public ServiceCategory getKategorija() {
        return kategorija;
    }

    public void setKategorija(ServiceCategory kategorija) {
        this.kategorija = kategorija;
    }
}
