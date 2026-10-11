# Deploying HOTELIER on Render

Three pieces, created in this order: **PostgreSQL database → Java backend (Web Service) → React frontend (Static Site)**.
Menu names on Render's dashboard can change slightly; if a label differs, look for the closest match and check https://render.com/docs.

> **Before you start:** the code must be on GitHub (see `GITHUB_WINDOWS.md`) with `backend/` and `frontend/` at the top level of the repo.
> **Important:** the Spring Boot layer of this project was written and compile-checked but **never started against a real Spring/Maven environment before delivery**. The first Render build is therefore the first real run. If it fails, copy the red lines from the Render **Logs** and send them to me – most fixes are one-line changes.

## Free-tier facts you must know (as documented by Render, checked October 2026)

- A **free web service sleeps after ~15 minutes** without traffic and needs **about a minute** to wake up. Open the backend URL before a demo.
- A **free PostgreSQL database expires 30 days after creation** (then a 14-day grace period, then deletion) and has no backups. Create it close to your viva date, or upgrade it, or export the data.
- Free web services have **512 MB RAM**. The Dockerfile limits Java's memory to fit, but a Spring Boot app is a tight fit; if logs show "Out of memory" you need a paid instance (a few US dollars per month).
- Free services have a monthly instance-hour allowance shared across your workspace.
- The service's local disk is **temporary** – invoice `.txt` copies saved on the server disappear on restart. (Downloads to the user's PC still work.)
- Static sites (the React frontend) are free and do not sleep.
- Render needs a card on file for some account types; read the prompts rather than assuming.

## Step 1 – Create the database

1. Render dashboard → **New +** → **PostgreSQL**.
2. Name `hotelier-db`. Choose a **region** (remember it – use the same one for the backend). Instance type **Free**.
3. Create. When it shows *Available*, open it and copy the **Internal Database URL** (starts with `postgresql://`). Use *Internal*, not *External*, for the backend – it is faster and stays inside Render's network (it only works from services in the same region).

## Step 2 – Deploy the Java backend

1. **New +** → **Web Service** → connect your GitHub account → choose the `hotelier` repository.
2. Settings:

| Setting | Value |
|---|---|
| Name | `hotelier-api` (becomes `https://hotelier-api.onrender.com`; yours may differ) |
| Region | same as the database |
| Branch | `main` |
| Root Directory | `backend` |
| Language / Runtime | **Docker** (Render has no native Java runtime, so Docker is used) |
| Dockerfile Path | leave the default (`./Dockerfile`). If the log says it cannot find the Dockerfile, try `backend/Dockerfile` |
| Instance Type | Free |
| Health Check Path | `/api/health` |

3. **Environment variables** (Advanced → Add Environment Variable):

| Key | Value |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `postgres` |
| `DATABASE_URL` | the **Internal Database URL** you copied |
| `HOTELIER_TIMEZONE` | `Asia/Kolkata` |
| `CORS_ALLOWED_ORIGINS` | add later (Step 4); for now `http://localhost:5173` |

   You do **not** set `PORT` – Render provides it and the app reads it. You do not need to build the JDBC URL – `JdbcUrlConverter` does it.
4. **Create Web Service**. The first build downloads Maven dependencies and takes several minutes. Watch **Logs**. Success looks like `Started HotelierApplication` and `Sample data loaded.`
5. Test in a browser: `https://YOUR-BACKEND.onrender.com/api/health` → `{"status":"UP",...}` and `/api/rooms` → a JSON list of 10 rooms.

If `SPRING_PROFILES_ACTIVE=postgres` is forgotten, the app silently uses an internal H2 file database that **is wiped on every restart**. Always confirm the log line contains `The following 1 profile is active: "postgres"`.

## Step 3 – Deploy the React frontend

1. **New +** → **Static Site** → same repository.
2. Settings:

| Setting | Value |
|---|---|
| Root Directory | `frontend` |
| Build Command | `npm install && npm run build` |
| Publish Directory | `dist` |

3. Environment variable: `VITE_API_URL` = your backend URL, e.g. `https://hotelier-api.onrender.com` (**no trailing slash**, **no `/api`**).
   This variable is read at **build** time and baked into the site. If you change it later, trigger a new deploy of the static site.
4. Create. When done, copy the site's URL (e.g. `https://hotelier.onrender.com`).

The app uses `#` URLs (like `.../#/rooms`) so refreshing any page works without extra rewrite rules.

## Step 4 – CORS (connect the two)

Browsers block a website from calling an API on a different address unless the API allows it.

1. Backend service → **Environment** → set `CORS_ALLOWED_ORIGINS` to the exact frontend URL, e.g. `https://hotelier.onrender.com` (no trailing slash; several URLs can be comma-separated).
2. Save – Render redeploys the backend.
3. Open the frontend URL. The dashboard should show live data. If the browser console (F12) shows a "CORS" error, the value is wrong or the redeploy has not finished.

## Step 5 – Verify the full workflow

1. Dashboard shows 10 rooms.
2. Add a guest → book a room → price shown → check in → check out → invoice → print.
3. Reload the page – data is still there.
4. Backend → **Manager → Restart** (or wait for a redeploy), reload – data is still there (this proves the Postgres database is used).

## Health checks and updates

- `/api/health` is the health endpoint; Render uses it for the health check setting above.
- Pushing to `main` redeploys automatically (auto-deploy is the default).

## Troubleshooting

| Symptom | Cause / fix |
|---|---|
| Frontend shows "Cannot reach the server" | Backend is asleep (wait ~1 min and retry), or `VITE_API_URL` is wrong. Check it, then redeploy the static site |
| Browser console: CORS error | `CORS_ALLOWED_ORIGINS` missing/typo/trailing slash/old URL |
| Backend log: `Connection refused` / `UnknownHostException` | Used the wrong database URL (use *Internal*), or database and service are in different regions, or the database expired |
| Backend log: `DATABASE_URL` not resolvable / placeholder error | Variable name misspelt or missing |
| Backend log: `password authentication failed` | URL copied from an old/deleted database |
| Build fails in Maven | Copy the first `ERROR` lines from the log and send them to me |
| Build killed / "Out of memory" | Free 512 MB is too small; upgrade the instance type |
| Data disappears after restart | Profile `postgres` not active (see Step 2 warning) |
| Sample data missing | `SEED_DEMO_DATA=false` set, or the database already had rooms |
| Dates behave oddly around midnight | `HOTELIER_TIMEZONE` not set to your zone |
| Frontend build fails | Root Directory must be `frontend`; build command exactly `npm install && npm run build` |

## Costs (honest summary)

Everything above can run at **$0** within the free limits, with the sleep delay and the 30-day database expiry. Paid options on Render exist for always-on services and permanent databases; prices change, so check https://render.com/pricing before upgrading.
