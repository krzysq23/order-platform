# Distributed Order Processing System

Production-style distributed system built with **Java 21, Kotlin, Spring Boot 4, Kafka and PostgreSQL**.

This project demonstrates practical implementation of:

- Domain-Driven Design (DDD)
- Saga Pattern (Orchestration)
- Outbox Pattern
- Idempotent Consumer
- Event-Driven Architecture
- Clean Architecture

---

# Architecture Overview

The system consists of three microservices:

| Service | Language | Responsibility |
|----------|----------|----------------|
| `orders-service` | Java (Spring Boot 4) | Order lifecycle, Saga orchestration, Outbox publishing |
| `inventory-service` | Java (Spring Boot 4) | Stock management and reservation |
| `payments-service` | Kotlin (Spring Boot 3) | Payment processing |

Communication between services is fully **asynchronous via Kafka**.

Each service has its own database (Database per Service pattern).

---

# Business Flow (Happy Path)

1. `POST /orders`
2. Order created with status `INVENTORY_PENDING`
3. `ReserveStockRequested` saved to outbox
4. Inventory reserves stock → publishes `StockReserved`
5. Orders service publishes `PaymentRequested`
6. Payments service processes payment → publishes `PaymentSucceeded`
7. Orders service marks order as `PAID`

All cross-service communication happens through Kafka topics.

---

# Patterns Implemented

## Saga Pattern (Orchestration)

- Implemented in `orders-service`
- `saga_instances` table maintains state
- Explicit state machine
- Idempotent event handling

### Saga States

- Each service has `outbox_messages`
- Events written in the same DB transaction as business state
- Background dispatcher publishes to Kafka
- `FOR UPDATE SKIP LOCKED` used for safe concurrent processing

### Saga States

- `processed_events` table prevents double processing
- Ensures safe retries and at-least-once delivery

### Domain-Driven Design

- Aggregates encapsulate business logic
- Domain events emitted from aggregates
- Infrastructure maps Domain Events → Integration Events
- Clean separation:
```
domain
application
infrastructure
```

---

# Event Envelope Structure

All integration events follow consistent structure:

```json
{
  "eventId": "uuid",
  "eventType": "StockReserved",
  "version": 1,
  "occurredAt": "2026-02-11T10:49:43Z",
  "data": { ... }
}
```

This ensures:
- versioning
- schema stability
- consumer compatibility

---

# Database

Each service has its own PostgreSQL database.

Key tables:

## Orders Service

- `orders`
- `saga_instances`
- `processed_events`
- `outbox_messages`

## Inventory Service

- `products`
- `product_category`
- `stock_items`
- `stock_reservations`
- `stock_reservation_lines`
- `outbox_messages`

## Payments Service

- `payments`
- `outbox_messages`
- `processed_events`

Flyway is used for schema migrations.

---

# Running the Project

## Requirements

- Docker
- Docker Compose
- Java 21+
- Gradle

## Start all services

```bash
docker-compose up --build
```

Kafka, PostgreSQL and all services will start automatically.

---

# Example Flow

## Create Order

```bash
curl -X POST http://localhost:8081/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-1",
    "items": [
      { "sku": "TV-LG-65-OLED", "quantity": 1 },
      { "sku": "DISHWASHER-SIEMENS", "quantity": 1 }
    ]
  }'
```

## Create Order

```bash
GET http://localhost:8081/orders/{orderId}
```

---

# API Documentation

Swagger UI available:

- Orders:
`http://localhost:8081/swagger-ui.html`
- Inventory:
`http://localhost:8083/swagger-ui.html`
- Payments:
`http://localhost:8082/swagger-ui.html`

---

# Technical Highlights

- Spring Boot 4 (Java 21)
- Kotlin Spring Boot
- Kafka (event-driven architecture)
- PostgreSQL + Flyway
- Hibernate + JPA
- Structured logging
- Clean Architecture layering
- Explicit state transitions
- No anemic domain model
- Explicit integration event mapping

---

# Example State Machines

## OrderStatus

```
CREATED
INVENTORY_PENDING
PAYMENT_PENDING
PAID
PAYMENT_FAILED
CANCELLED
```

## SagaState

```
INVENTORY_REQUESTED
INVENTORY_RESERVED
PAYMENT_REQUESTED
PAID
PAYMENT_FAILED
CANCELLED
```

---

# Design Decisions

- Orchestration Saga instead of Choreography → clearer flow control
- Explicit envelope events for versioning
- Domain Events ≠ Integration Events
- Outbox for guaranteed delivery
- Idempotency at consumer side
- No synchronous inter-service calls

---

# Why This Project?

The goal was to build a realistic production-style distributed system, demonstrating:

- handling of distributed transactions
- event-driven architecture
- reliability patterns
- domain modeling discipline
- clean infrastructure boundaries

This project is intended as a portfolio-grade example of building resilient microservices using modern JVM stack.

---

# Possible Future Extensions

- Inventory step compensation
- Dead-letter topics
- Observability (Prometheus + Grafana)
- OpenTelemetry tracing
- Contract testing
- Kubernetes deployment manifests
