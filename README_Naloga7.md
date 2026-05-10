# Naloga 7: Vzorci mikrostoritvene arhitekture

V sklopu te naloge smo obstoječi sistem nadgradili z dvema novima vzorcema mikrostoritvene arhitekture, ki še nista bila uporabljena, s ciljem izboljšanja zanesljivosti, fleksibilnosti in odpornosti sistema na napake.

## 1. Zunanja konfiguracija (External Configuration)

**Opis vzorca:** V mikrostoritveni arhitekturi je pogosto potrebno spreminjati konfiguracijske vrednosti (npr. povezave do podatkovnih baz, URL-je drugih storitev, nastavitve okolij) brez ponovnega prevajanja ali ponovnega nameščanja (deploy) same storitve. Vzorec zunanje konfiguracije izloči te nastavitve iz kode v ločen zunanji vir (npr. `.env` datoteke, konfiguracijski strežnik ali mehanizme platforme za orkestracijo, kot so Kubernetes ConfigMaps/Secrets oz. Docker Compose environment varijable).

**Smiselnost vpeljave in način uporabe:**
V našem sistemu smo storitve prilagodili tako, da namesto trdo kodiranih nastavitev (hardcoded values) za povezovanje na baze ali vmesnike (npr. vrata in hosti za gRPC/HTTP) parametre berejo iz zunanjega okolja ob zagonu kontejnerja. 
- Ustvarjeno in uporabljeno je bilo zunanje upravljanje preko `.env` datotek v sklopu `docker-compose.yml`.
- S tem se omogoča enostaven prehod med različnimi okolji (razvojno, testno, produkcijsko) samo z zamenjavo `.env` datoteke, kar poveča hitrost in varnost (gesla niso v kodi).

## 2. Idempotentni porabnik (Idempotent Consumer)

**Opis vzorca:** Pri komunikaciji med mikrostoritvami ali zunanjimi odjemalci se pogosto lahko zgodi, da zaradi omrežnih napak ali ponovnih poskusov pošiljanja (retries) isti ukaz pripotuje do storitve večkrat. Idempotentni porabnik je vzorec, ki zagotavlja, da obravnava istega sporočila (ali zahteve) večkrat nima drugačnih stranskih učinkov kot če bi bilo obravnavano samo enkrat.

**Smiselnost vpeljave in način uporabe:**
Vpeljan je bil mehanizem za preprečevanje podvajanja ključnih operacij, kot je npr. kreiranje naročila v `order-service` ali dodajanje nove knjige. 
- Odjemalci (API prehod oz. zaledje za uporabniški vmesnik) ob klicu občutljive operacije pošljejo unikaten identifikator operacije (npr. `Idempotency-Key` v HTTP zaglavju).
- Storitev (npr. `order-service`) prevzame ta ključ in preveri, ali je bila zahteva s tem ključem že uspešno obdelana (shranjeno v začasnem pomnilniku, kot je Redis, ali neposredno v bazi).
- Če je bila že obdelana, vrne prejšnji, že znan odziv, s čimer se prepreči dvojno bremenitev ali kreiranje podvojenih zapisov. S tem bistveno izboljšamo robustnost in stabilnost podatkov.

---

S tema vzorcema (ki dopolnjujeta obstoječe vzorce kot so Zbirka podatkov na storitev, Zaledja za uporabniške vmesnike in CQRS/Domenski dogodki) smo vzpostavili sistem, ki je varnejši, lažje vodljiv ter veliko bolj robusten na morebitne napake pri komunikaciji med komponentami.
