. Poslovni Problem in Namen Sistema

Osnovna Funkcionalnost

Book Landing System je platforma ki omogoča ustvarjanje in upravljanje promocijskih spletnih strani (landing pages) za nove knjižne izdaje. Sistem omogoča ustvarjanje knjig, ustvarjanje strani, pregled osnovne statistike...

Uporabniki in Pričakovanja

Avtorji/Založniki:

Ustvarjanje knjig
Ustvarjanje strani za svoje knjige
Dodajanje vsebin (naslov, opis, slike, cene)
Pregled statistike obiskov in prednaročil
Obiskovalci/Bralci:

Ogled informacij o knjigah
Prednaročilo knjige
Prejemanje obvestil o izdaji
Administratorji:

Upravljanje uporabnikov
Moderacija vsebin
Pregled analitike sistema
Komunikacija med Komponentami

Komponente sistema komunicirajo prek REST API-jev z JSON formatom. Za asinhrono obdelavo dogodkov (npr. pošiljanje emailov) se uporablja sporočilni posrednik (RabbitMQ).

2. Glavne Domene in Mikrostoritve (Izbira tri)

Book Service (Storitev za Knjige)

Odgovornosti:

Upravljanje podatkov o knjigah (CRUD operacije)
Shranjevanje metapodatkov (naslov, avtor, opis, ISBN, cena)
Upravljanje slik knjig
Validacija knjižnih podatkov
User Service (Storitev za Uporabnike)

Odgovornosti:

Registracija in avtentikacija uporabnikov
Upravljanje uporabniških profilov
Avtorizacija in vloge (avtor, založnik, admin)
Upravljanje sej
Landing Page Service (Storitev za Landing Page)

Odgovornosti:

Ustvarjanje in upravljanje landing page
Konfiguracijo dizajna in predlog
Povezava knjig z landing page
Objava in deaktivacija strani
Order Service (Storitev za Naročila)

Odgovornosti:

Upravljanje prednaročil
Sledenje stanju naročil
Integracija s plačilnimi sistemi (se preveriti)
Generiranje računov
Notification Service (Storitev za Obvestila)

Odgovornosti:

Pošiljanje emailov (potrditve, obvestila)
Push obvestila
Upravljanje predlog za obvestila
Asinhrona obdelava prek sporočilne vrste
Analytics Service (Storitev za Analitiko)

Odgovornosti:

Sledenje obiskov landing page
Statistika konverzij (ogledi → naročila)
Analitika uporabniških dejanj
Generiranje poročil
število obiskov landing page
število klikov na "Prednaroči"
konverzijo (views → orders)
3. Arhitektura Sistema

┌─────────────────────────────────────────────────────────────────┐
│                        Frontend (React/Vue)                      │
│                    Landing Pages + Admin Panel                   │
└────────────────────────────┬────────────────────────────────────┘
                             │ HTTPS/REST
┌────────────────────────────┴────────────────────────────────────┐
│                        API Gateway (Kong/Nginx)                  │
│                    Authentication & Rate Limiting                │
└─────┬──────┬──────┬──────┬──────┬──────────────────────────────┘
      │      │      │      │      │
      │      │      │      │      │     REST APIs
┌─────▼──┐ ┌─▼────┐ ┌─▼──┐ ┌─▼──┐ ┌─▼──────┐
│ Book   │ │ User │ │Land│ │Ord │ │Analytics│
│Service │ │Serv  │ │Page│ │Serv│ │ Service │
│        │ │ice   │ │Serv│ │ice │ │         │
└───┬────┘ └──┬───┘ └─┬──┘ └─┬──┘ └────┬────┘
    │         │       │      │         │
    │ DB      │ DB    │ DB   │ DB      │ DB
