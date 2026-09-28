# DermaVet Backend

Spring Boot backend za sajt veterinarske ambulante DermaVet - zamenjuje ranije rešenje
preko Make.com webhook-a pravim REST API-jem sa bazom, autentifikacijom, testovima i
API dokumentacijom.

## Overview

Ovaj servis prima zahteve za termine sa postojećeg sajta (statični `index.html`, poslužen
kroz isti Spring Boot server), čuva ih u bazi, i tek posle toga ih prosleđuje dalje u
Google kalendar (preko Make.com) i šalje mejlove. Ima i admin deo (zaštićen prijavom) za
pregled, izmenu i brisanje termina, kao i javni katalog usluga i tima lekara. Pri zakazivanju
klijent bira veterinara, a ponuđene usluge i termini zavise od izabranog veterinara.

## Features

- Zakazivanje termina sa izborom veterinara, validacijom radnog vremena i sprečavanjem duplog zakazivanja
- Svaki veterinar ima svoj raspored (radni dani) i listu usluga koje obavlja - forma nudi samo te usluge
- Endpoint za stvarno slobodne termine po datumu i veterinaru (zauzetost se računa po veterinaru)
- Prosleđivanje zakazanog termina u Google kalendar (Make.com webhook), sa bojom po veterinaru
- Automatski mejl vlasniku ambulante (novi zahtev) i klijentu (potvrda)
- Admin endpoint-i za pregled/izmenu/brisanje termina, zaštićeni HTTP Basic prijavom
- Tim lekara (sekcija "Tim" i forma) i katalog usluga po kategorijama, učitani iz baze
- JUnit 5 + Mockito testovi za poslovnu logiku
- OpenAPI/Swagger dokumentacija svih endpoint-a
- Postman kolekcija spremna za uvoz
- GitHub Actions CI (build + testovi na svaki push/PR)
- Docker + Docker Compose za pokretanje sa pravom PostgreSQL bazom

## Tech Stack

Java 17, Spring Boot 3 (Web, Data JPA, Validation, Security, Mail), PostgreSQL / H2,
springdoc-openapi, JUnit 5, Mockito, Docker, Docker Compose, GitHub Actions

## Architecture

Standardna slojevita arhitektura - kontroler ne razgovara direktno sa bazom, uvek ide
preko servisa:

```
controller/  -> prima HTTP zahteve, validira ulaz, vraća odgovor
service/     -> poslovna logika (radno vreme, provera termina, kalendar, mejlovi)
repository/  -> Spring Data JPA, razgovor sa bazom
model/       -> JPA entiteti (Appointment, Doctor, ServiceCategory, ClinicService)
               Doctor <-> ClinicService je many-to-many (usluge koje veterinar obavlja)
dto/         -> objekti koji putuju kroz REST API (sa validacijom i OpenAPI opisima)
exception/   -> centralizovano hvatanje grešaka (GlobalExceptionHandler)
config/      -> Security, OpenAPI, RestTemplate bean, seed podaci
```

Tok zahteva pri zakazivanju termina:

```
Frontend (index.html, forma za zakazivanje)
        |
        v
   Controller  (AppointmentController - prima i validira zahtev)
        |
        v
   Service     (AppointmentService - radno vreme, provera duplog zakazivanja)
        |
        v
   Repository  (Spring Data JPA)
        |
        v
   PostgreSQL / H2   <-- baza je "izvor istine", upisuje se prva

Posle uspešnog upisa u bazu, Service dalje zove (svaki u svom try/catch, ne blokiraju
zakazivanje ako padnu):
        |-- Google Calendar (preko Make.com webhook-a)
        |-- Email servis   (mejl vlasniku ambulante + potvrda klijentu)
```

### Tok forme za zakazivanje

1. Sajt pri učitavanju zove `GET /api/doctors` i `GET /api/services` - puni sekciju "Tim" i formu
2. Klijent bira veterinara -> polje "Usluga" se puni uslugama tog veterinara, grupisanim po kategoriji
3. Klijent bira datum -> `GET /api/appointments/slobodni-termini?datum=...&veterinarId=...` vraća
   slobodne termine (prazna lista ako veterinar tog dana ne radi)
4. `POST /api/appointments` - backend ponovo proverava sve (radni dan veterinara, termin, zauzetost)

Sekcija "Usluge" na samom sajtu je namerno ostala statična (potpun prikaz svega što ambulanta
nudi); baza čuva isti katalog da bi ga forma mogla koristiti.

### Google Calendar - boja termina po veterinaru

Backend uz svaki termin šalje Make.com webhook-u i polja `veterinar` (ime) i `colorId`
(Google Calendar boja 1-11, čuva se u `Doctor.googleCalendarBoja`; u seed podacima 5 = žuta,
9 = plava). Sama boja se podešava u Make.com scenariju (van ovog repozitorijuma):

- u modulu Google Calendar -> Create an Event potražiti polje za boju (Color) i mapirati `colorId`, ili
- ako polje prihvata samo izbor iz liste: ispred modula dodati Router sa filterom po polju
  `veterinar` i po jedan Google Calendar modul za svakog veterinara sa fiksno izabranom bojom.

## API

Puna dokumentacija (parametri, primeri, mogući error kodovi) dostupna je kroz Swagger UI
kad je aplikacija pokrenuta: **http://localhost:8080/swagger-ui.html**
(raw OpenAPI JSON: `/v3/api-docs`).

