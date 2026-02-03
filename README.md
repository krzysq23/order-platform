# Order Platform – Microservices Demo

Production-like system built to practice and demonstrate modern backend development
with Java/Kotlin, Spring Boot, and event-driven microservices architecture.

The project focuses on real-world problems often discussed during Mid/Senior Java
interviews: transactions, distributed systems, Kafka, idempotency, concurrency,
Clean Architecture, DDD, and observability.

---

## 📌 System Overview

The system simulates an order processing platform:
Order → Payment → Stock Reservation → Notification.

Communication between services is asynchronous (Kafka) and follows
event-driven principles.

---

## 🧩 Microservices

| Service | Language | Responsibility |
|-------|--------|----------------|
| orders-service | Java | Order lifecycle, transactions, outbox, read model |
| payments-service | Kotlin | Payment processing, async workflows |
| inventory-service | Java | Stock management and reservation |
| notifications-service | Java | Asynchronous notifications |

Each service:
- owns its database (PostgreSQL),
- exposes REST APIs,
- communicates via Kafka events.

---

## 🏗 Architecture & Design

- **Architecture style:** Microservices + Clean Architecture (Hexagonal)
- **Domain modeling:** Domain-Driven Design (aggregates, value objects, domain events)
- **Messaging:** Kafka (at-least-once delivery)
- **Data consistency:** Outbox Pattern, idempotent consumers
- **Caching & NoSQL:** Redis (cache-aside, idempotency keys)
- **Concurrency:** Controlled async processing and parallel workflows
- **Transactions:** Declarative transaction management (Spring)

Architecture decisions are documented in `/docs/adr`.

---

## 🔄 Event Flow (High Level)

1. Client creates an order via `orders-service`
2. Order is stored in DB and an event is written to the outbox table (same transaction)
3. Outbox publisher publishes `OrderCreated` event to Kafka
4. `payments-service` processes payment and emits `PaymentAuthorized` or `PaymentRejected`
5. `inventory-service` reserves stock
6. `notifications-service` sends confirmation asynchronously

---

## 🧪 Testing Strategy

- Unit tests: domain and application layers
- Integration tests: REST, Kafka, database (Testcontainers)
- Focus on business rules, transaction boundaries, and idempotency

---

## 🚀 Running the System Locally

### Prerequisites
- Java 21
- Docker & Docker Compose

### Start infrastructure
```bash
docker-compose up -d
