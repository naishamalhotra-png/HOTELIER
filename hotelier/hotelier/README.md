# HOTELIER – Hotel Management System

A Java OOPS college mini-project: a Spring Boot backend that demonstrates core OOPS concepts through a real hotel booking workflow, and a React + Tailwind frontend in a warm boutique-hotel style (ivory, forest green, restrained gold).

## Features

Dashboard with live numbers · Room management (add / edit / search / filter) · Guest management (unique guest IDs) · Bookings (date validation, capacity check, overlap prevention, Java-calculated price, cancel) · Check-in / check-out with room-availability updates · Itemised invoice with 12% GST, print view and `.txt` export · Simulated payment status · Persistent database · Fictional sample data.

## Tech stack

Java 17+ · Spring Boot 3.5.5 · Spring Data JPA · Maven · PostgreSQL (deployed) / H2 (local default) · React 18 · TypeScript · Vite 5 · Tailwind CSS 4 · Lucide icons · Docker (for Render) · GitHub

## Project structure

```
hotelier/
├── backend/                  Java + Spring Boot (Maven)
│   ├── Dockerfile            used by Render
│   ├── pom.xml
│   └── src/main/java/com/hotelier/
│       ├── model/            domain classes  (OOPS lives here)
│       ├── service/          business rules
│       ├── repository/       database access interfaces
│       ├── controller/       REST endpoints
│       ├── dto/              request/response shapes (records)
│       ├── exception/        custom exceptions + global handler
│       └── config/           CORS, clock, DB URL conversion, sample data
├── frontend/                 React + TypeScript + Vite + Tailwind
├── docs/                     OOPS map, viva guide, GitHub + deployment guides
├── .env.example
└── .gitignore
```

## Documents

| File | Contents |
|---|---|
| `docs/OOPS_MAP.md` | Every OOPS concept → file and method |
| `docs/VIVA_GUIDE.md` | Class diagram, dry run, demo script, 30 viva Q&A, limitations |
| `docs/GITHUB_WINDOWS.md` | Windows PowerShell upload to GitHub `main`, repository-detection troubleshooting |
| `docs/DEPLOYMENT.md` | Render: database, backend, frontend, env vars, CORS, health check, free-tier limits |

## What was actually verified (please read)

| Item | Status |
|---|---|
| Java domain layer (models, validator, invoice, URL converter) compiles with `javac --release 17` | ✅ Done |
| 31 JUnit 5 unit tests (OOPS behaviour, polymorphism, overloading, booking rules, state transitions, overlap, invoice maths, DB-URL conversion) | ✅ Run – all passed |
| Whole backend (all 55 main source files) compiled against **stand-in stubs** of the Spring/JPA annotations | ✅ Done – catches Java type errors only |
| Frontend `tsc` type-check + `vite build` (production) | ✅ Done |
| `mvn` build with real Spring Boot, Spring startup, JPA/Hibernate mapping, PostgreSQL connection, REST behaviour | ⚠️ **Not run** – the build environment had no access to Maven Central |
| `HotelierWorkflowIntegrationTest` (full API workflow on H2) | ⚠️ Written, **not run** |
| Docker image build, Render deployment, browser testing of the UI | ⚠️ **Not run** |

So the first real run of the Spring layer will happen on your machine or on Render. Run `mvn clean verify` in `backend/` if you ever can; otherwise send me the Render build/runtime log for any error.

## Run locally (optional)

Requires JDK 17+, Maven 3.9+, Node 20+.

```powershell
# terminal 1 – backend (uses an H2 file database in backend/data)
cd backend
mvn spring-boot:run

# terminal 2 – frontend
cd frontend
copy .env.example .env
npm install
npm run dev
```

Open http://localhost:5173. Backend health check: http://localhost:8080/api/health.

Tests: `cd backend && mvn test` · Frontend build: `cd frontend && npm run build`.

## Configuration (environment variables)

| Variable | Where | Meaning | Default |
|---|---|---|---|
| `SPRING_PROFILES_ACTIVE` | backend | `postgres` for production | H2 |
| `DATABASE_URL` | backend | Render's Internal Database URL | – |
| `CORS_ALLOWED_ORIGINS` | backend | Frontend address(es), comma-separated | `http://localhost:5173` |
| `HOTELIER_TIMEZONE` | backend | Time zone for "today" | `Asia/Kolkata` |
| `SEED_DEMO_DATA` | backend | Load sample data into an empty DB | `true` |
| `EXPORT_DIR` | backend | Folder for invoice `.txt` files | `exports` |
| `VITE_API_URL` | frontend (build time) | Backend address | `http://localhost:8080` |

## REST API

| Method & path | Purpose |
|---|---|
| `GET /api/health` | Health check |
| `GET /api/dashboard` | Live dashboard figures |
| `GET /api/staff` | Front-desk staff |
| `GET/POST /api/guests`, `GET/PUT /api/guests/{id}` | Guests (`?q=` search) |
| `GET/POST /api/rooms`, `GET/PUT /api/rooms/{id}` | Rooms (`?q=&type=&status=&minCapacity=`) |
| `GET /api/rooms/available?checkIn=&checkOut=&guests=` | Rooms free for given dates |
| `GET/POST /api/bookings`, `GET /api/bookings/{id}` | Bookings (`?status=&q=`) |
| `POST /api/bookings/{id}/cancel \| check-in \| check-out \| pay` | State changes |
| `GET /api/bookings/{id}/invoice`, `.../invoice/export` | Bill as JSON / downloadable `.txt` |

Errors come back as `{ "status": 409, "error": "Conflict", "message": "…" }`.

## Business rules (all in Java)

- Check-in cannot be in the past; check-out must be after check-in; maximum 30 nights.
- Guests ≤ room capacity. Maintenance rooms cannot be booked.
- Overlap rule: stays overlap when each starts before the other ends. Check-out day is free for the next guest. A row lock on the room inside a transaction prevents two simultaneous bookings.
- Price = nightly rate × nights (Standard = base, Deluxe = base × 1.15, Suite = base × 1.40). The nightly rate is stored on the booking so later price edits do not change old bookings.
- Status: Confirmed → Checked in → Checked out, or Confirmed → Cancelled. Check-in is allowed from the check-in date until before the check-out date, and only if the room is free. "Today" follows `HOTELIER_TIMEZONE`.
- Invoice = room charges + 12% GST (a constant in the `Billable` interface).

## Common problems

| Problem | Fix |
|---|---|
| `java -version` shows < 17 | Install JDK 17 or 21 |
| Port 8080 or 5173 in use | Stop the other program or change `PORT` / Vite port |
| Frontend shows "Cannot reach the server" | Backend not running, or `VITE_API_URL` wrong |
| CORS error in browser console | Set `CORS_ALLOWED_ORIGINS` to the exact frontend URL |
| Local data reset | Delete `backend/data` to start fresh (sample data reloads) |

All sample names, phone numbers and addresses are fictional.
