# ProjectFlow architecture

## Overview

ProjectFlow is a full-stack collaborative project management system:

- React + Vite frontend (`client/`)
- Spring Boot 3 REST API (`server/`)
- MySQL 8 with Flyway migrations
- JWT authentication and backend role-based authorization

```text
Browser (Vite :5173)
    |  HTTPS/HTTP + cookie or Bearer token
Spring Boot API (:8080 /api)
    |
MySQL 8 (projectFlow_springBoot)
```

In local development the Vite proxy forwards `/api` to `http://localhost:8080`, so the browser can treat the API as same-origin.

## Layers

1. Controllers accept validated DTOs and never return JPA entities.
2. Services enforce authorization, membership checks, and business rules.
3. Repositories isolate persistence.
4. Flyway owns schema. Hibernate `ddl-auto` is `validate`.

## Roles

### Global roles

| Role | Capabilities |
| --- | --- |
| ADMIN | All users, all projects, memberships, tasks, comments, dashboard |
| PROJECT_MANAGER | Create projects; manage owned or MANAGER-membership projects |
| TEAM_MEMBER | Access member projects; limited task updates; comments |
| VIEWER | Read-only access to projects they belong to |

Public registration always creates `TEAM_MEMBER`. It never grants `ADMIN`.

### Project membership roles

| Role | Capabilities |
| --- | --- |
| MANAGER | Edit project, members, tasks, comments |
| MEMBER | View; comment; if assigned, update task **status** only |
| VIEWER | Read-only inside that project |

### Precedence

1. Inactive users cannot authenticate.
2. Global `ADMIN` bypasses project membership.
3. Project **owner** is treated as a project `MANAGER`.
4. Global `VIEWER` remains read-only even on projects they join unless they are `ADMIN`.
5. Project `VIEWER` membership is read-only for that project.
6. Object IDs are always checked against membership. Knowing a project or task ID is not enough (IDOR protection).

## Authentication

Login and register:

- Hash passwords with BCrypt.
- Issue a signed JWT containing user id, email, and global role.
- Set an **HttpOnly** `pf_access_token` cookie (`SameSite=Lax`, `Secure` in production).
- Also return `accessToken` in the JSON body so API tests and the SPA can send `Authorization: Bearer`.

The SPA stores the Bearer token in `sessionStorage` as a fallback when the cookie is not sent (cross-port calls without the Vite proxy). `sessionStorage` is readable by JavaScript, so XSS in the SPA could steal it. Prefer the Vite proxy + HttpOnly cookie in development, and HTTPS + `Secure` cookies in production.

Logout clears the HttpOnly cookie and drops the client-side token. **A still-valid JWT is not revoked on the server.** Stateless JWTs remain usable until they expire if an attacker already copied the token. Mitigations: short expiry (`JWT_EXPIRATION_MS`), HTTPS, HttpOnly cookies, and rotating the signing secret if compromise is suspected.

## Optimistic concurrency

`Task.version` is a JPA `@Version` column.

1. Clients read the task, including `version`.
2. Updates must send that `version`.
3. If it does not match the current row, the API returns **HTTP 409** with `code: STALE_TASK_VERSION`.
4. Hibernate optimistic locking also rejects lost updates if two transactions race.
5. The UI shows a conflict message, reloads the latest task, and lets the user retry.

This is enforced on the server. The frontend only surfaces the conflict.

## Timestamps

Entity timestamps use `Instant` stored in UTC (`hibernate.jdbc.time_zone=UTC`).

## Cascades

- Deleting a project cascades to memberships, tasks, and comments.
- Users are not cascade-deleted from projects or comments.
- Duplicate `(project_id, user_id)` memberships are rejected by a unique constraint.

