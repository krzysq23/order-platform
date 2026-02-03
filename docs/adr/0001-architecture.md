# ADR 0001 – System Architecture

## Context
We need a production-like system to demonstrate:
- microservices
- event-driven architecture
- clean architecture
- resilience and observability

## Decision
- Monorepo with multiple Spring Boot services
- Kafka for asynchronous communication
- Orders-service acts as saga orchestrator
- PostgreSQL per service
- Redis for cache and idempotency
- Docker Compose for local environment

## Consequences
- Clear service boundaries
- Independent persistence
- Eventual consistency
