# ProjectFlow API

Base path: `/api`  
OpenAPI UI: `http://localhost:8080/swagger-ui.html`  
OpenAPI JSON: `http://localhost:8080/v3/api-docs`  
Health: `http://localhost:8080/actuator/health`

Authenticate with `Authorization: Bearer <token>` or the `pf_access_token` cookie.

Error body:

```json
{
  "timestamp": "2026-10-23T00:00:00Z",
  "status": 409,
  "error": "CONFLICT",
  "code": "STALE_TASK_VERSION",
  "message": "This task was updated by someone else. Reload the latest version and try again.",
  "path": "/api/tasks/1",
  "fieldErrors": []
}
```

## Auth

| Method | Path | Access |
| --- | --- | --- |
| POST | `/api/auth/login` | Public |
| POST | `/api/auth/register` | Public (always TEAM_MEMBER) |
| POST | `/api/auth/logout` | Authenticated |
| GET | `/api/auth/me` | Authenticated |
| PATCH | `/api/auth/me` | Authenticated |

Login body: `{ "email": "...", "password": "..." }`  
Register body: `{ "name": "...", "email": "...", "password": "..." }`

## Users

| Method | Path | Access |
| --- | --- | --- |
| GET | `/api/users` | ADMIN, PROJECT_MANAGER (search `q`, paging) |
| GET | `/api/users/{id}` | ADMIN, PROJECT_MANAGER, or self |
| POST | `/api/users` | ADMIN |
| PATCH | `/api/users/{id}` | ADMIN |
| DELETE | `/api/users/{id}` | ADMIN (deactivates; cannot remove last admin) |

## Projects

| Method | Path | Access |
| --- | --- | --- |
| GET | `/api/projects` | Members of accessible projects (`q`, `status`, paging) |
| GET | `/api/projects/{id}` | Admin, owner, or member |
| POST | `/api/projects` | ADMIN, PROJECT_MANAGER |
| PATCH | `/api/projects/{id}` | Admin, owner, or project MANAGER |
| DELETE | `/api/projects/{id}` | Admin, owner, or project MANAGER |

## Members

| Method | Path | Access |
| --- | --- | --- |
| GET | `/api/projects/{projectId}/members` | Project viewers |
| POST | `/api/projects/{projectId}/members` | Project managers |
| PATCH | `/api/projects/{projectId}/members/{memberId}` | Project managers |
| DELETE | `/api/projects/{projectId}/members/{memberId}` | Project managers (owner cannot be removed) |

## Tasks

| Method | Path | Access |
| --- | --- | --- |
| GET | `/api/projects/{projectId}/tasks` | Project viewers |
| POST | `/api/projects/{projectId}/tasks` | Project managers |
| GET | `/api/tasks/{id}` | Project viewers |
| PATCH | `/api/tasks/{id}` | Managers: full fields. Assigned MEMBER: status only. Requires `version`. |
| DELETE | `/api/tasks/{id}` | Project managers |

Stale `version` → `409 STALE_TASK_VERSION`.

## Comments

| Method | Path | Access |
| --- | --- | --- |
| GET | `/api/tasks/{taskId}/comments` | Project viewers |
| POST | `/api/tasks/{taskId}/comments` | Non-viewer members |
| PATCH | `/api/comments/{id}` | Author or ADMIN |
| DELETE | `/api/comments/{id}` | Author, project manager, or ADMIN |

## Dashboard

`GET /api/dashboard/stats` returns role-aware counts plus recent projects and tasks.

## Pagination

Spring `page`, `size`, `sort` query parameters. Responses use:

```json
{ "content": [], "page": 0, "size": 20, "totalElements": 0, "totalPages": 0 }
```
