package com.dermavet.backend.controller;

import com.dermavet.backend.dto.AppointmentRequestDTO;
import com.dermavet.backend.dto.AppointmentResponseDTO;
import com.dermavet.backend.model.AppointmentStatus;
import com.dermavet.backend.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Tag(name = "Termini", description = "Zakazivanje i administracija termina")
@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService service;

    public AppointmentController(AppointmentService service) {
        this.service = service;
    }

    @Operation(summary = "Zakazivanje novog termina",
            description = "Poziva ga forma sa sajta. Proverava radno vreme ambulante i da termin nije već zauzet, " +
                    "upisuje ga u bazu, i prosleđuje ga u Google kalendar i mejlom (klijentu i vlasniku ambulante).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Termin uspešno zakazan"),
            @ApiResponse(responseCode = "400", description = "Neispravni podaci, termin van radnog vremena, ili termin je već zauzet")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentResponseDTO zakaziTermin(@Valid @RequestBody AppointmentRequestDTO dto) {
        return service.zakaziTermin(dto);
    }

    @Operation(summary = "Slobodni termini za dati datum i veterinara",
            description = "Vraća listu stvarno slobodnih termina za IZABRANOG veterinara (radno vreme minus već " +
                    "zauzeti termini tog veterinara). Vraća praznu listu za nedelju ili dan kad taj veterinar ne radi.")
    @ApiResponse(responseCode = "200", description = "Lista slobodnih termina (može biti prazna)")
    @GetMapping("/slobodni-termini")
    public List<LocalTime> slobodniTermini(
            @Parameter(description = "Datum za koji se proveravaju slobodni termini", example = "2026-09-25")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate datum,
            @Parameter(description = "ID veterinara", example = "1")
            @RequestParam Long veterinarId) {
        return service.getSlobodniTermini(datum, veterinarId);
    }

    @Operation(summary = "Svi zahtevi za termine (admin)", description = "Zaštićeno - zahteva admin prijavu.")
    @SecurityRequirement(name = "basicAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista svih termina"),
            @ApiResponse(responseCode = "401", description = "Nije prijavljen ili pogrešni kredencijali")
    })
    @GetMapping
    public List<AppointmentResponseDTO> sviTermini() {
        return service.getSviTermini();
    }

    @Operation(summary = "Promena statusa termina (admin)", description = "Zaštićeno - zahteva admin prijavu.")
    @SecurityRequirement(name = "basicAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status uspešno promenjen"),
            @ApiResponse(responseCode = "401", description = "Nije prijavljen ili pogrešni kredencijali"),
            @ApiResponse(responseCode = "404", description = "Termin sa datim ID-jem ne postoji")
    })
    @PatchMapping("/{id}/status")
    public AppointmentResponseDTO promeniStatus(
            @Parameter(description = "ID termina") @PathVariable Long id,
            @Parameter(description = "Novi status") @RequestParam AppointmentStatus status) {
        return service.promeniStatus(id, status);
    }

    @Operation(summary = "Brisanje termina (admin)", description = "Zaštićeno - zahteva admin prijavu.")
    @SecurityRequirement(name = "basicAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Termin uspešno obrisan"),
            @ApiResponse(responseCode = "401", description = "Nije prijavljen ili pogrešni kredencijali"),
            @ApiResponse(responseCode = "404", description = "Termin sa datim ID-jem ne postoji")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void obrisi(@Parameter(description = "ID termina") @PathVariable Long id) {
        service.obrisiTermin(id);
    }
}
