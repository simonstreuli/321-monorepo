---
marp: true
theme: default
paginate: true
backgroundColor: #fff
backgroundImage: url('https://marp.app/assets/hero-background.svg')
---

<!-- _class: lead -->

# 🍕 Distributed Pizza Platform

## Microservices-Architektur für einen modernen Pizza-Lieferdienst

**Team:** Simon, Noris, Fran, Mouad

---

# 🎯 Projektübersicht

Eine **verteilte Plattform** für einen Pizza-Lieferdienst mit:

- ✅ 4 unabhängige Microservices
- ✅ REST & RabbitMQ Kommunikation
- ✅ Event-Driven Architecture
- ✅ Resilience & Fehlertoleranz
- ✅ Horizontale Skalierung

**Technologien:** Java 21, Spring Boot, RabbitMQ, Docker

---

# 👥 Team & Zuständigkeiten

| Name | Rolle | Service | Port |
|------|-------|---------|------|
| **Simon** | Einstiegspunkt & Validierung | Order Service | 8080 |
| **Noris** | Zahlungsabwicklung | Payment Service | 8081 |
| **Fran** | Asynchrone Zubereitung | Kitchen Service | 8082 |
| **Mouad** | Status-Tracking | Delivery Service | 8083 |

---

# 🏗️ System-Architektur

```
              Client (Browser / API)
                      │
                      ▼
            ┌──────────────────┐
            │  Order Service   │  ◄── Einstiegspunkt
            │    Port 8080     │
            └────┬─────────┬───┘
                 │         │
         REST ──►│         │◄── AMQP async
                 │         │
                 ▼         ▼
        ┌─────────────┐  ┌──────────────┐
        │  Payment    │  │  RabbitMQ    │
        │  Service    │  │    Broker    │
        └─────────────┘  └──┬────────┬──┘
                            │        │
                            ▼        ▼
                    ┌──────────┐  ┌──────────┐
                    │ Kitchen  │  │ Delivery │
                    │ Service  │  │ Service  │
                    └──────────┘  └──────────┘
```

---

# 🔄 Bestellablauf (1/2)

**1. Client sendet Bestellung**
   - `POST /orders` mit Pizza, Menge, Adresse, Name

**2. Order Service validiert**
   - Prüft Pflichtfelder
   - Berechnet Preis

**3. Synchrone Zahlung**
   - REST-Call zu Payment Service
   - Timeout: 5 Sekunden
   - Bei Fehler: Graceful Degradation

---

# 🔄 Bestellablauf (2/2)

**4. Event Publishing (Asynchron)**
   - `order.placed` Event → RabbitMQ
   - Durable Queue speichert Message

**5. Kitchen konsumiert Event**
   - Zubereitung (5-10 Sekunden)
   - `order.ready` Event → RabbitMQ

**6. Delivery konsumiert Event**
   - Fahrerzuweisung
   - Status-Updates via REST API

---

# 💡 Design Patterns

### 1️⃣ Event-Driven Architecture
Loose Coupling durch Events statt direkte Calls

### 2️⃣ Competing Consumers
Automatische Lastverteilung über mehrere Kitchen Instances

### 3️⃣ Circuit Breaker (Basis)
Timeout-Handling & Graceful Degradation

### 4️⃣ Shared Models Library
Zentrale `pizza-models` für Konsistenz

---

# 🛠️ Technologie-Stack

### Backend
- **Java 21** – Moderne Java-Features
- **Spring Boot 3.2** – Framework
- **Spring AMQP** – RabbitMQ Integration
- **Maven** – Build Tool

### Infrastruktur
- **RabbitMQ 3.12** – Message Broker
- **Docker** – Containerisierung
- **Docker Compose** – Orchestrierung

### Frontend
- **Node.js 18** + Express.js

---

# 🔐 Resilience Features

✅ **Durable Queues**
   Nachrichten überleben Broker-Neustart

✅ **Asynchrone Verarbeitung**
   Services müssen nicht gleichzeitig verfügbar sein

✅ **Retry Logic**
   Automatische Wiederholungen via Spring AMQP

✅ **Timeout Handling**
   5 Sekunden für Payment Service

✅ **Horizontal Scaling**
   Stateless Services, beliebig skalierbar

---

# 📊 Resilience-Szenario 1

## Payment Service offline

```bash
# Payment Service stoppen
docker compose stop payment-service

# Bestellung versuchen
curl -X POST http://localhost:8080/orders ...
```

**Ergebnis:**
❌ Freundliche Fehlermeldung
✅ Keine Bestellung durchgeführt
✅ System bleibt stabil

---

# 📊 Resilience-Szenario 2

## Kitchen Service offline

```bash
# Kitchen Service stoppen
docker compose stop kitchen-service

# 5 Bestellungen senden
for i in {1..5}; do curl ...; done

# Kitchen Service starten
docker compose start kitchen-service
```

**Ergebnis:**
✅ RabbitMQ puffert alle Messages
✅ Alle Orders werden verarbeitet
✅ Kein Datenverlust

---

# 📊 Resilience-Szenario 3

## Hohe Last & Skalierung

```bash
# 3 Kitchen Instances starten
docker compose up --scale kitchen-service=3

# 10 parallele Bestellungen
for i in {1..10}; do curl ... & done
```

**Ergebnis:**
🚀 Load Balancing über 3 Instances
✅ Parallele Verarbeitung
✅ Kein Bottleneck

---

# 📡 API Contract: POST /orders

**Request:**
```json
{
  "pizza": "Margherita",
  "quantity": 2,
  "address": "Musterstrasse 123, 8000 Zürich",
  "customerName": "Max Mustermann"
}
```

