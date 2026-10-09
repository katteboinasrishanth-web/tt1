# Deployment Guide — tt1 Restaurant Reservation System

Architecture:

```
Browser → Vercel (React SPA) → /api proxy → Render (Spring Boot Docker) → Aiven MySQL
```

## Components
| Part | Host | Notes |
|------|------|-------|
| Frontend | Vercel | `frontend/` (Vite). Root Directory = `frontend`. |
| Backend | Render | Docker web service from `backend/Dockerfile`. |
| Database | Aiven MySQL | Database `restaurant_db`. |

## 1. Database (Aiven MySQL)
- Server: `tt1-katteboinasrishanthya-ff69.i.aivencloud.com`, port `20037`, SSL required.
  - Use **20037** (classic MySQL port). Do NOT use 20041 — that is the X Protocol port.
- Database: `restaurant_db` (already created).
- User: `avnadmin` (password from the Aiven console).
- Tables are auto-created by Hibernate (`ddl-auto=update`) and the default
  admin (`sri` / `sri123`) plus 8 tables are seeded on first start.

## 2. Backend (Render)
Create a **Web Service** (Docker) from this repo:
- Root/Docker context: `backend/`
- Dockerfile: `backend/Dockerfile`
- Health check path: `/api/tables`
- Environment variables:
  | Key | Value |
  |-----|-------|
  | `SPRING_DATASOURCE_URL` | `jdbc:mysql://tt1-katteboinasrishanthya-ff69.i.aivencloud.com:20037/restaurant_db?sslMode=REQUIRED&allowPublicKeyRetrieval=true&serverTimezone=UTC` |
  | `SPRING_DATASOURCE_USERNAME` | `avnadmin` |
  | `SPRING_DATASOURCE_PASSWORD` | *(Aiven password — keep secret)* |

Or use the blueprint: **New → Blueprint**, pick this repo (`render.yaml`).

The service listens on Render's `PORT` automatically (`server.port=${PORT:8080}`).

## 3. Frontend (Vercel)
- Import this repo, set **Root Directory** to `frontend` (Vite preset).
- Build command `npm run build`, output `dist`.
- `frontend/vercel.json` proxies `/api/*` to the Render backend and rewrites
  all other paths to `index.html` (SPA routing), so no CORS config is needed.

## Notes
- Render free tier sleeps after ~15 minutes idle; the first request may take ~50s.
- Any push to `main` redeploys both Render and Vercel.
