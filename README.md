# tf2-database-manager

REST API service that owns the PostgreSQL state for the tf2autobot stack. It exposes CRUD endpoints for the pricelist, blocked users, trade stats, and poll data, replacing the in-memory stores in `tf2autobot-java` with a durable, queryable database.

## Architecture

```
tf2autobot-java  ──HTTP──▶  tf2-database-manager  ──JPA──▶  PostgreSQL
                                (port 8081)                   (tf2autobot DB)
```

Flyway runs migrations on startup — the schema is always in sync with the code, no manual SQL needed.

## API Endpoints

All endpoints require `Authorization: Bearer <token>` unless `DB_MANAGER_API_TOKEN` is blank (dev mode). `/health` is always unauthenticated.

### Health

| Method | Path | Description |
|---|---|---|
| `GET` | `/health` | Liveness check — returns `{"status":"ok"}` |

### Pricelist

| Method | Path | Description |
|---|---|---|
| `GET` | `/pricelist` | All entries. Add `?enabled=true` to filter |
| `GET` | `/pricelist/{sku}` | Single entry by SKU (e.g. `5021;6`) |
| `PUT` | `/pricelist/{sku}` | Upsert an entry |
| `POST` | `/pricelist` | Create an entry (body must include `sku`) |
| `DELETE` | `/pricelist/{sku}` | Delete one entry |
| `DELETE` | `/pricelist` | Wipe the entire pricelist |

SKUs contain semicolons (e.g. `5021;6`) — the `{sku:.+}` regex on the path variable handles this correctly.

**Pricelist entry body:**

```json
{
  "sku": "5021;6",
  "enabled": true,
  "autoprice": false,
  "intent": 2,
  "min": 0,
  "max": 1,
  "buy":  { "keys": 1, "metal": 0.33 },
  "sell": { "keys": 1, "metal": 0.66 },
  "note": null
}
```

`intent`: `0` = buy only, `1` = sell only, `2` = bank (buy + sell).

### Blocked Users

| Method | Path | Description |
|---|---|---|
| `GET` | `/blocked-users` | All blocked users |
| `GET` | `/blocked-users/{steamId64}` | Single user |
| `PUT` | `/blocked-users/{steamId64}` | Block (body: `{"reason":"..."}`) |
| `DELETE` | `/blocked-users/{steamId64}` | Unblock |

### Trade Stats

Single-row table — always pre-seeded with a row at `id=1`.

| Method | Path | Description |
|---|---|---|
| `GET` | `/trade-stats` | Current stats |
| `POST` | `/trade-stats/accepted` | Increment accepted count. Body: `{"profitInRefined": 0.33}` (optional, defaults to 0) |
| `POST` | `/trade-stats/declined` | Increment declined count |
| `POST` | `/trade-stats/reset` | Zero out all counters |

### Poll Data

Tracks the last-known state of each trade offer for polling continuity.

| Method | Path | Description |
|---|---|---|
| `GET` | `/poll-data` | All offer entries |
| `GET` | `/poll-data/offer-since` | Get the `offer_since` cursor timestamp |
| `PUT` | `/poll-data/offer-since` | Set the cursor. Body: `{"offerSince":"1700000000"}` |
| `PUT` | `/poll-data/{offerId}` | Upsert an offer entry |
| `DELETE` | `/poll-data/{offerId}` | Remove one entry |
| `DELETE` | `/poll-data` | Clear all entries and reset `offer_since` to `0` |
| `GET` | `/poll-data/settings` | All key/value settings rows |

**Offer entry body:**

```json
{
  "direction": "received",
  "state": 2,
  "partnerSteamId64": "76561198000000001"
}
```

## Configuration

All settings are via environment variables.

| Variable | Default | Description |
|---|---|---|
| `PORT` | `8081` | HTTP port |
| `DB_HOST` | `localhost` | Postgres host |
| `DB_PORT` | `5432` | Postgres port |
| `DB_NAME` | `tf2autobot` | Database name |
| `SPRING_DATASOURCE_USERNAME` | — | Postgres user |
| `SPRING_DATASOURCE_PASSWORD` | — | Postgres password |
| `DB_MANAGER_API_TOKEN` | *(blank)* | Bearer token for auth. Blank = auth disabled (dev mode) |

## Running Locally

Requires a Postgres instance. The quickest way:

```bash
docker run -d --name tf2-manager-pg \
  -e POSTGRES_DB=tf2autobot \
  -e POSTGRES_USER=tf2manager \
  -e POSTGRES_PASSWORD=secret \
  -p 5432:5432 \
  postgres:16
```

Then run the service:

```bash
cd tf2-database-manager
mvn spring-boot:run
```

Flyway will create all tables on first startup. The service is available at `http://localhost:8081`.

To run tests (uses H2 in-memory, no Postgres needed):

```bash
mvn test
```

## Docker

Build:

```bash
docker build -t mwesterham/tf2-database-manager:latest .
```

Run:

```bash
docker run -d \
  -e DB_HOST=tf2-manager-postgres \
  -e DB_NAME=tf2autobot \
  -e SPRING_DATASOURCE_USERNAME=tf2manager \
  -e SPRING_DATASOURCE_PASSWORD=secret \
  -e DB_MANAGER_API_TOKEN=your-token \
  -p 8081:8081 \
  mwesterham/tf2-database-manager:latest
```

## Database Migrations

Flyway migrations live in `src/main/resources/db/migration/`. They run automatically on startup in order.

| Version | Description |
|---|---|
| V1 | `pricelist` table + indexes |
| V2 | `blocked_users` table |
| V3 | `trade_stats` singleton table + seed row |
| V4 | `poll_data` table + indexes |
| V5 | `settings` key-value table + `offer_since` seed |

New migrations: add a `V6__description.sql` file and restart the service.

## Kubernetes

Deployed in the `tf2-manager` namespace on the homelab cluster.

| Resource | Address |
|---|---|
| REST API | `http://tf2-manager.ani:8081` |
| Postgres | `tf2-manager-postgres.ani:5432` |

Manifests live in `k8s-mwesterham-homelab`:

```
configuration/services/tf2autobot/
├── tf2-manager-storage/       # Postgres PVC, Deployment, Service
│   ├── configmap.yaml
│   ├── postgres-pvc.yaml
│   └── tf2-manager-postgres.yaml
└── tf2-manager/               # App Deployment, Service, Secrets
    ├── tf2-manager.yaml
    ├── tf2-manager-secrets.yaml          (gitignored — fill from template)
    └── tf2-manager-secrets.yaml.template
```

See the homelab README for the full deploy runbook.
