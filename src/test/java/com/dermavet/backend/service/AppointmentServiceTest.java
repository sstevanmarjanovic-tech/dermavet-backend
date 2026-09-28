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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit testovi za AppointmentService.
 * Repozitorijumi, kalendar i mejl servis su mokovani - testira se samo poslovna logika
 * (radno vreme, raspored veterinara, provera duplog zakazivanja, promena statusa, brisanje),
 * bez prave baze.
 */
@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    private static final String SVI_DANI = "PON,UTO,SRE,CET,PET,SUB";

    @Mock
    private AppointmentRepository repository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private CalendarNotificationService calendarNotificationService;

    @Mock
    private EmailService emailService;

    private AppointmentService service;

    @BeforeEach
    void setUp() {
        service = new AppointmentService(repository, doctorRepository, calendarNotificationService, emailService);
    }

    // --- pomocne metode za testove ---

    private LocalDate nextDateOn(DayOfWeek dayOfWeek) {
        LocalDate date = LocalDate.now().plusDays(1);
        while (date.getDayOfWeek() != dayOfWeek) {
            date = date.plusDays(1);
        }
        return date;
    }

    private Doctor doctor(Long id, String radniDani) {
        Doctor d = new Doctor("Dr vet. Test Testović", "Veterinar", "Interna medicina",
                "Bio", 5, radniDani, "9");
        d.setId(id);
        return d;
    }

    private AppointmentRequestDTO validRequest(LocalDate datum, LocalTime vreme, Long veterinarId) {
        AppointmentRequestDTO dto = new AppointmentRequestDTO();
        dto.setIme("Marko Marković");
        dto.setTelefon("0641234567");
        dto.setEmail("marko@primer.com");
        dto.setLjubimac("Reks");
        dto.setVrsta("Pas");
        dto.setUsluga("Kompletan klinički pregled i anamneza");
        dto.setVeterinarId(veterinarId);
        dto.setDatum(datum);
        dto.setVreme(vreme);
        dto.setNapomena(null);
        return dto;
    }

    private Appointment savedAppointmentFrom(AppointmentRequestDTO dto, Doctor veterinar) {
        Appointment a = new Appointment();
        a.setId(1L);
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
        return a;
    }

    @Nested
    @DisplayName("zakaziTermin")
    class ZakaziTermin {

        @Test
        @DisplayName("uspešno zakazuje termin radnim danom kod izabranog veterinara")
        void savesAppointmentOnValidWeekday() {
            LocalDate ponedeljak = nextDateOn(DayOfWeek.MONDAY);
            LocalTime vreme = LocalTime.of(11, 0);
            Doctor veterinar = doctor(1L, SVI_DANI);
            AppointmentRequestDTO dto = validRequest(ponedeljak, vreme, 1L);
            Appointment sacuvan = savedAppointmentFrom(dto, veterinar);

            when(doctorRepository.findById(1L)).thenReturn(Optional.of(veterinar));
            when(repository.findByDatumAndVremeAndVeterinar(ponedeljak, vreme, veterinar)).thenReturn(Optional.empty());
            when(repository.save(any(Appointment.class))).thenReturn(sacuvan);

            AppointmentResponseDTO odgovor = service.zakaziTermin(dto);

            assertThat(odgovor.getId()).isEqualTo(1L);
            assertThat(odgovor.getStatus()).isEqualTo(AppointmentStatus.NA_CEKANJU);
            assertThat(odgovor.getVeterinar()).isEqualTo(veterinar.getImePrezime());

            ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
            verify(repository).save(captor.capture());
            assertThat(captor.getValue().getVeterinar()).isSameAs(veterinar);

            verify(calendarNotificationService).posaljiUKalendar(sacuvan);
            verify(emailService).posaljiAdminObavestenje(sacuvan);
            verify(emailService).posaljiPotvrduKlijentu(sacuvan);
        }

        @Test
        @DisplayName("prihvata termin subotom u skraćenom radnom vremenu")
        void savesAppointmentOnValidSaturdaySlot() {
            LocalDate subota = nextDateOn(DayOfWeek.SATURDAY);
            LocalTime vreme = LocalTime.of(10, 30);
            Doctor veterinar = doctor(1L, SVI_DANI);
            AppointmentRequestDTO dto = validRequest(subota, vreme, 1L);
            Appointment sacuvan = savedAppointmentFrom(dto, veterinar);

            when(doctorRepository.findById(1L)).thenReturn(Optional.of(veterinar));
            when(repository.findByDatumAndVremeAndVeterinar(subota, vreme, veterinar)).thenReturn(Optional.empty());
            when(repository.save(any(Appointment.class))).thenReturn(sacuvan);

            assertThat(service.zakaziTermin(dto)).isNotNull();
            verify(repository).save(any(Appointment.class));
        }

        @Test
        @DisplayName("odbija termin u prošlosti")
        void rejectsAppointmentInThePast() {
            AppointmentRequestDTO dto = validRequest(LocalDate.now().minusDays(1), LocalTime.of(11, 0), 1L);

            assertThatThrownBy(() -> service.zakaziTermin(dto))
                    .isInstanceOf(InvalidAppointmentException.class)
                    .hasMessageContaining("prošlosti");

            verifyNoInteractions(calendarNotificationService, emailService);
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("odbija termin nedeljom jer ambulanta ne radi")
        void rejectsAppointmentOnSunday() {
            LocalDate nedelja = nextDateOn(DayOfWeek.SUNDAY);
            AppointmentRequestDTO dto = validRequest(nedelja, LocalTime.of(11, 0), 1L);

            assertThatThrownBy(() -> service.zakaziTermin(dto))
                    .isInstanceOf(InvalidAppointmentException.class)
                    .hasMessageContaining("Nedeljom");

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("odbija vreme koje nije u radnom vremenu za taj dan")
        void rejectsTimeOutsideWorkingHours() {
            LocalDate subota = nextDateOn(DayOfWeek.SATURDAY);
            // 15:00 subotom ne postoji u listi dozvoljenih termina (subota ide do 12:30)
            AppointmentRequestDTO dto = validRequest(subota, LocalTime.of(15, 0), 1L);

            assertThatThrownBy(() -> service.zakaziTermin(dto))
                    .isInstanceOf(InvalidAppointmentException.class)
                    .hasMessageContaining("radnom vremenu");

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("odbija zahtev ako izabrani veterinar ne postoji")
        void rejectsUnknownVeterinarian() {
            LocalDate ponedeljak = nextDateOn(DayOfWeek.MONDAY);
            AppointmentRequestDTO dto = validRequest(ponedeljak, LocalTime.of(11, 0), 99L);

            when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.zakaziTermin(dto))
                    .isInstanceOf(InvalidAppointmentException.class)
                    .hasMessageContaining("veterinar ne postoji");

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("odbija termin na dan kad izabrani veterinar ne radi")
        void rejectsDayWhenVeterinarianDoesNotWork() {
            LocalDate ponedeljak = nextDateOn(DayOfWeek.MONDAY);
            Doctor veterinar = doctor(1L, "UTO,CET,SUB"); // ne radi ponedeljkom
            AppointmentRequestDTO dto = validRequest(ponedeljak, LocalTime.of(11, 0), 1L);

            when(doctorRepository.findById(1L)).thenReturn(Optional.of(veterinar));

            assertThatThrownBy(() -> service.zakaziTermin(dto))
                    .isInstanceOf(InvalidAppointmentException.class)
                    .hasMessageContaining("ne radi");

            verify(repository, never()).save(any());
            verifyNoInteractions(calendarNotificationService, emailService);
        }

        @Test
        @DisplayName("odbija termin koji je već zauzet kod istog veterinara")
        void rejectsSlotAlreadyBookedForSameVeterinarian() {
            LocalDate ponedeljak = nextDateOn(DayOfWeek.MONDAY);
            LocalTime vreme = LocalTime.of(11, 30);
            Doctor veterinar = doctor(1L, SVI_DANI);
            AppointmentRequestDTO dto = validRequest(ponedeljak, vreme, 1L);

            when(doctorRepository.findById(1L)).thenReturn(Optional.of(veterinar));
            when(repository.findByDatumAndVremeAndVeterinar(ponedeljak, vreme, veterinar))
                    .thenReturn(Optional.of(new Appointment()));

            assertThatThrownBy(() -> service.zakaziTermin(dto))
                    .isInstanceOf(InvalidAppointmentException.class)
                    .hasMessageContaining("zauzet");

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("dozvoljava isti termin kod drugog veterinara - zauzetost je po veterinaru")
        void allowsSameSlotForDifferentVeterinarian() {
            LocalDate ponedeljak = nextDateOn(DayOfWeek.MONDAY);
            LocalTime vreme = LocalTime.of(12, 0);
            Doctor prviVeterinar = doctor(1L, SVI_DANI);
            Doctor drugiVeterinar = doctor(2L, SVI_DANI);
            AppointmentRequestDTO dto = validRequest(ponedeljak, vreme, 2L);
            Appointment sacuvan = savedAppointmentFrom(dto, drugiVeterinar);

            // prvi veterinar je u tom terminu zauzet, ali zahtev je za drugog
            lenient().when(repository.findByDatumAndVremeAndVeterinar(ponedeljak, vreme, prviVeterinar))
                    .thenReturn(Optional.of(new Appointment()));
            when(doctorRepository.findById(2L)).thenReturn(Optional.of(drugiVeterinar));
            when(repository.findByDatumAndVremeAndVeterinar(ponedeljak, vreme, drugiVeterinar))
                    .thenReturn(Optional.empty());
            when(repository.save(any(Appointment.class))).thenReturn(sacuvan);

            assertThat(service.zakaziTermin(dto)).isNotNull();
            verify(repository).save(any(Appointment.class));
        }
    }

    @Nested
    @DisplayName("getSlobodniTermini")
    class GetSlobodniTermini {

        @Test
        @DisplayName("vraća samo termine koji nisu već zauzeti kod izabranog veterinara")
        void returnsOnlyUnbookedSlots() {
            LocalDate ponedeljak = nextDateOn(DayOfWeek.MONDAY);
            Doctor veterinar = doctor(1L, SVI_DANI);
            Appointment zauzet1 = new Appointment();
            zauzet1.setVreme(LocalTime.of(11, 0));
            Appointment zauzet2 = new Appointment();
            zauzet2.setVreme(LocalTime.of(14, 30));

            when(doctorRepository.findById(1L)).thenReturn(Optional.of(veterinar));
            when(repository.findByDatumAndVeterinar(ponedeljak, veterinar)).thenReturn(List.of(zauzet1, zauzet2));

            List<LocalTime> slobodni = service.getSlobodniTermini(ponedeljak, 1L);

            assertThat(slobodni)
                    .doesNotContain(LocalTime.of(11, 0), LocalTime.of(14, 30))
                    .contains(LocalTime.of(11, 30), LocalTime.of(17, 30));
        }

        @Test
        @DisplayName("vraća praznu listu za nedelju")
        void returnsEmptyListForSunday() {
            LocalDate nedelja = nextDateOn(DayOfWeek.SUNDAY);

            List<LocalTime> slobodni = service.getSlobodniTermini(nedelja, 1L);

            assertThat(slobodni).isEmpty();
            verifyNoInteractions(repository, doctorRepository);
        }

        @Test
        @DisplayName("vraća praznu listu na dan kad veterinar ne radi")
        void returnsEmptyListWhenVeterinarianDoesNotWork() {
            LocalDate ponedeljak = nextDateOn(DayOfWeek.MONDAY);
            Doctor veterinar = doctor(1L, "UTO,CET,SUB");

            when(doctorRepository.findById(1L)).thenReturn(Optional.of(veterinar));

            assertThat(service.getSlobodniTermini(ponedeljak, 1L)).isEmpty();
            verifyNoInteractions(repository);
        }

        @Test
        @DisplayName("baca grešku ako veterinar ne postoji")
        void throwsWhenVeterinarianDoesNotExist() {
            LocalDate ponedeljak = nextDateOn(DayOfWeek.MONDAY);

            when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getSlobodniTermini(ponedeljak, 99L))
                    .isInstanceOf(InvalidAppointmentException.class);
        }
    }

    @Nested
    @DisplayName("promeniStatus")
    class PromeniStatus {

        @Test
        @DisplayName("menja status postojećeg termina")
        void updatesStatusOfExistingAppointment() {
            Appointment postojeci = new Appointment();
            postojeci.setId(5L);
            postojeci.setStatus(AppointmentStatus.NA_CEKANJU);

            when(repository.findById(5L)).thenReturn(Optional.of(postojeci));
            when(repository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

            AppointmentResponseDTO odgovor = service.promeniStatus(5L, AppointmentStatus.POTVRDJEN);

            assertThat(odgovor.getStatus()).isEqualTo(AppointmentStatus.POTVRDJEN);
            verify(repository).save(postojeci);
        }

        @Test
        @DisplayName("baca grešku ako termin ne postoji")
        void throwsWhenAppointmentNotFound() {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.promeniStatus(99L, AppointmentStatus.POTVRDJEN))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("obrisiTermin")
    class ObrisiTermin {

        @Test
        @DisplayName("briše postojeći termin")
        void deletesExistingAppointment() {
            when(repository.existsById(3L)).thenReturn(true);

            service.obrisiTermin(3L);

            verify(repository).deleteById(3L);
        }

        @Test
        @DisplayName("baca grešku ako termin za brisanje ne postoji")
        void throwsWhenDeletingNonExistentAppointment() {
            when(repository.existsById(42L)).thenReturn(false);

            assertThatThrownBy(() -> service.obrisiTermin(42L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(repository, never()).deleteById(any());
        }
    }
}
