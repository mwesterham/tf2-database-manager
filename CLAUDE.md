# CLAUDE.md

## Architecture Role

**tf2-database-manager is a pure storage layer.** It exposes HTTP CRUD endpoints so the autobot can persist and retrieve data. It has no knowledge of any other service and must never call the pricer, the autobot, or any other external service.

All orchestration and decision-making lives in the autobot. All pricing logic lives in the pricer. This service stores data only.

## Commands

```bash
# Build
mvn package -DskipTests

# Run tests
mvn test

# Docker
docker build -t mwesterham/tf2-database-manager:latest .
```

## Guardrails

- **Never add outbound HTTP calls to other services.** db-manager must not call the pricer, the autobot, or any third-party API.
- **Never add scheduling** (`@Scheduled`, `@EnableScheduling`) for tasks that involve calling another service.
- **Never run `git push` unprompted** — only push when the user explicitly asks.
