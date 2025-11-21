# POwerApp

POwerApp é um blueprint de arquitetura corporativa com Quarkus (Java 17) + Angular 17 + PostgreSQL + Liquibase e ambiente totalmente dockerizado.

## Estrutura

```
backend/   -> Quarkus REST API (JWT, Hibernate Panache, Liquibase)
frontend/  -> Angular 17 + Material + JWT guard/interceptor
database/  -> Changelog Liquibase (incluído em backend/resources)
docker/    -> docker-compose para stack completa
shared/    -> Espaço para DTOs/schemas comuns
```

## Subir em Docker

1. Copie `.env.example` para `.env` e ajuste variáveis (DB, JWT, JIRA).
2. Na pasta `docker/` execute: `docker compose up --build`
3. Serviços:
   - API: `http://localhost:8080`
   - Frontend: `http://localhost/`
   - Postgres: porta `5432`
   - PgAdmin: `http://localhost:5050`

## Backend (Quarkus)

- Endpoints principais:
  - Auth: `POST /auth/register`, `POST /auth/login`, `GET /auth/me`, `POST /auth/settings`
  - Feriados: `POST/GET/DELETE /holidays`
  - Sprints: `POST/GET /sprints`, `GET /sprints/{id}`, `GET /sprints/{id}/capacity`, `GET /sprints/{id}/unplanned`
  - Forecasts: `GET /forecast/sprint/{id}`, `GET /forecast/epic/{epicKey}`, `GET /forecast/dc/{dcId}`, `GET /forecast/alerts/epics/{dcId}`
  - Jira progress: `GET /epics/{epicKey}`
  - Time: `POST/GET/PUT/DELETE /team`
- Liquibase: `backend/src/main/resources/db/changelog/db.changelog-master.yaml`
- Config: `backend/src/main/resources/application.properties`
- JWT dev keys: `backend/src/main/resources/META-INF/resources/privateKey.pem` e `publicKey.pem` (trocar em produção).

## Frontend (Angular)

- Rotas protegidas por guard e interceptor de JWT.
- Telas iniciais: login, registro, dashboard, feriados, sprints (capacidade), épicos (progresso Jira), domain cycles, time.
- Ajuste `environment.ts` para apontar para a URL do backend.
- Dev server: `npm install && npm start` dentro de `frontend/`.

## Próximos passos sugeridos

- Completar integração real com Jira API no serviço `JiraService`.
- Expandir cálculos de capacidade/forecast conforme regras do PDF do domínio.
- Adicionar testes unitários e collection de OpenAPI gerada automaticamente pelo Quarkus.
