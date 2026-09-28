package com.dermavet.backend.service;

import com.dermavet.backend.model.Appointment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final DateTimeFormatter DATUM_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy.");

    private final JavaMailSender mailSender;
    private final String adminEmail;

    public EmailService(JavaMailSender mailSender, @Value("${dermavet.admin-email}") String adminEmail) {
        this.mailSender = mailSender;
        this.adminEmail = adminEmail;
    }

    // Mejl vlasniku ambulante - ko je zakazao i svi podaci
    public void posaljiAdminObavestenje(Appointment a) {
        try {
            SimpleMailMessage poruka = new SimpleMailMessage();
            poruka.setTo(adminEmail);
            poruka.setSubject("Novi zahtev za termin - " + a.getIme() + " (" + a.getDatum().format(DATUM_FORMAT) + " u " + a.getVreme() + ")");
            poruka.setText(String.format("""
                    Stigao je novi zahtev za termin preko sajta.

                    Ime i prezime: %s
                    Telefon: %s
                    Email: %s
                    Ljubimac: %s (%s)
                    Razlog dolaska: %s
                    Veterinar: %s
                    Datum: %s
                    Vreme: %s
                    Napomena: %s
                    """,
                    a.getIme(), a.getTelefon(), a.getEmail(),
                    a.getLjubimac(), a.getVrsta() == null || a.getVrsta().isBlank() ? "nije navedeno" : a.getVrsta(),
                    a.getUsluga(), a.getVeterinar().getImePrezime(), a.getDatum().format(DATUM_FORMAT), a.getVreme(),
                    a.getNapomena() == null || a.getNapomena().isBlank() ? "-" : a.getNapomena()));
            mailSender.send(poruka);
        } catch (Exception e) {
            log.error("Neuspesno slanje admin mejla za termin id={}: {}", a.getId(), e.getMessage());
        }
    }

    // Mejl klijentu - potvrda da je zahtev primljen
    public void posaljiPotvrduKlijentu(Appointment a) {
        try {
            SimpleMailMessage poruka = new SimpleMailMessage();
            poruka.setTo(a.getEmail());
            poruka.setSubject("Potvrda zahteva za termin - DermaVet");
            poruka.setText(String.format("""
                    Poštovani/a %s,

                    Hvala Vam što ste zakazali termin u DermaVet ambulanti.

                    Detalji zahteva:
                    Ljubimac: %s
                    Razlog dolaska: %s
                    Veterinar: %s
                    Datum: %s
                    Vreme: %s

                    Naše osoblje će pregledati Vaš zahtev i po potrebi Vas kontaktirati radi potvrde termina.

                    ---
                    Ovo je automatska poruka, molimo Vas da na nju ne odgovarate.
                    Ako imate pitanja, pišite nam na %s.

                    DermaVet - Veterinarski dermatološki centar
                    Vranjska 2, Beograd
                    """,
                    a.getIme(), a.getLjubimac(), a.getUsluga(), a.getVeterinar().getImePrezime(),
                    a.getDatum().format(DATUM_FORMAT), a.getVreme(), adminEmail));
            mailSender.send(poruka);
        } catch (Exception e) {
            log.error("Neuspesno slanje potvrde klijentu za termin id={}: {}", a.getId(), e.getMessage());
        }
    }
}
