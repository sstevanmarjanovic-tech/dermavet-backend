package com.dermavet.backend.service;

import com.dermavet.backend.model.Appointment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class CalendarNotificationService {

    private static final Logger log = LoggerFactory.getLogger(CalendarNotificationService.class);

    private final RestTemplate restTemplate;
    private final String webhookUrl;

    public CalendarNotificationService(RestTemplate restTemplate,
                                        @Value("${dermavet.make-webhook-url}") String webhookUrl) {
        this.restTemplate = restTemplate;
        this.webhookUrl = webhookUrl;
    }

    // Isti oblik podataka koji je ranije slao frontend direktno na Make.com,
    // samo sto sad to radi backend, POSLE sto je termin vec upisan u bazu.
    // Dodato: ime veterinara i "colorId" (Google Calendar boja 1-11) - u Make.com scenariju
    // treba mapirati "colorId" polje na Color parametar Google Calendar modula, da bi
    // termini razlicitih veterinara imali razlicitu boju u kalendaru.
    public void posaljiUKalendar(Appointment a) {
        try {
            Map<String, Object> payload = Map.ofEntries(
                    Map.entry("ime", a.getIme()),
                    Map.entry("telefon", a.getTelefon()),
                    Map.entry("email", a.getEmail()),
                    Map.entry("ljubimac", a.getLjubimac()),
                    Map.entry("vrsta", a.getVrsta() == null ? "" : a.getVrsta()),
                    Map.entry("usluga", a.getUsluga()),
                    Map.entry("veterinar", a.getVeterinar().getImePrezime()),
                    Map.entry("colorId", a.getVeterinar().getGoogleCalendarBoja() == null ? "" : a.getVeterinar().getGoogleCalendarBoja()),
                    Map.entry("datum", a.getDatum().toString()),
                    Map.entry("vreme", a.getVreme().toString().substring(0, 5)),
                    Map.entry("napomena", a.getNapomena() == null ? "" : a.getNapomena())
            );
            restTemplate.postForEntity(webhookUrl, payload, String.class);
        } catch (Exception e) {
            // Termin je vec siguran u bazi - ako kalendar padne, ne rusimo ceo zahtev,
            // samo belezimo gresku da bi se moglo naknadno proveriti/rucno dodati u kalendar.
            log.error("Neuspesno slanje termina (id={}) ka Google kalendaru: {}", a.getId(), e.getMessage());
        }
    }
}
