# ProjectFlow

Collaborative project management for small teams. Create projects, manage memberships, assign tasks, track progress, and discuss work with comments. Authorization is enforced on the Spring Boot API, not only in the UI.

## Main features

- JWT login, registration (always `TEAM_MEMBER`), logout, and profile updates
- Four global roles: `ADMIN`, `PROJECT_MANAGER`, `TEAM_MEMBER`, `VIEWER`
- Project membership roles: `MANAGER`, `MEMBER`, `VIEWER`
- Project CRUD, search, filters, pagination, and progress
- Task CRUD, assignment, overdue flags, and optimistic locking
- Chronological comments with author/manager delete rules
- Role-aware dashboard statistics
- Admin user search, create, activate/deactivate
- Springdoc OpenAPI UI and Actuator health

## Technology stack

- Frontend: React 18, Vite, TypeScript, Tailwind CSS, React Router, Axios, Lucide React
- Backend: Java 21, Spring Boot 3.3, Maven, Spring Web, Data JPA, Security, Validation, Actuator
- Database: MySQL 8, Flyway, Hibernate (`ddl-auto=validate`)
- Tests: JUnit 5, MockMvc, Spring Boot Test, Vitest

## Architecture

The browser talks to `/api` on the Spring Boot server. In development, Vite proxies `/api` to `http://localhost:8080`. Persistence is MySQL; Flyway owns the schema. See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for roles, JWT behavior, and optimistic locking.

## Folder structure

```text
client/     React SPA
server/     Spring Boot API
docs/       Architecture and API notes
docker-compose.yml   Optional local MySQL 8
.env.example         Environment template (no secrets)
```

## Prerequisites

- Java 21 LTS
- Maven 3.9+
- Node.js 20+ and npm
- MySQL 8.x (or Docker for the optional Compose file)
- Git

This workspace did not originally include Java, Maven, MySQL, Docker, or GitHub CLI. Temurin JDK 21 can be installed with:

```powershell
winget install --id EclipseAdoptium.Temurin.21.JDK -e --accept-source-agreements --accept-package-agreements
```

Install Maven from https://maven.apache.org/download.cgi and add it to `PATH`. Open a **new** PowerShell window after installing JDK so `java -version` works.

## MySQL installation and configuration

1. Install MySQL 8 from https://dev.mysql.com/downloads/mysql/ or use Docker Compose:

```powershell
docker compose up -d
```

2. Create the database and a dedicated user (replace the password). In the MySQL client:

```sql
CREATE DATABASE projectFlow_springBoot CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'projectflow_user'@'localhost' IDENTIFIED BY 'replace_me';
GRANT ALL PRIVILEGES ON projectflow.* TO 'projectflow_user'@'localhost';
FLUSH PRIVILEGES;
```

Do not reuse a personal password in committed files. This repository never stores your real MySQL password.

## Environment variables

Copy `.env.example` and set values in your shell or a local `.env` file that you **do not commit**.

```powershell
Copy-Item .env.example .env
```

| Variable | Purpose |
| --- | --- |
| `DB_HOST` | MySQL host (`localhost`) |
| `DB_PORT` | MySQL port (`3306`) |
| `DB_NAME` | Database name (`projectflow`) |
| `DB_USERNAME` | Database user |
| `DB_PASSWORD` | Database password |
| `JWT_SECRET` | At least 32 characters; used to sign tokens |
| `JWT_EXPIRATION_MS` | Token lifetime (default 86400000) |
| `FRONTEND_URL` | CORS origin (`http://localhost:5173`) |
| `SERVER_PORT` | API port (`8080`) |
| `SPRING_PROFILES_ACTIVE` | `dev` locally; `prod` must not seed demo admins |
| `COOKIE_SECURE` | `true` when serving HTTPS |
| `VITE_API_BASE_URL` | Leave empty when using the Vite proxy |

Generate a JWT secret:

```powershell
-join ((1..48) | ForEach-Object { Get-Random -InputObject ([char[]](48..57 + 65..90 + 97..122)) })
```

Then, in the same PowerShell session:

