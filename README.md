# POwerApp

POwerApp é um blueprint de arquitetura corporativa com Quarkus (Java 17) + Angular 17 + PostgreSQL + Liquibase e ambiente totalmente dockerizado. Este README detalha o papel de cada diretório para facilitar a navegação e evolução do projeto.

## Estrutura geral

```
backend/    -> API Quarkus com camadas de domínio, aplicação e infraestrutura
frontend/   -> Angular 17 + Material, guard e interceptor JWT
database/   -> Observações sobre versionamento de banco (Liquibase)
docker/     -> Docker Compose para subir stack completa
shared/     -> DTOs/contratos compartilháveis entre frontend e backend
```

### Backend (Quarkus)

- `backend/pom.xml`: dependências do projeto (Quarkus RESTEasy Reactive, Panache, JWT, Liquibase, etc.).
- `backend/src/main/java/com/powerapp/model`: entidades de domínio persistidas.
- `backend/src/main/java/com/powerapp/repository`: repositórios Hibernate Panache que expõem acesso ao banco.
- `backend/src/main/java/com/powerapp/dto`: objetos de entrada/saída usados pelos recursos REST.
- `backend/src/main/java/com/powerapp/application`: camada de aplicação.
  - `.../usecase`: orquestra casos de uso (por exemplo, cálculos de capacidade e previsão).
  - `.../service`: serviços de aplicação que coordenam repositórios e integrações.
  - `.../port`: contratos para integrações externas (ex.: Jira).
  - `.../mapper`: mapeamentos entre entidades e DTOs.
- `backend/src/main/java/com/powerapp/resource`: controladores REST que expõem endpoints (auth, sprints, feriados, forecast, time, Jira).
- `backend/src/main/java/com/powerapp/security`: classes de autenticação/autorizações JWT, gerenciando tokens e claims.
- `backend/src/main/java/com/powerapp/jira` e `backend/src/main/java/com/powerapp/infrastructure/jira`: clientes e modelos para integração com Jira.
- `backend/src/main/resources/application.properties`: configuração da aplicação (datasource, JWT, chaves Jira, etc.).
- `backend/src/main/resources/db/changelog/db.changelog-master.yaml`: changelog Liquibase aplicado na subida do serviço.
- `backend/src/main/resources/META-INF/resources/privateKey.pem` e `publicKey.pem`: chaves JWT para ambiente local (trocar em produção).

### Frontend (Angular)

- `frontend/angular.json` e `package.json`: configuração de build e dependências do Angular 17 + Material.
- `frontend/src/app/app-routing.module.ts`: roteamento com guard de autenticação.
- `frontend/src/app/core`: serviços e infraestrutura do app (auth service, guard, interceptor de API/JWT).
- Features:
  - `frontend/src/app/auth`: telas de login/registro.
  - `frontend/src/app/dashboard`: visão geral após login.
  - `frontend/src/app/holidays`: CRUD de feriados.
  - `frontend/src/app/sprints`: capacidade e planejamento de sprints.
  - `frontend/src/app/epics`: progresso de épicos via Jira.
  - `frontend/src/app/domain-cycles`: visão de domain cycles.
  - `frontend/src/app/team`: gestão de time e alocação.
  - `frontend/src/app/project-config`: configurações adicionais de projeto.
- `frontend/src/environments`: URLs e variáveis por ambiente (`environment.ts` e `environment.prod.ts`).
- Desenvolvimento: dentro de `frontend/`, executar `npm install` e `npm start` para servir em `http://localhost:4200`.

### Database

- Versionamento oficial via Liquibase está em `backend/src/main/resources/db/changelog/db.changelog-master.yaml`.
- O diretório `database/` pode receber documentação ou artefatos auxiliares de banco. Veja `database/README.md`.

### Docker

- `docker/docker-compose.yml`: sobe Postgres, PgAdmin, backend e frontend. Útil para validar a stack completa localmente.
- Copie `.env.example` para `.env` na raiz antes de subir (configura DB, JWT e chaves Jira).

### Shared

- Espaço para DTOs, contratos ou schemas comuns que possam ser consumidos por backend e frontend. Veja `shared/README.md`.

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
