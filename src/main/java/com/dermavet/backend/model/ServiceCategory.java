package com.dermavet.backend.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "service_categories")
public class ServiceCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String naziv;

    @OneToMany(mappedBy = "kategorija", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<ClinicService> usluge = new ArrayList<>();

    public ServiceCategory() {
    }

    public ServiceCategory(String naziv) {
        this.naziv = naziv;
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

    public List<ClinicService> getUsluge() {
        return usluge;
    }

    public void setUsluge(List<ClinicService> usluge) {
        this.usluge = usluge;
    }
}
