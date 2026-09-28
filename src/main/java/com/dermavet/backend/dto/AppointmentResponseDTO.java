package com.dermavet.backend.dto;

import com.dermavet.backend.model.Appointment;
import com.dermavet.backend.model.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public class AppointmentResponseDTO {

    private Long id;
    private String ime;
    private String ljubimac;
    private String usluga;
    private String veterinar;
    private LocalDate datum;
    private LocalTime vreme;
    private AppointmentStatus status;

    public AppointmentResponseDTO(Appointment a) {
        this.id = a.getId();
        this.ime = a.getIme();
        this.ljubimac = a.getLjubimac();
        this.usluga = a.getUsluga();
        this.veterinar = a.getVeterinar() != null ? a.getVeterinar().getImePrezime() : null;
        this.datum = a.getDatum();
        this.vreme = a.getVreme();
        this.status = a.getStatus();
    }

    public Long getId() {
        return id;
    }

    public String getIme() {
        return ime;
    }

    public String getLjubimac() {
        return ljubimac;
    }

    public String getUsluga() {
        return usluga;
    }

    public String getVeterinar() {
        return veterinar;
    }

    public LocalDate getDatum() {
        return datum;
    }

    public LocalTime getVreme() {
        return vreme;
    }

    public AppointmentStatus getStatus() {
        return status;
    }
}
