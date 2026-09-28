# PetHotelGO — API

REST backend for a **pet hotel / pet boarding management system**: clients
(owners), their pets, boarding reservations with capacity control and
per-stay pricing, stay history, and vaccination cards. Consumed by a
companion React frontend ([`pethotelgo-frontend`](../pethotelgo-frontend)).

> This is a public, portfolio version of a private production repository.
> Infrastructure identifiers (real hosts, IPs, cloud credentials) have been
> removed or replaced with placeholders — everything else (code, config
> shape, migrations, docs) matches production.

## Stack

| | |
| --- | --- |
| Language / runtime | **Kotlin 2.2**, Java 17 |
| Framework | **Spring Boot 4** (Web, Security, Data JPA, Actuator) |
| Database | **MySQL 8+** |
| Migrations | **Flyway** (`src/main/resources/db/migration/`) |
| Auth | Self-issued **JWT** (HMAC-SHA256, `io.jsonwebtoken:jjwt`) — no external IdP |
| API docs | springdoc-openapi (Swagger UI, dev profile only) |
| Build | Maven (`./mvnw`) |
| Container | Docker (multi-stage-free: jar built outside the image) |

## Architecture

**Controller → Service → Repository**, with DTOs at the edges so JPA
entities never leave the service layer.

```
src/main/kotlin/com/api/pethotelgo/
├── controller/        REST endpoints (thin) — implement the interfaces in controller/api/
│   └── api/            @RequestMapping interfaces (double as the OpenAPI contract)
├── service/            business logic (interface) + service/impl/ (implementation)
├── repository/         Spring Data JPA repositories
├── model/
│   ├── entity/         @Entity classes (JPA)
│   ├── dto/             request/response payloads
│   └── enums/           PetSize, SociabilityLevel, ReservationStatus, UserRole
├── security/            JwtService (mints tokens) + JwtAuthenticationFilter (validates)
├── config/              WebSecurityConfig, SwaggerConfig
└── exception/           GlobalExceptionHandler + typed ErrorCode
```

Splitting the controller interface (`controller/api/*Api.kt`) from its
implementation keeps the OpenAPI annotations out of the controller body and
gives springdoc a clean contract to document, independent of the handler
logic.

### Authentication & security strategy

- **Access token** — short-lived JWT, sent as `Authorization: Bearer <token>`,
  subject = user id. Minted by `security/JwtService.kt`.
- `JwtAuthenticationFilter` re-reads the user from the database on every
  request (rather than trusting only the token claims), so deactivating an
  account takes effect immediately instead of waiting out the token's TTL.
- **Refresh token** — an opaque 256-bit random value (*not* a JWT); only its
  SHA-256 digest is persisted. Every `POST /auth/refresh` rotates it, and
  replaying an already-rotated token revokes the user's entire token family
  (classic refresh-token-reuse detection).
- **Passwords** — hashed with BCrypt. Emails normalized to lowercase before
  lookup/storage to avoid duplicate-account edge cases.
- **CORS** — `WebSecurityConfig` builds an explicit allow-list from
  `app.cors.allowed-origins`; when unset it falls back to
  `allowedOriginPatterns=*` rather than `allowedOrigins=*`, since Spring
  rejects a literal wildcard together with `allowCredentials=true`.
- **Public endpoints:** `/auth/register`, `/auth/login`, `/auth/refresh`,
  `/actuator/health` (+ Swagger UI, dev profile only). Everything else
  requires a valid bearer token and returns `401` (not a login-page redirect)
  otherwise — enforced via a custom `AuthenticationEntryPoint`.
- `app.jwt.secret` must decode to at least 32 bytes (HS256 requirement) or
  the application refuses to start. There is **no default in production** —
  `JWT_SECRET` is a mandatory environment variable.
- Stateless sessions (`SessionCreationPolicy.STATELESS`) — no server-side
  session store, so the API scales horizontally without sticky sessions.

### Database & migrations

