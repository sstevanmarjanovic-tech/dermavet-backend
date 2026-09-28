package com.dermavet.backend.service;

import com.dermavet.backend.dto.AppointmentRequestDTO;
import com.dermavet.backend.dto.AppointmentResponseDTO;
import com.dermavet.backend.exception.InvalidAppointmentException;
import com.dermavet.backend.exception.ResourceNotFoundException;
import com.dermavet.backend.model.Appointment;
import com.dermavet.backend.model.AppointmentStatus;
import com.dermavet.backend.model.Doctor;
import com.dermavet.backend.repository.AppointmentRepository;
import com.dermavet.backend.repository.DoctorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    // Isti termini kao u JS-u na sajtu (populateSlots) - radno vreme ambulante.
    private static final List<LocalTime> SLOTOVI_RADNIM_DANOM = List.of(
            LocalTime.of(11, 0), LocalTime.of(11, 30), LocalTime.of(12, 0), LocalTime.of(12, 30),
            LocalTime.of(13, 0), LocalTime.of(13, 30), LocalTime.of(14, 0), LocalTime.of(14, 30),
            LocalTime.of(15, 0), LocalTime.of(15, 30), LocalTime.of(16, 0), LocalTime.of(16, 30),
            LocalTime.of(17, 0), LocalTime.of(17, 30));

    private static final List<LocalTime> SLOTOVI_SUBOTOM = List.of(
            LocalTime.of(10, 0), LocalTime.of(10, 30), LocalTime.of(11, 0),
            LocalTime.of(11, 30), LocalTime.of(12, 0), LocalTime.of(12, 30));

    // Skracenice dana koje koristi Doctor.radniDani (npr. "PON,UTO,SRE,CET,PET")
    private static final Map<DayOfWeek, String> SKRACENICA_DANA = new EnumMap<>(Map.of(
            DayOfWeek.MONDAY, "PON", DayOfWeek.TUESDAY, "UTO", DayOfWeek.WEDNESDAY, "SRE",
            DayOfWeek.THURSDAY, "CET", DayOfWeek.FRIDAY, "PET", DayOfWeek.SATURDAY, "SUB",
            DayOfWeek.SUNDAY, "NED"));

    private final AppointmentRepository repository;
    private final DoctorRepository doctorRepository;
    private final CalendarNotificationService calendarNotificationService;
    private final EmailService emailService;

    public AppointmentService(AppointmentRepository repository,
                               DoctorRepository doctorRepository,
                               CalendarNotificationService calendarNotificationService,
                               EmailService emailService) {
        this.repository = repository;
        this.doctorRepository = doctorRepository;
        this.calendarNotificationService = calendarNotificationService;
        this.emailService = emailService;
    }

    private List<LocalTime> slotoviZaDan(LocalDate datum) {
        DayOfWeek dan = datum.getDayOfWeek();
        if (dan == DayOfWeek.SUNDAY) {
            return List.of();
        }
        return dan == DayOfWeek.SATURDAY ? SLOTOVI_SUBOTOM : SLOTOVI_RADNIM_DANOM;
    }

    // Da li dati lekar uopste radi na taj dan u nedelji (na osnovu Doctor.radniDani)
    private boolean radiTogDana(Doctor veterinar, LocalDate datum) {
        String skracenica = SKRACENICA_DANA.get(datum.getDayOfWeek());
        String radniDani = veterinar.getRadniDani();
        if (radniDani == null || radniDani.isBlank()) {
            return true; // ako nije uneto, ne ogranicavamo
        }
        return List.of(radniDani.split(",")).stream().map(String::trim).anyMatch(skracenica::equals);
    }

    private Doctor pronadjiVeterinara(Long veterinarId) {
        return doctorRepository.findById(veterinarId)
                .orElseThrow(() -> new InvalidAppointmentException("Izabrani veterinar ne postoji."));
    }

    @Transactional(readOnly = true)
    public List<LocalTime> getSlobodniTermini(LocalDate datum, Long veterinarId) {
        List<LocalTime> sviSlotovi = slotoviZaDan(datum);
        if (sviSlotovi.isEmpty()) {
            return sviSlotovi; // nedelja - nema termina
        }
        Doctor veterinar = pronadjiVeterinara(veterinarId);
        if (!radiTogDana(veterinar, datum)) {
            return List.of(); // izabrani lekar tog dana ne radi
        }

        List<LocalTime> zauzeti = repository.findByDatumAndVeterinar(datum, veterinar).stream()
                .map(Appointment::getVreme)
                .toList();

        List<LocalTime> slobodni = new ArrayList<>(sviSlotovi);
        slobodni.removeAll(zauzeti);
        return slobodni;
    }

    @Transactional
    public AppointmentResponseDTO zakaziTermin(AppointmentRequestDTO dto) {
        if (dto.getDatum().isBefore(LocalDate.now())) {
            throw new InvalidAppointmentException("Ne možete zakazati termin u prošlosti.");
        }
        List<LocalTime> dozvoljeniSlotovi = slotoviZaDan(dto.getDatum());
        if (dozvoljeniSlotovi.isEmpty()) {
            throw new InvalidAppointmentException("Nedeljom ambulanta ne radi. Izaberite drugi dan.");
        }
        if (!dozvoljeniSlotovi.contains(dto.getVreme())) {
            throw new InvalidAppointmentException("Izabrano vreme nije u radnom vremenu ambulante za taj dan.");
        }

        Doctor veterinar = pronadjiVeterinara(dto.getVeterinarId());
        if (!radiTogDana(veterinar, dto.getDatum())) {
            throw new InvalidAppointmentException("Izabrani veterinar tog dana ne radi. Izaberite drugi dan ili drugog veterinara.");
        }

        boolean zauzet = repository.findByDatumAndVremeAndVeterinar(dto.getDatum(), dto.getVreme(), veterinar).isPresent();
        if (zauzet) {
            throw new InvalidAppointmentException("Izabrani veterinar je već zauzet u tom terminu, izaberite drugo vreme.");
        }

        Appointment a = new Appointment();
        a.setIme(dto.getIme());
        a.setTelefon(dto.getTelefon());
        a.setEmail(dto.getEmail());
        a.setLjubimac(dto.getLjubimac());
        a.setVrsta(dto.getVrsta());
        a.setUsluga(dto.getUsluga());
        a.setVeterinar(veterinar);
        a.setDatum(dto.getDatum());
        a.setVreme(dto.getVreme());
        a.setNapomena(dto.getNapomena());
        a.setStatus(AppointmentStatus.NA_CEKANJU);

        Appointment sacuvan = repository.save(a);

        // Termin je bezbedan u bazi - sad ga prosledjujemo u Google kalendar i saljemo mejlove.
        calendarNotificationService.posaljiUKalendar(sacuvan);
        emailService.posaljiAdminObavestenje(sacuvan);
        emailService.posaljiPotvrduKlijentu(sacuvan);

        return new AppointmentResponseDTO(sacuvan);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getSviTermini() {
        return repository.findAll().stream()
                .map(AppointmentResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public AppointmentResponseDTO promeniStatus(Long id, AppointmentStatus noviStatus) {
        Appointment a = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Termin sa id " + id + " ne postoji."));
        a.setStatus(noviStatus);
        return new AppointmentResponseDTO(repository.save(a));
    }

    @Transactional
    public void obrisiTermin(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Termin sa id " + id + " ne postoji.");
        }
        repository.deleteById(id);
    }
}