| Metoda | Putanja | Zaštita | Opis |
|---|---|---|---|
| POST | `/api/appointments` | javno | Zakazuje termin (telo sadrži `veterinarId`) |
| GET | `/api/appointments/slobodni-termini?datum=2026-09-25&veterinarId=1` | javno | Slobodni termini za datum i veterinara |
| GET | `/api/appointments` | admin | Svi termini |
| PATCH | `/api/appointments/{id}/status?status=POTVRDJEN` | admin | Promena statusa termina |
| DELETE | `/api/appointments/{id}` | admin | Brisanje termina |
| GET | `/api/doctors` | javno | Lista lekara, sa radnim danima i uslugama koje obavljaju |
| GET | `/api/services` | javno | Katalog usluga po kategorijama |

## Authentication

Admin endpoint-i (`GET/PATCH/DELETE /api/appointments`) su zaštićeni Spring Security-jem
uz HTTP Basic prijavu. Kredencijali dolaze isključivo iz environment varijabli
(`ADMIN_USERNAME`, `ADMIN_PASSWORD`) i hešuju se BCrypt-om - nigde u kodu nema
hardkodovane lozinke.

```bash
curl -u <ADMIN_USERNAME>:<ADMIN_PASSWORD> http://localhost:8080/api/appointments
```

Sesije se ne koriste (`STATELESS`), pa je CSRF zaštita namerno isključena - CSRF napad
zavisi od kolačića koje browser šalje automatski, a ovde ih nema.

## Database

- **Lokalno (`dev` profil)**: H2 baza u memoriji, ne treba ništa instalirati. Reset pri
  svakom pokretanju - dovoljno za razvoj i testiranje.
- **Docker (`prod` profil)**: prava PostgreSQL baza, podaci ostaju sačuvani u Docker
  volume-u i posle restarta.

Profil se bira automatski preko `SPRING_PROFILES_ACTIVE` (podrazumevano `dev`).

Pri prvom pokretanju prazna baza se puni početnim podacima (`DataSeeder`): katalog usluga i
dva lekara. Radni dani lekara i veza lekar - usluge su **ilustrativni primer podataka**
(u pravoj ambulanti se menjaju prema stvarnom rasporedu).

Napomena: `Appointment` sada ima obavezno polje `veterinar_id`. Ako je PostgreSQL volume nastao
pre ove izmene i već sadrži termine, Hibernate ne može da doda kolonu - reset lokalne baze:
`docker-compose down -v`.

## Running locally

1. Otvoriti projekat u IntelliJ IDEA kao Maven projekat (`pom.xml`)
2. Pokrenuti `DermavetBackendApplication`
3. Otvoriti `http://localhost:8080` - sajt i backend rade na istom portu (bez CORS-a)
4. H2 konzola: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:dermavet`)

Za mejlove i admin login treba podesiti environment varijable - videti sekciju
**Setup .env** ispod.

## Docker

```bash
cp .env.example .env   # popuniti sopstvenim vrednostima
docker-compose up --build
```

Podiže PostgreSQL bazu i backend zajedno; sajt i API dostupni na `http://localhost:8080`.

### Setup .env

Projekat ne radi "iz kutije" - mejl i admin login zahtevaju sopstvene kredencijale.

1. `cp .env.example .env`
2. Popuniti:
   - `DB_PASSWORD` - lozinka za Postgres bazu (proizvoljna, samo za lokalni Docker)
   - `MAIL_USERNAME` / `MAIL_PASSWORD` - nalog sa kog se šalju mejlovi (SMTP)
   - `ADMIN_EMAIL` - na koji mejl stiže obaveštenje o novom zahtevu za termin
   - `ADMIN_USERNAME` / `ADMIN_PASSWORD` - login za zaštićene `/api/appointments` endpoint-e

Za Gmail SMTP treba App Password, ne obična lozinka naloga - generiše se u podešavanjima
naloga (zahteva uključenu dvofaktorsku autentifikaciju). Ako opcija nije dostupna (npr.
organizacioni/Workspace nalog gde je administrator to onemogućio), koristiti drugi mejl
provajder ili poseban nalog napravljen samo za ovu svrhu.

Bez popunjenog `.env` fajla aplikacija se pokreće, ali mejlovi neće biti poslati, a admin
prijava neće raditi dok se `ADMIN_USERNAME`/`ADMIN_PASSWORD` ne postave.

## Testing

Unit testovi (JUnit 5 + Mockito, 17 testova) pokrivaju poslovnu logiku u `AppointmentService`: radno
vreme, raspored veterinara, zauzetost po veterinaru, slobodni termini, promena statusa i brisanje. Baza,
kalendar i mejl servis su mokovani - testovi ne zavise od prave baze ili interneta.

```bash
mvn test
```

CI (`.github/workflows/ci.yml`) pokreće `mvn clean verify` na svaki push i pull request
ka `main` grani.

## Postman

Kolekcija sa svim endpoint-ima nalazi se u `postman/DermaVet.postman_collection.json`.
Uvesti je u Postman (Import -> File), i po potrebi podesiti kolekcijske varijable
(`baseUrl`, `adminUsername`, `adminPassword`, `appointmentId`).

## Future improvements

- JWT umesto HTTP Basic ako zatreba više od jednog admin naloga
- Integracioni testovi (`@SpringBootTest` + Testcontainers za PostgreSQL)
- Raspored veterinara po satima (trenutno samo po danima u nedelji) i odsustva/godišnji odmori
- Admin endpoint-i za izmenu lekara i usluga (trenutno se menjaju kroz seed/bazu)
- Paginacija za `GET /api/appointments` kad broj termina poraste