Flyway owns the schema; Hibernate only *validates* against it
(`ddl-auto=validate`) in `dev` and `prod` — it never auto-generates DDL
outside local throwaway runs. On MySQL the type mapping is strict, so new
columns mirror what Hibernate expects:

| Kotlin / mapping | MySQL column |
| --- | --- |
| `@Enumerated(STRING)` | `ENUM('a','b',…)` (values in Hibernate's declared order) |
| `Boolean` | `BIT` |
| `LocalDateTime` / `Instant` | `DATETIME(6)` |
| `@Column(columnDefinition = "text")` | `TEXT` |
| `@Column(columnDefinition = "LONGTEXT")` | `LONGTEXT` (base64-encoded vaccination cards) |

Migrations are append-only (`V<n>__description.sql`) and applied
automatically on startup — a bad migration is fixed forward with a new file,
never edited in place.

### Domain highlights

- **Capacity-aware reservations** — the reservation service checks hotel
  capacity per day before confirming a booking.
- **Global settings, single source of truth** — `app_settings` is a
  single-row table (`GET`/`PUT /settings`) holding the boarding daily rates
  (`dailyRateStandard`, `dailyRateLarge`). `ReservationServiceImpl` uses it
  as the default whenever a reservation doesn't carry an explicit override,
  so a rate change never requires a deploy.
- **Vaccination cards** stored as `LONGTEXT` (base64), validated for size at
  the service layer with a friendly `413`-equivalent error rather than
  letting the container reject an oversized multipart body.
- **Typed error model** — `GlobalExceptionHandler` + `ErrorCode` map domain
  exceptions to consistent HTTP responses instead of leaking stack traces.

## Local development

**Prerequisites:** JDK 17+, Docker (for a local MySQL).

```bash
docker compose up -d                 # MySQL on localhost:3306
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

API at `http://localhost:8080/api`. Swagger UI at `/api/swagger-ui.html`
(dev profile only).

**Profiles**

| Profile | ddl-auto | Flyway | Swagger | Secrets |
| --- | --- | --- | --- | --- |
| `dev` | `validate` | on | on | dev-only JWT secret baked in |
| `prod` | `validate` | on | off | all from env vars, no defaults |
| *(none)* | `create-drop` | off | — | throwaway schema generated from entities |

**Build a runnable jar:**
```bash
./mvnw clean package -DskipTests      # -> target/pethotelgo-*.jar
```

## Configuration

All production secrets are environment variables — nothing sensitive is
committed. See [`.env.backend.example`](.env.backend.example) for the full
list with explanations:

| Variable | Purpose |
| --- | --- |
| `JDBC_DATABASE_URL` / `_USERNAME` / `_PASSWORD` | Database connection |
| `JWT_SECRET` | `openssl rand -base64 64` — mandatory, ≥ 32 bytes decoded |
| `CORS_ALLOWED_ORIGINS` | Comma-separated allow-list; empty ⇒ permissive pattern match |
| `JWT_EXPIRATION` / `JWT_REFRESH_EXPIRATION` | Token lifetimes in ms (default 1 h / 7 d) |
| `HIKARI_MAX_POOL_SIZE` | Connection pool size |

## Deploy strategy (summary)

The production deployment (not part of this public repo) runs this API as a
Docker container on a small Always-Free-tier cloud VM, behind a reverse
proxy that terminates HTTPS and forwards `/api/*` to the container, talking
to a managed MySQL instance reachable only over a private network — the
database has no public IP, and only the reverse proxy's edge is exposed to
the internet. `docker-compose.backend.yml` and
[`scripts/provision-mysql-oci.sh`](scripts/provision-mysql-oci.sh) show the
container and managed-database provisioning shape without any
environment-specific hosts or credentials.

## API testing

[`pethotelgo-hoppscotch.json`](pethotelgo-hoppscotch.json) is a
[Hoppscotch](https://hoppscotch.io/) collection covering auth (register,
login, refresh-token rotation) and the core resource endpoints, useful as a
quick smoke-test suite against a local instance.

## License

No license file is included — all rights reserved by the author. Code is
shared here for portfolio/review purposes.
