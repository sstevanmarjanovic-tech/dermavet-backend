package com.dermavet.backend.config;

import com.dermavet.backend.model.ClinicService;
import com.dermavet.backend.model.Doctor;
import com.dermavet.backend.model.ServiceCategory;
import com.dermavet.backend.repository.DoctorRepository;
import com.dermavet.backend.repository.ServiceCategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Puni bazu pocetnim podacima kad je prazna: katalog usluga (tacno onako kako je prikazan
 * na sajtu u sekciji "Usluge" - ta sekcija na sajtu ostaje staticna, ovo je samo isti sadrzaj
 * u bazi da bi forma za zakazivanje mogla da ga koristi) i lekare, sa ILUSTRATIVNIM rasporedom
 * rada i vezom ka uslugama koje svaki od njih moze da obavi.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private record SeedUsluga(String naziv, String ikona, String opis, String detaljanOpis) {
        SeedUsluga(String naziv) {
            this(naziv, null, null, null);
        }
    }

    private final DoctorRepository doctorRepository;
    private final ServiceCategoryRepository categoryRepository;

    public DataSeeder(DoctorRepository doctorRepository,
                       ServiceCategoryRepository categoryRepository) {
        this.doctorRepository = doctorRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional // drzi jednu Hibernate sesiju otvorenu dok povezujemo lekare sa vec ucitanim uslugama (lazy veza)
    public void run(String... args) {
        if (categoryRepository.count() == 0) {
            seedServiceCatalog();
        }
        if (doctorRepository.count() == 0) {
            seedDoctors();
        }
    }

    private void seedServiceCatalog() {
        kategorija("Dermatologija",
                new SeedUsluga("Alergo test", "🧬", "Intradermalni i iz krvi - jedina metoda koja otkriva uzrok alergija.",
                        "Intradermalni alergotest ubrizgavanjem mikro-količina alergena u kožu otkriva tačne okidače - poleni, grinje, buva pljuvačka. Alergotest iz krvi (serum IgE) je neinvazivna alternativa. Na osnovu rezultata kreiramo individualizovanu imunoterapiju - jedini trajni tretman alergija."),
                new SeedUsluga("Dermatološki pregled", "🔬", "Kompletan pregled kože i dlake korišćenjem dermatoskopa i Wood lampe.",
                        "Kompletan pregled kože pomoću dermatoskopa, lupe i Wood lampe. Procenjujemo dlaku, nokte, kožne nabore. Tražimo parazite (Demodex, Sarcoptes), procenjujemo vrstu infekcije i određujemo optimalnu terapiju."),
                new SeedUsluga("Citologija", "🧫", "Brza mikroskopska dijagnostika promena na koži za 15-20 minuta.",
                        "Celofanskim brisom, skarifikatom ili aspiracijom ćelija dobijamo materijal za mikroskopsku analizu. Za 15-20 minuta saznajemo da li su prisutne bakterije, kvasci, inflamatorne ćelije ili maligni elementi."),
                new SeedUsluga("Wood lampa", "🔦", "UV dijagnostika gljivičnih infekcija - brza i neinvazivna metoda.",
                        "Dermatofite apsorbuju UV svetlost i fluorescentno sijaju zeleno-žutom bojom. Pregled traje 2-3 minuta. Bezbolna i potpuno bezopasna metoda za pacijenta."),
                new SeedUsluga("Video-otoskopija", "👂", "HD pregled ušnog kanala uz uvećanje od 20-40x za preciznu dijagnostiku.",
                        "Video-otoskop sa HD kamerom prikazuje ušni kanal uveličan 20-40x na monitoru. Terapija se izvodi istim aparatom pod direktnom vizuelnom kontrolom."),
                new SeedUsluga("Laser u dermatologiji", "⚡", "Medicinski diodni laser za terapiju hroničnih kožnih oboljenja i otitisa.",
                        "Medicinski diodni laser deluje precizno na inflamatorna tkiva smanjujući bol, edem i ubrzavajući zarastanje. Vidljivi rezultati već posle 2-3 tretmana, bez hemikalija."),
                new SeedUsluga("Trihogram", "💇", "Mikroskopska analiza dlake za dijagnostiku endokrinih poremećaja.",
                        "Analiziramo 20-30 dlaka pod mikroskopom - određujemo fazu rasta, strukturu dlake i prisustvo parazita. Patološki trihogram ukazuje na endokrini disbalans ili imunoposredovane dermatoze."),
                new SeedUsluga("FNA metoda", "🩺", "Fine needle aspiration tumora i limfnih čvorova za citološku analizu.",
                        "Tankom iglom aspiriramo ćelije iz tumora, uvećanih limfnih čvorova ili cistične promene. Citološka analiza za 15 minuta određuje hitnost hirurške intervencije.")
        );

        kategorija("Min. invazivna hirurgija",
                new SeedUsluga("Laparoskopska sterilizacija kuja i mačaka"),
                new SeedUsluga("Klasična sterilizacija i kastracija mužjaka"),
                new SeedUsluga("Opšta hirurgija mekih tkiva"),
                new SeedUsluga("Savremena anestezija i intraoperativni monitoring"),
                new SeedUsluga("Postoperativna nega i praćenje oporavka")
        );

        kategorija("Endoskopija",
                new SeedUsluga("Video-otoskopija", "👂", "Pregled ušnog kanala uz HD uvećanje - dijagnostika i tretman infekcija.",
                        "Pomoću video-otoskopa sa HD kamerom pregledamo ušni kanal uveličan 20-40x. Terapeutske procedure vrše se istim aparatom pod direktnom vizuelnom kontrolom - bez slepe aplikacije."),
                new SeedUsluga("Gastroskopija", "🔭", "Pregled jednjaka i želuca bez hirurgije - biopsija i vađenje stranih tela.",
                        "Fleksibilnim videoendoskopom pregledamo jednjak, želudac i gornji duodenum. Uzimamo biopsije za histopatologiju. Strana tela se vade istim aparatom - bez hirurškog reza."),
                new SeedUsluga("Bronhoskopija", "🫁", "Pregled disajnih puteva, uzimanje BAL uzoraka, dijagnostika traheje.",
                        "Pregledamo larinks, traheju i bronhije. Uzimamo BAL za citologiju i kulturu. Dijagnostikujemo trahealni kolaps, hronični bronhitis, tumore i strana tela."),
                new SeedUsluga("Rinoskopija", "👃", "Pregled nosnih šupljina - polipi, tumori, strana tela, dentalne fistule.",
                        "Pregledamo nosne hodnike, etmoidnu regiju i nazofarinks. Uzimamo biopsije i vršimo minimalno invazivne terapeutske procedure."),
                new SeedUsluga("Kolonoskopija", "🔍", "Pregled debelog creva - kolitisi, polipi, biopsija na više nivoa.",
                        "Fleksibilnim kolonoskopom pregledamo debelo crevo i ileocekalnu regiju. Uzimamo biopsije na više nivoa za preciznu histopatološku analizu.")
        );

        kategorija("Interna medicina",
                new SeedUsluga("Kompletan klinički pregled i anamneza"),
                new SeedUsluga("Gastroenterologija - creva, pankreas, jetra"),
                new SeedUsluga("Urologija - bubrezi, bešika, prostata"),
                new SeedUsluga("Endokrinologija - dijabetes, Cushingov sindrom"),
                new SeedUsluga("Ultrazvučna dijagnostika unutrašnjih organa"),
                new SeedUsluga("Ehokardiografija - srce i krvni sudovi"),
                new SeedUsluga("Terapija trovanja i intenzivna nega")
        );

        kategorija("Stomatologija",
                new SeedUsluga("Ultrazvučno uklanjanje kamenca"),
                new SeedUsluga("Poliranje zuba i fluoridizacija"),
                new SeedUsluga("Ekstrakcija zuba u opštoj anesteziji"),
                new SeedUsluga("Terapija stomatitisa i gingivitisa"),
                new SeedUsluga("Oralna hirurgija - uklanjanje tumora i cista"),
                new SeedUsluga("Kompletan pregled usne duplje")
        );

        kategorija("Laboratorija",
                new SeedUsluga("Hematologija", "🩸", "5-diff analizator - kompletna krvna slika za 3 minute.",
                        "Automatski 5-diff analizator broji i diferentuje sve krvne ćelije. Ključno za dijagnostiku anemija, infekcija i leukoza."),
                new SeedUsluga("Biohemija krvi", "🧪", "20+ parametara - jetra, bubrezi, pankreas, elektroliti.",
                        "Analizator simultano meri enzime jetre, funkciju bubrega, pankreas, proteine, elektrolite, glukozu i holesterol. Kompletan uvid za 20 minuta."),
                new SeedUsluga("Analiza urina", "💧", "Sediment i biohemija - dijagnostika bubrežnih i urinarnih oboljenja.",
                        "Fizičkim, hemijskim i mikroskopskim pregledom detektujemo glukozu, proteine, bilirubin, pH i sediment. Dijagnostikujemo cistitis, nefritis, dijabetes i urolitijazu."),
                new SeedUsluga("Koprologija", "🔬", "Pregled stolice na parazite - Toxocara, Giardia, Cryptosporidium.",
                        "Flotacijom, nativnom i Lugol metodom pronalazimo jaja i larvne forme parazita. Određujemo intenzitet invazije i biramo odgovarajući antiparazitik."),
                new SeedUsluga("Antigenski testovi", "⚡", "Brzi testovi: parvo, distemper, FIV, FeLV, giardija - 10 minuta.",
                        "Rapid ELISA testovi iz kapljice krvi. Rezultat za 10 minuta, visoka senzitivnost i specifičnost - pouzdani i u urgentnim situacijama.")
        );

        kategorija("Preventiva",
                new SeedUsluga("Savetovanje vlasnika", "💬", "Individualni plan ishrane, nege i prevencije bolesti.",
                        "Kreiramo individualni program ishrane i nege prilagođen rasi, starosti i zdravstvenom statusu. Edukujemo o znacima bolesti i vakcinacionim protokolima."),
                new SeedUsluga("Dehelmintizacija", "🪱", "Zaštita od buva, krpelja, grinja, glista - ESCCAP protokol.",
                        "Na osnovu životnog stila i rizika kreiramo individualni plan antiparazitne zaštite, vodeći se ESCCAP smernicama."),
                new SeedUsluga("Vakcinacija", "💉", "WSAVA protokoli - besnilo, parvo, distemper, FPV, pasoši.",
                        "Vakcinišemo prema WSAVA i nacionalnim protokolima. Core vakcine su obavezne za sve, non-core preporučujemo na osnovu procenjenog rizika. Izdajemo pasoše za putovanje.")
        );
    }

    private void kategorija(String naziv, SeedUsluga... usluge) {
        ServiceCategory kat = new ServiceCategory(naziv);
        for (SeedUsluga u : usluge) {
            // popunjavamo obe strane veze; cascade ALL na kategoriji cuva i usluge
            kat.getUsluge().add(new ClinicService(u.naziv(), u.ikona(), u.opis(), u.detaljanOpis(), kat));
        }
        categoryRepository.save(kat);
    }

    private void seedDoctors() {
        // Google Calendar colorId: 5 = Banana (zuta), 9 = Blueberry (plava) - videti README/Make.com uputstvo
        Doctor jovana = doctorRepository.save(new Doctor(
                "Spec. dr vet. Jovana Ranisavljević",
                "Specijalista dermatolog",
                "Dermatolog, Imunoterapija",
                "Diplomirala na Fakultetu veterinarske medicine u Beogradu, gde je odbranila specijalističku tezu iz veterinarske dermatologije.",
                9,
                "PON,UTO,SRE,CET,PET", // specijalista - radi svih 5 radnih dana, ne radi subotom
                "5"));

        Doctor dusan = doctorRepository.save(new Doctor(
                "Dr vet. Dušan Milenković",
                "Veterinar",
                "Interna medicina, Endoskopija, Hirurgija",
                "Diplomirao na Fakultetu veterinarske medicine u Beogradu. Usavršavao se u minimalno invazivnoj dijagnostici i endoskopskim procedurama.",
                5,
                "UTO,CET,SUB", // opsti veterinar - radi deo nedelje, pokriva i subotnju smenu
                "9"));

        // Jovana (specijalista dermatolog) - pokriva Dermatologiju i Preventivu
        List<ClinicService> jovaninUsluge = new ArrayList<>();
        jovaninUsluge.addAll(uslugeKategorije("Dermatologija"));
        jovaninUsluge.addAll(uslugeKategorije("Preventiva"));
        jovana.setUsluge(jovaninUsluge);
        doctorRepository.save(jovana);

        // Dusan (opsti veterinar) - pokriva sirok raspon: hirurgija, endoskopija, interna, stomatologija, laboratorija
        List<ClinicService> dusanoveUsluge = new ArrayList<>();
        dusanoveUsluge.addAll(uslugeKategorije("Min. invazivna hirurgija"));
        dusanoveUsluge.addAll(uslugeKategorije("Endoskopija"));
        dusanoveUsluge.addAll(uslugeKategorije("Interna medicina"));
        dusanoveUsluge.addAll(uslugeKategorije("Stomatologija"));
        dusanoveUsluge.addAll(uslugeKategorije("Laboratorija"));
        dusan.setUsluge(dusanoveUsluge);
        doctorRepository.save(dusan);
    }

    private List<ClinicService> uslugeKategorije(String nazivKategorije) {
        return categoryRepository.findAll().stream()
                .filter(k -> k.getNaziv().equals(nazivKategorije))
                .findFirst()
                .map(ServiceCategory::getUsluge)
                .orElse(List.of());
    }
}