┌───▼────┐┌──▼───┐┌──▼──┐┌──▼──┐  ┌───▼────┐
│Books DB││Users ││Pages││Orders│ │Analytics│
│(Postgre││DB    ││DB   ││DB    │ │DB       │
│SQL)    ││(Post)││(Mong││(Post)│ │(MongoDB)│
└────────┘└──────┘└─────┘└──────┘ └─────────┘
              │            │
              └────────┬───┘
              ┌────────▼──────────┐
              │ Message Broker    │
              │    (RabbitMQ)     │
              └────────┬──────────┘
                       │
              ┌────────▼──────────┐
              │  Notification     │
              │    Service        │
              └───────────────────┘
Diagram

Frontend je del uporabniskega vmesnika. Ne komunicira direktno s storitvijo, ampak s pomocjo https/rest
Api Gateway je vstopna tocka v sistem. Njegove naloge so: sprejem zahtev iz frontenda, preveri avtentikacijo, preusmeri zahtevo na mikrostoritev
Mikrostoritev, vsaka je neodvisno in ima tocno doloceno odgovornost
DB : vsaka mikrostoritev ima svojo bazo podatkov, vsaka vpravlja svoje podakte
RabbitMQ omogoca asinhrono komunikacijo, storitev poslje dogodek in ga druge poslusaju
Ključne Arhitekturne Odločitve

Komunikacija:

REST API za sinhrono komunikacijo med storitvami Komunikacija med froentnd in mikrostoritvijo Komunikacija poteka preko http protokola, podatki so posredovani v json formatu
primer: frontend pošlje zahtevo GET /api/landing-pages/45 Landing page service vrne podatke o strani { "id": 45, "bookId": 123, "title": "New Fantasy Novel", "description": "A new epic adventure" } Nato frontend zahteva še podatke o knjigi GET /api/books/123 Book service vrne podatke o knjigi

RabbitMQ za asinhrono pošiljanje obvestil

za podatke ki ne potrebujejo odogovor takoj, se uporablja asinhrona komunikacija primer: Uporabnik odda narocilo Order service ustvari narocilo v bazi Nato poslje dogodek v RabbitMQ order.placed Notification service poslusa dogodek in ko sprejme sporocilo poslje uporabniku email Tako order service ne caka da se email poslje

Api gateway

API Gateway je vstopna točka v sistem. Njegove naloge so: usmerjanje zahtev do mikrostoritev avtentikacija uporabnikov omejevanje zahtev (rate limiting) skrivanje notranje arhitekture sistema

Baze Podatkov:

PostgreSQL za strukturirane podatke (knjige, uporabniki, naročila) Za strogo strukturirane podatke kot so Book Service, User Service in Order service
MongoDB za fleksibilne dokumente (landing page konfiguracije, analitika) za manje strukturirane podatke, kao sto su analytics service in landing page service
Uporabniški Vmesnik:

Frontend SPA (Single Page Application) ločen od backend storitev
Komunicira prek API Gateway primer: Avtor ustvari novo knjigo, frontend poslje novo zahtevo post /api/books Api gateway zahtevo preusmeri na Book Service Book service shvari v DB Sistem vrne odgovor in frontend prikaze ustvarjeno knjigo
4. Struktura Repozitorija

new-book-landing-system/
├── README.md
├── docs/
│   ├── architecture.md
│   ├── api-specification.md
│   └── deployment.md
├── services/
│   ├── book-service/
│   │   ├── src/
│   │   ├── tests/
│   │   ├── Dockerfile
│   │   └── package.json
│   ├── user-service/
│   │   ├── src/
│   │   ├── tests/
│   │   ├── Dockerfile
│   │   └── package.json
│   ├── landing-page-service/
│   │   ├── src/
│   │   ├── tests/
│   │   ├── Dockerfile
│   │   └── package.json
│   ├── order-service/
│   │   ├── src/
│   │   ├── tests/
│   │   ├── Dockerfile
│   │   └── package.json
│   ├── notification-service/
│   │   ├── src/
│   │   ├── tests/
│   │   ├── Dockerfile
│   │   └── package.json
│   └── analytics-service/
│       ├── src/
│       ├── tests/
│       ├── Dockerfile
│       └── package.json
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   └── services/
│   ├── public/
│   └── package.json
├── api-gateway/
│   ├── config/
│   └── nginx.conf
├── infrastructure/
│   ├── docker-compose.yml
│   ├── kubernetes/
│   └── terraform/
└── shared/
    ├── models/
    └── utils/
5. Komunikacija med Storitvami

REST API Komunikacija

Book Service API:

GET    /api/books              - Seznam knjig
GET    /api/books/:id          - Podrobnosti knjige
POST   /api/books              - Ustvari knjigo
PUT    /api/books/:id          - Posodobi knjigo
DELETE /api/books/:id          - Izbriši knjigo
User Service API:

POST   /api/auth/register      - Registracija
POST   /api/auth/login         - Prijava
GET    /api/users/profile      - Uporabniški profil
PUT    /api/users/profile      - Posodobi profil
Landing Page Service API:

GET    /api/landing-pages      - Seznam landing pages
GET    /api/landing-pages/:id  - Podrobnosti landing page
POST   /api/landing-pages      - Ustvari landing page
PUT    /api/landing-pages/:id  - Posodobi landing page
POST   /api/landing-pages/:id/publish - Objavi landing page
Order Service API:

POST   /api/orders             - Ustvari naročilo
GET    /api/orders/:id         - Podrobnosti naročila
GET    /api/orders/user/:userId - Naročila uporabnika
Sporočilni Posrednik (Message Broker)

Eventi prek RabbitMQ:

book.created - Nova knjiga ustvarjena
order.placed - Novo naročilo oddano
order.confirmed - Naročilo potrjeno
user.registered - Nov uporabnik registriran
Notification Service posluša te dogodke in pošilja ustrezna obvestila.

6. Implementacija Osnovnih Funkcionalnosti

Pristop k Implementaciji

Poslovna Logika - najprej implementiramo osnovne poslovne funkcije (domain layer)
API Endpoints - nato dodamo REST API končne točke
Integracija s Podatkovno Bazo - povezava z bazo podatkov
Komunikacija med Storitvami - integracija storitev
Primer Strukture Storitve (Book Service)

book-service/
├── src/
│   ├── domain/              # Poslovna logika
│   │   ├── book.entity.ts
│   │   ├── book.repository.interface.ts
│   │   └── book.service.ts
│   ├── application/         # Uporabniški primeri
│   │   ├── create-book.usecase.ts
│   │   └── get-book.usecase.ts
│   ├── infrastructure/      # Tehnična implementacija
│   │   ├── database/
│   │   │   └── book.repository.ts
│   │   └── api/
│   │       └── book.controller.ts
│   └── main.ts
└── tests/
    ├── unit/
    └── integration/
7. Testiranje

Enotski Testi (Unit Tests)

Testiranje poslovne logike brez odvisnosti
Uporaba mock objektov za externe odvisnosti
Pokritost vsaj 80% kode
Integracijski Testi

Testiranje API končnih točk
Testiranje povezave s podatkovno bazo
Testiranje komunikacije med storitvami
E2E Testi

Testiranje celotnih uporabniških scenarijev
Simulacija realnih uporabniških interakcij
Primer Testov

// Unit test
describe('BookService', () => {
  it('should create a book with valid data', async () => {
    const bookData = {
      title: 'Test Book',
      author: 'Test Author',
      isbn: '978-3-16-148410-0'
    };
    const book = await bookService.createBook(bookData);
    expect(book.title).toBe('Test Book');
  });
});

// Integration test
describe('Book API', () => {
  it('POST /api/books should create a new book', async () => {
    const response = await request(app)
      .post('/api/books')
      .send({ title: 'Test Book', author: 'Author' });
    expect(response.status).toBe(201);
  });
});
8. Uporabniški Vmesnik

Frontend Aplikacija

Framework: React ali Vue.js
Styling: Tailwind CSS ali Material-UI
Stanje: Redux/Pinia
HTTP Client: Axios
Strani

Javne Strani:

Landing page za knjigo (dinamično generirana)
Seznam knjig
Stran za prednaročilo
Admin Panel:

Dashboard s statistiko
Upravljanje knjig
Upravljanje landing pages
Pregled naročil
Komunikacija

Frontend komunicira izključno prek API Gateway, ki usmeri zahteve do ustreznih mikrostoritev.

9. Navodila za Zagon

Predpogoji

Docker & Docker Compose
Node.js 18+
PostgreSQL 14+
MongoDB 6+
RabbitMQ 3.11+
Razvoj z Docker Compose

# Klonirajte repozitorij
git clone https://github.com/your-username/new-book-landing-system.git
cd new-book-landing-system

# Poženite vse storitve
docker-compose up -d

# Preverite status
docker-compose ps

# Ogled logov
docker-compose logs -f [service-name]
Ročni Zagon Posamezne Storitve

# Book Service
cd services/book-service
npm install
npm run dev

# User Service
cd services/user-service
npm install
npm run dev

# Frontend
cd frontend
npm install
npm run dev
Dostop do Aplikacije

Frontend: http://localhost:3000
API Gateway: http://localhost:8080
Book Service: http://localhost:3001
User Service: http://localhost:3002
Landing Page Service: http://localhost:3003
Order Service: http://localhost:3004
Analytics Service: http://localhost:3005
Notification Service: http://localhost:3006
Produkcijski Zagon

# Build Docker images
docker-compose -f docker-compose.prod.yml build

# Deploy
docker-compose -f docker-compose.prod.yml up -d
Tehnologije

Backend: Node.js (Express/NestJS)
Frontend: React/Vue.js
Baze: PostgreSQL, MongoDB
Message Broker: RabbitMQ
API Gateway: Kong/Nginx
Containerization: Docker
Orchestration: Kubernetes (production)
CI/CD: GitHub Actions
Monitoring: Prometheus, Grafana
Prispevanje

Fork repozitorija
Ustvarite feature branch (git checkout -b feature/nova-funkcionalnost)
Commit spremembe (git commit -m 'Dodaj novo funkcionalnost')
Push na branch (git push origin feature/nova-funkcionalnost)
Odprite Pull Request
Scenariji

Avtor ali zaloznik se prijavi v sistem
Pritisne gumb za ustvarjanje nove knjige, "Create book"
Izpolni ime, naslov, opis, avtor, cena, isbn
Frontend poslje zahtevo POST /api/books
API Gateway zahtevo preusmeri na Book Service.
Book service preveri podatke in shrani jo v Book DB
Sistem vrne odgovor frontendu
Frontend prikaze novo ustvarjeno knjigo
Scenarij 2

Avtor ustvari landing page

Avtor izbere obstojeco knjigo
Klikne gumb za ustvarjanje landing page-a, "Create landing page"
Frontend poslje zahtevo POST /api/landing-pages
API Gateway zahtevo preusmeri na Landing Page Service
Landing page service shrani konfiguracijo strani in poveze bookID
Avtor klikne "Publish"
Sistem generira javni URL /landing/new-fantasy-novel
Scenarij 3

Bralec obisce stran

Bralec odrpe URL strani
Frontend poslje get api/landing-pages/id
Landing service vrne bookId in stran
Frontend poslje zahtevo get /api/books/id
Book service vrne podatke o knjigi
Frontend prikaze stran
Scenarij 4

Bralec odda narocilo

Klikne gumb "Naroci"
Izpolni ime, email in naslov
Frontend poslje post /api/orders
Order service preveri ali knjiga obstaja in shrani narocilo v OrdersDB
Order Service objavi event: order.placed v RabbitMQ
Notification Service prejme event in pošlje email.
scenarij 5

Avtor pregleda statistiko

Avtor odpre dashboard
Frontend poslje get /api/orders/book/id
order service vrne stevilo narocil ali statuse narocil
Frontednd prikaz osnovne statistike