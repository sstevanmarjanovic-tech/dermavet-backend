package com.dermavet.backend.repository;

import com.dermavet.backend.model.Appointment;
import com.dermavet.backend.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Slotovi se sad proveravaju PO LEKARU - dva razlicita lekara mogu imati
    // pacijenta u isto vreme, samo jedan te isti lekar ne moze na dva mesta odjednom.
    List<Appointment> findByDatumAndVeterinar(LocalDate datum, Doctor veterinar);

    Optional<Appointment> findByDatumAndVremeAndVeterinar(LocalDate datum, LocalTime vreme, Doctor veterinar);
}
