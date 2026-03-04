
## 1. Poslovni Problem in Namen Sistema


### Osnovna Funkcionalnost
Book Landing System je platforma ki omogoča ustvarjanje in upravljanje promocijskih spletnih strani (landing pages) za nove knjižne izdaje. Sistem omogoča ustvarjanje knjig, ustvarjanje strani, pregled osnovne statistike...

### Uporabniki in Pričakovanja

**Avtorji/Založniki:**
- Ustvarjanje knjig
- Ustvarjanje strani za svoje knjige
- Dodajanje vsebin (naslov, opis, slike, cene)
- Pregled statistike obiskov in prednaročil

**Obiskovalci/Bralci:**
- Ogled informacij o knjigah
- Prednaročilo knjige
- Prejemanje obvestil o izdaji

**Administratorji:**
- Upravljanje uporabnikov
- Moderacija vsebin
- Pregled analitike sistema

### Komunikacija med Komponentami
Komponente sistema komunicirajo prek REST API-jev z JSON formatom. Za asinhrono obdelavo dogodkov (npr. pošiljanje emailov) se uporablja sporočilni posrednik (RabbitMQ).

---

## 2. Glavne Domene in Mikrostoritve (Izbira tri)

### Book Service (Storitev za Knjige)
**Odgovornosti:**
- Upravljanje podatkov o knjigah (CRUD operacije)
- Shranjevanje metapodatkov (naslov, avtor, opis, ISBN, cena)
- Upravljanje slik knjig
- Validacija knjižnih podatkov

### User Service (Storitev za Uporabnike)
**Odgovornosti:**
- Registracija in avtentikacija uporabnikov
- Upravljanje uporabniških profilov
- Avtorizacija in vloge (avtor, založnik, admin)
- Upravljanje sej

### Landing Page Service (Storitev za Landing Page)
**Odgovornosti:**
- Ustvarjanje in upravljanje landing page
- Konfiguracijo dizajna in predlog
- Povezava knjig z landing page
- Objava in deaktivacija strani

### Order Service (Storitev za Naročila)
**Odgovornosti:**
- Upravljanje prednaročil
- Sledenje stanju naročil
- Integracija s plačilnimi sistemi (se preveriti)
- Generiranje računov

### Notification Service (Storitev za Obvestila)
**Odgovornosti:**
- Pošiljanje emailov (potrditve, obvestila)
- Push obvestila
- Upravljanje predlog za obvestila
- Asinhrona obdelava prek sporočilne vrste

### Analytics Service (Storitev za Analitiko)
**Odgovornosti:**
- Sledenje obiskov landing page
- Statistika konverzij (ogledi → naročila)
- Analitika uporabniških dejanj
- Generiranje poročil

---

## 3. Arhitektura Sistema

```
┌─