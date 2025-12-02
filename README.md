Config Service

Short: Spring Boot service for managing application configurations (CRUD), publishing change events to Kafka, and caching with Redis.

Quick start
- Prereqs: JDK 21, Docker, Gradle wrapper
- Start infra:
```
docker compose up -d
```

Smoke checks
- Health: GET http://localhost:8080/actuator/health
- API docs: http://localhost:8080/swagger-ui/index.html
- List configs: GET http://localhost:8080/api/v1/configurations

Create example
```
curl -X POST http://localhost:8080/api/v1/configurations \
  -H "Content-Type: application/json" \
  -d '{
    "name": "feature.flag.x",
    "application": "payments",
    "environment": "dev",
    "content": "true"
  }'
```

Config knobs (override via env or system props)
- SPRING_DATASOURCE_URL (Postgres)
- SPRING_KAFKA_BOOTSTRAP_SERVERS (Kafka)
- SPRING_DATA_REDIS_HOST / SPRING_DATA_REDIS_PORT (Redis)
- SERVER_PORT (HTTP)
- sdk.config-service.base-url (SDK default)

Tests
```
./gradlew test
```