**Response:**
```json
{
  "orderId": "123e4567-e89b-12d3-a456-426614174000",
  "status": "SUCCESS",
  "message": "Order placed successfully!"
}
```

---

# 📡 Event Contracts

**order.placed:**
```json
{
  "orderId": "...",
  "pizza": "Margherita",
  "quantity": 2,
  "address": "Musterstrasse 123, 8000 Zürich",
  "customerName": "Max Mustermann",
  "timestamp": "2026-01-09T10:30:00"
}
```

**order.ready:**
```json
{
  "orderId": "...",
  "pizza": "Margherita",
  "quantity": 2,
  "preparedAt": "2026-01-09T10:35:00"
}
```

---

# 📊 Monitoring & Observability

### Health Endpoints
- `/orders/health` – Order Service
- `/health` – Payment Service
- `/deliveries/health` – Delivery Service

### RabbitMQ Management UI
- **URL:** http://localhost:15672
- **Login:** guest / guest
- Queue Monitoring, Message Rates, Consumer Status

### Structured Logging
- Order IDs für Tracing
- INFO, DEBUG, ERROR Levels

---

# 🧪 Testing & CI/CD

### Integration Tests
- End-to-End Tests für kompletten Flow
- GitHub Actions CI
- Automatisch bei Push zu `main`/`develop`
- Täglich um 2:00 UTC

### Lokale Ausführung
```bash
./integration-test.sh
```

**Test Flow:**
1. Startet alle Services
2. Wartet auf Health Checks
3. Führt E2E-Tests durch
4. Räumt automatisch auf

---

# 🚢 Installation & Start

### Voraussetzungen
- Docker & Docker Compose

### Quick Start
```bash
# Standard Setup
docker compose up --build

# Frontend öffnen
open http://localhost:3000
```

### Mit Skalierung
```bash
# 3 Kitchen Instances
docker compose up --scale kitchen-service=3
```

---

# 📚 Shared Pizza Models Library

**Problem:** Jeder Service definiert eigene Models
→ Inkonsistenzen, Breaking Changes

**Lösung:** Zentrale `pizza-models` Library

### Vorteile
✅ Konsistenz über alle Services
✅ Zentrale Wartung
✅ Type Safety (Compile-Time)
✅ Automatische Updates

**Modelle:** OrderRequest, OrderResponse, OrderPlacedEvent, OrderReadyEvent, PaymentRequest, PaymentResponse

---

# 🔑 Key Features Zusammenfassung

### Asynchrone Kommunikation
Services müssen nicht gleichzeitig verfügbar sein

### Horizontale Skalierung
Competing Consumers mit automatischem Load Balancing

### Fehlertoleranz
Timeout Handling & Graceful Degradation

### Message Persistence
Durable Queues in RabbitMQ

---

# 🎯 Lessons Learned

### Was funktioniert gut ✅
- Asynchrone Kommunikation ist sehr robust
- Docker Compose vereinfacht Entwicklung
- Shared Models verhindert Breaking Changes
- Competing Consumers = einfache Skalierung

### Herausforderungen ⚠️
- Debugging über Services ist komplex
- Distributed Tracing wäre hilfreich
- Monitoring könnte erweitert werden

---

# 🔮 Mögliche Erweiterungen

### Kurzfristig
- API Gateway (einheitlicher Einstiegspunkt)
- Service Discovery (dynamisch)
- Distributed Tracing (Zipkin/Jaeger)

### Mittelfristig
- Authentication & Authorization
- Rate Limiting
- Caching Layer (Redis)

### Langfristig
- Kubernetes Deployment
- Database per Service
- Event Sourcing

---

# 📈 Architektur-Highlights

### Microservices Best Practices
✅ Single Responsibility
✅ Loose Coupling
✅ High Cohesion
✅ API-First Design

### Cloud-Native Patterns
✅ Containerization
✅ Orchestration-Ready
✅ Health Checks
✅ 12-Factor App Principles

---

# 🔗 Verfügbare Services

| Service | URL | Port |
|---------|-----|------|
| Frontend | http://localhost:3000 | 3000 |
| Order Service | http://localhost:8080 | 8080 |
| Payment Service | http://localhost:8081 | 8081 |
| Kitchen Service | http://localhost:8082 | 8082 |
| Delivery Service | http://localhost:8083 | 8083 |
| RabbitMQ UI | http://localhost:15672 | 15672 |

---

# 🎓 Zusammenfassung

### Was haben wir gebaut?
Vollständige **Microservices-Architektur** mit:
- 4 unabhängigen Services
- Synchroner & asynchroner Kommunikation
- Resilience & Skalierbarkeit

### Was haben wir gelernt?
- Event-Driven Architecture in der Praxis
- Message Broker als zentrale Komponente
- Microservice-Patterns in Aktion

### Warum ist das relevant?
**Produktionsreife** Architektur mit modernen Cloud-Native-Prinzipien

---

<!-- _class: lead -->

# 🚀 Live Demo

**Bereit für die Demonstration!**

1. System starten
2. Frontend zeigen
3. Bestellung aufgeben
4. Resilience-Tests durchführen
5. Skalierung demonstrieren

---

<!-- _class: lead -->

# ❓ Fragen?

**Dokumentation:**
- README.md
- ARCHITECTURE.md
- INTEGRATION_TESTS.md

**Repository:**
https://github.com/simonstreuli/321-monorepo

---

<!-- _class: lead -->

# 🙏 Vielen Dank!

**Distributed Pizza Platform**

Team: Simon, Noris, Fran, Mouad

Java 21 · Spring Boot · RabbitMQ · Docker
Microservices · Event-Driven · Cloud-Native

---