```powershell
$env:DB_HOST="localhost"
$env:DB_PORT="3306"
$env:DB_NAME="projectFlow_springBoot"
$env:DB_USERNAME="projectflow_user"
$env:DB_PASSWORD="replace_me"
$env:JWT_SECRET="paste_the_generated_secret_here"
$env:FRONTEND_URL="http://localhost:5173"
$env:SERVER_PORT="8080"
$env:SPRING_PROFILES_ACTIVE="dev"
```

## Database migrations

Flyway runs automatically when the server starts (`server/src/main/resources/db/migration/V1__init_schema.sql`). Hibernate then **validates** the schema; it does not create it.

To migrate without starting the web UI, start the Spring Boot app once against an empty `projectflow` database.

## Backend setup and commands

```powershell
cd C:\Users\arvin\Downloads\projectFlow-SpringBoot\server
mvn test
mvn package
mvn spring-boot:run
```

API: `http://localhost:8080`  
Swagger UI: `http://localhost:8080/swagger-ui.html`  
Health: `http://localhost:8080/actuator/health`

## Frontend setup and commands

```powershell
cd C:\Users\arvin\Downloads\projectFlow-SpringBoot\client
npm install
npm run dev
```

App: `http://localhost:5173`

Other commands:

```powershell
npm run test
npm run lint
npm run build
```

## Demo users (development only)

Seeded only when `SPRING_PROFILES_ACTIVE=dev` **and** the `users` table is empty. They are **not** created on the `prod` profile. Do not use these passwords outside local development.

| Role | Email | Password |
| --- | --- | --- |
| ADMIN | `admin@projectflow.dev` | `Admin123!` |
| PROJECT_MANAGER | `manager@projectflow.dev` | `Manager123!` |
| TEAM_MEMBER | `member@projectflow.dev` | `Member123!` |
| VIEWER | `viewer@projectflow.dev` | `Viewer123!` |

## API documentation

See [docs/API.md](docs/API.md) and the OpenAPI UI at `/swagger-ui.html`.

## Testing

Backend tests use an in-memory H2 database in MySQL compatibility mode (`application-test.yml`). They do **not** require a running MySQL instance.

```powershell
cd C:\Users\arvin\Downloads\projectFlow-SpringBoot\server
mvn test
```

Frontend:

```powershell
cd C:\Users\arvin\Downloads\projectFlow-SpringBoot\client
npm test
npm run build
```

MySQL-backed startup is a local run of `mvn spring-boot:run` after you create the database. That step cannot pass until MySQL is installed and `DB_*` variables match your server.

## Git and GitHub workflow

```powershell
cd C:\Users\arvin\Downloads\projectFlow-SpringBoot
git init
git checkout -b main
git add .
git status
git commit -m "feat: add ProjectFlow Spring Boot and React application"
```

If GitHub CLI is installed and authenticated:

```powershell
gh auth status
gh repo create ProjectFlow --private --source=. --remote=origin --push
```

If `gh` is missing, create a **private** empty repository named `ProjectFlow` on GitHub (do not add a remote README), then:

```powershell
git remote add origin https://github.com/<your-username>/ProjectFlow.git
git push -u origin main
```

Sign in to GitHub when Git prompts for credentials. Do not force-push.

## Security decisions

- Passwords are hashed with BCrypt. Plaintext passwords are never stored or logged.
- JWT secrets come from `JWT_SECRET`. Change the development default before any shared environment.
- CORS allows only `FRONTEND_URL`.
- Controllers return DTOs, not JPA entities.
- Authorization is checked in services for every project, task, member, and comment operation.
- Public registration cannot create `ADMIN`.
- Logout clears the HttpOnly cookie and client `sessionStorage`. **It does not revoke the JWT on the server.** A copied token remains valid until expiry. Keep expirations short and treat XSS as a token-theft risk for the `sessionStorage` fallback.

## Optimistic locking

Each task has a `version` column mapped with JPA `@Version`. Clients send the version they read. A mismatch returns **HTTP 409** (`STALE_TASK_VERSION`). The UI warns, reloads the latest task, and lets the user retry. An integration test covers stale updates.

## Known limitations

- Java/Maven/MySQL/GitHub CLI may still need to be installed on a fresh machine.
- JWTs are stateless; logout is client/cookie clearing, not a server denylist.
- Demo accounts exist only in the `dev` profile.
- Cross-origin cookie auth depends on same-site localhost behavior or the Vite proxy.
- Optional Docker Compose requires Docker Desktop, which is not assumed.

