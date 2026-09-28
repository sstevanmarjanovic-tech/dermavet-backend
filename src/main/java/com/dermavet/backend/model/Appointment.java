package com.dermavet.backend.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ime;

    @Column(nullable = false)
    private String telefon;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String ljubimac;

    // Slobodan tekst iz <select id="fsp"> (Pas, Mačka, Zec...) - ne mora enum
    private String vrsta;

    @Column(nullable = false)
    private String usluga;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veterinar_id", nullable = false)
    private Doctor veterinar;

    @Column(nullable = false)
    private LocalDate datum;

    @Column(nullable = false)
    private LocalTime vreme;

    @Column(length = 1000)
    private String napomena;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus status = AppointmentStatus.NA_CEKANJU;

    private LocalDateTime kreiran = LocalDateTime.now();

    public Appointment() {
    }

    // --- getteri i setteri ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Doctor getVeterinar() {
        return veterinar;
    }

    public void setVeterinar(Doctor veterinar) {
        this.veterinar = veterinar;
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

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public LocalDateTime getKreiran() {
        return kreiran;
    }

    public void setKreiran(LocalDateTime kreiran) {
        this.kreiran = kreiran;
    }
}
