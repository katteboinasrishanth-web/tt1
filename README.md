# tt1 · Restaurant Table Reservation System

Full-stack reservation system built exactly to the assigned flow:

```
Customer → Frontend (React/Vite) → Backend API (Spring Boot) → Database (MySQL) → Backend → Frontend
```

## Tech Stack
| Layer     | Technology                       |
|-----------|----------------------------------|
| Frontend  | React 18 + Vite (http://localhost:5173) |
| Backend   | Java 24 / Spring Boot 3.5 (http://localhost:8080/api) |
| Database  | MySQL (`restaurant_db`)          |

## Project Layout
```
tt1/
├─ backend/                  Spring Boot (Maven) API
│  ├─ src/main/java/com/restaurant/reservation/
│  │  ├─ config/             CORS config not needed (@CrossOrigin used) + DataSeeder
│  │  ├─ model/              User, RestaurantTable, Reservation, Admin, Occasion, ReservationTable
│  │  ├─ repository/         Spring Data JPA repositories
│  │  ├─ service/            Auth, Tables, Reservations, Admin, Combo, DiningWindow, TimeSlot
│  │  ├─ controller/         REST controllers
│  │  ├─ dto/                request/response objects
│  │  └─ exception/          global error handling
│  └─ src/main/resources/application.properties
├─ frontend/                 React + Vite SPA
│  └─ src/pages/             Login, Register, Home, MyReservations, Admin
├─ database/restaurant_db.sql   optional manual schema + seed script
├─ start-backend.bat         double-click to run the API
└─ start-frontend.bat        double-click to run the frontend
```

## API Endpoints
| Method | Endpoint                                 | Notes |
|--------|------------------------------------------|-------|
| POST   | `/api/auth/register`                     | Customer registration → token |
| POST   | `/api/auth/login`                        | Customer login → token |
| POST   | `/api/admin/auth/login`                  | Admin login → token |
| GET    | `/api/tables/available?date&time&numberOfPeople` | Step 2: check availability |
| GET    | `/api/tables/combos?date&time&numberOfPeople[&occasion]` | Seating suggestions (single + multi-table combos) |
| POST   | `/api/reservations`                      | Step 3: book (auth). Accepts `tableId` or `tableIds`, supports `occasion` + `occasionNote` |
| GET    | `/api/reservations/mine`                 | Your bookings (auth) |
| DELETE | `/api/reservations/mine/{id}`            | Cancel own booking (auth) |
| GET    | `/api/tables`                            | All tables (public) |
| POST   | `/api/admin/tables`                      | Add table (admin) |
| PUT    | `/api/admin/tables/{id}`                 | Update table (admin) |
| DELETE | `/api/admin/tables/{id}`                 | Delete table (admin) |
| GET    | `/api/admin/reservations`                | All bookings (admin) |
| GET    | `/api/admin/reservations/date?date`       | Bookings by date (admin) |
| PUT    | `/api/admin/reservations/{id}/status`    | Confirm / Cancel / Complete (admin) |
| GET    | `/api/admin/prep-board[?date]`            | Prep board (admin) — ordered by prep-ready time (slot - buffer) |

Requests that require auth send `Authorization: Bearer <token>`.

## How To Run (Windows)

Prerequisites: MySQL running, Java 17+, Node 18+.

1. **Database** — automatic. The backend connects to `root`/**`root`** on
   `localhost:3306`, auto-creates `restaurant_db`, builds tables and seeds:
   - 8 tables (capacity 2–10), zones `WINDOW/MAIN/PRIVATE` (tables in same zone can be joined)
   - default admin: **sri / sri123**
   > Change the password in `backend/src/main/resources/application.properties`
   > if your MySQL `root` uses something else. A manual script is also at
   > `database/restaurant_db.sql`.

2. **Backend** — double-click `start-backend.bat` (or):
   ```
   cd backend
   mvn package -DskipTests
   java -jar target\reservation-system-1.0.0.jar
   ```
   Wait until: `Started ReservationSystemApplication ... port 8080`.

3. **Frontend** — double-click `start-frontend.bat` (or):
   ```
   cd frontend
   npm install
   npm run dev
   ```
   Open **http://localhost:5173**.

## New Features (Unique)

- **Multi-table combo booking**: Large parties can be seated by joining two tables of the **same zone** into one big table. `/api/tables/combos` ranks both single and combo options by fewest wasted seats, and `POST /api/reservations` accepts `tableIds` (length 1 or 2).
- **Occasion mode + setup buffer**: Support for `NONE`, `BIRTHDAY`, `ANNIVERSARY`, `DATE_NIGHT`, `BUSINESS`, `FAMILY`, `OTHER`. Each occasion applies a setup buffer (e.g. BIRTHDAY/ANNIVERSARY = 15 min) before the slot. Prep-ready times are computed as `slot - buffer`.
- **Prep board**: Admin can view `/api/admin/prep-board?date=YYYY-MM-DD` — a task list ordered by prep-ready time showing which tables/combo, guest info, occasion note and buffer.

## How the Flow Matches the Design
- **Registration/Login** — `AuthController` validates credentials against the `users` table and returns a token.
- **Check Table Availability & Best Fit** — `/api/tables/combos` checks for window conflicts (including the occasion setup buffer and the dining window) and returns ranked single/combo suggestions.
- **Make Reservation** — at booking time, the system re-checks availability of all selected tables (and the full window) before saving, rejecting double-bookings with `409 Conflict`. Combos are persisted as multiple `reservation_tables` rows (one per table) with the biggest table as the anchor.
- **Admin** — Table CRUD, reservation status management, and a prep board for operations (occasion alerts + setup timing).
