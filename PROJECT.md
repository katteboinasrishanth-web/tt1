# tt1 — Restaurant Table Reservation System (Project Summary)

## About Project
**tt1** is a full-stack restaurant table reservation system following the assigned flow:

```
Customer → Frontend (React/Vite) → Backend API (Spring Boot) → Database (MySQL) → Backend → Frontend
```

Key features:
- Customer registration & login (session tokens)
- Check table availability and get best-fit seating suggestions (single tables **and** multi-table "combos" where two same-zone tables are joined for large parties)
- Book a reservation with **occasion** support (birthday, anniversary, etc.) that applies a setup buffer before the slot
- View / cancel your own reservations
- Admin: table CRUD, reservation status management, and a **prep board** ordered by prep-ready time

### Tech Stack
| Layer | Technology |
|-------|------------|
| Frontend | React 18 + Vite + React Router |
| Backend | Java 24 / Spring Boot 3.5 (Maven), Spring Data JPA |
| Database | MySQL (hosted on Aiven, SSL-secured) |
| Deployment | Vercel (frontend), Render Docker (backend), Aiven (MySQL) |

## Frontend (GitHub Link)
https://github.com/katteboinasrishanth-web/tt1/tree/main/frontend

## Backend (GitHub Link)
https://github.com/katteboinasrishanth-web/tt1/tree/main/backend

## Final Project Deployment
- **Live Website (Frontend):** https://tt1-bice.vercel.app
- **Live API (Backend):** https://tt1-backend-74dk.onrender.com/api
- **Database:** Aiven MySQL, database `restaurant_db` (SSL, credentials in Render env vars)
- **Source Repository:** https://github.com/katteboinasrishanth-web/tt1

### Deployment Details
- Frontend is hosted on **Vercel** (Root Directory = `frontend`); `/api/*` is proxied to the Render backend via `frontend/vercel.json`, so no CORS configuration is required.
- Backend is built as a **Docker** image (`backend/Dockerfile`, Java 24) and deployed on **Render** (free web service).
- Database connection details are supplied via Render environment variables
  (`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`).
- Tables are auto-created by Hibernate (`ddl-auto=update`); the default admin (`sri` / `sri123`) and 8 tables are seeded on first start.

## API Endpoints
| Method | Endpoint | Notes |
|--------|----------|-------|
| POST | `/api/auth/register` | Customer registration → token |
| POST | `/api/auth/login` | Customer login → token |
| POST | `/api/admin/auth/login` | Admin login → token |
| GET | `/api/tables` | All tables (public) |
| GET | `/api/tables/available?date&time&numberOfPeople` | Availability check |
| GET | `/api/tables/combos?date&time&numberOfPeople[&occasion]` | Seating suggestions (single + combos) |
| POST | `/api/reservations` | Book (auth, `tableId` or `tableIds`, `occasion`) |
| GET | `/api/reservations/mine` | Your bookings (auth) |
| DELETE | `/api/reservations/mine/{id}` | Cancel own booking (auth) |
| POST/PUT/DELETE | `/api/admin/tables[/{id}]` | Table CRUD (admin) |
| GET | `/api/admin/reservations[?date]` | All bookings (admin) |
| PUT | `/api/admin/reservations/{id}/status` | Confirm / Cancel / Complete (admin) |
| GET | `/api/admin/prep-board[?date]` | Prep board (admin) |

## Local Development
Backend:
```cmd
cd backend
mvn spring-boot:run
```
Frontend:
```cmd
cd frontend
npm install
npm run dev
```
Open http://localhost:5173 (Vite proxies `/api` to http://localhost:8080).

Default admin: **sri / sri123**
