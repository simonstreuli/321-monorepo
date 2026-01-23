# Distributed Pizza Platform
## Microservices-Architektur für einen modernen Pizza-Lieferdienst

---

## 🎯 Projektübersicht

Eine verteilte Plattform für einen Pizza-Lieferdienst mit vier unabhängigen Microservices, die über **REST** und **RabbitMQ** kommunizieren.

**Kernziele:**
- Demonstration moderner Microservice-Patterns
- Asynchrone Kommunikation mit Event-Driven Architecture
- Resilience & Fehlertoleranz
- Horizontale Skalierung

---

## 👥 Team & Zuständigkeiten

| Name  | Rolle                                          | Service                    |
|-------|------------------------------------------------|----------------------------|
| **Simon** | Einstiegspunkt, Validierung & Event-Publishing | Order Service (Port 8080)  |
| **Noris** | Synchrone Zahlungsabwicklung & Fehler-Simulation | Payment Service (Port 8081) |
| **Fran**  | Asynchrone Zubereitung & Skalierung            | Kitchen Service (Port 8082) |
| **Mouad** | Event-Konsumierung & Status-Tracking           | Delivery Service (Port 8083)|

---

## 🏗️ System-Architektur

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │ HTTP POST
       ▼
┌─────────────────────┐
│  Order Service      │ ◄── Einstiegspunkt & Validierung
│  Port 8080          │
└──────┬──────┬───────┘
       │      │
       │ REST │ AMQP async
       │      │
       ▼      ▼
┌─────────────┐  ┌──────────────────┐
│  Payment    │  │   RabbitMQ       │
│  Service    │  │   Message Broker │
│  Port 8081  │  │   (Port 5672)    │
└─────────────┘  └────┬────────┬────┘
                      │        │
               order. │        │ order.
               placed │        │ ready
                      ▼        ▼
              ┌─────────────┐  ┌─────────────┐
              │  Kitchen    │  │  Delivery   │
              │  Service    │  │  Service    │
              │  Port 8082  │  │  Port 8083  │
              └─────────────┘  └─────────────┘
```

---

## 🔄 Bestellablauf (Request Flow)

**1. Client → Order Service**
   - POST /orders mit Bestelldaten
   - Validierung der Eingaben

**2. Order Service → Payment Service** (Synchron)
   - POST /pay zur Zahlungsabwicklung
   - Timeout: 5 Sekunden
   - Fehlerbehandlung mit Graceful Degradation

**3. Order Service → RabbitMQ** (Asynchron)
   - Publish `order.placed` Event
   - Durable Queue garantiert Nachrichtenspeicherung

**4. Kitchen Service ← RabbitMQ**
   - Consume `order.placed` Event
   - Simulation der Zubereitung (5-10 Sekunden)
   - Publish `order.ready` Event

**5. Delivery Service ← RabbitMQ**
   - Consume `order.ready` Event
   - Fahrerzuweisung
   - Status-Tracking via REST API

---

## 💡 Design Patterns & Best Practices

### 1. **Event-Driven Architecture**
- Events statt direkte Aufrufe
- Publish-Subscribe Pattern
- Lose Kopplung zwischen Services

### 2. **Competing Consumers Pattern**
- Mehrere Kitchen Service Instanzen möglich
- Automatische Lastverteilung durch RabbitMQ
- Horizontale Skalierbarkeit

### 3. **Circuit Breaker (Basis)**
- Timeout-Handling bei REST-Calls
- Fehlerbehandlung ohne System-Crash
- Graceful Degradation bei Service-Ausfall

### 4. **Shared Models Library**
- Zentrale `pizza-models` Bibliothek
- Konsistente Datenmodelle über alle Services
- Type Safety & Compile-Time-Fehler

---

## 🛠️ Technologie-Stack

### Backend Services
- **Java 21** – Moderne Java-Features
- **Spring Boot 3.2** – Application Framework
- **Spring AMQP** – RabbitMQ Integration
- **Maven** – Build Management

### Message Broker
- **RabbitMQ 3.12** – AMQP Message Broker
- **Management UI** – Monitoring (Port 15672)

### Frontend
- **Node.js 18** – Runtime
- **Express.js** – Web Framework
- **Vanilla JavaScript** – UI mit Echtzeit-Updates

### Deployment
- **Docker** – Containerisierung
- **Docker Compose** – Multi-Container Orchestrierung

---

## 🔐 Resilience & Hochverfügbarkeit

### Verfügbarkeitsmerkmale

✅ **Durable Queues**
   - RabbitMQ Persistence verhindert Datenverluste
   - Nachrichten überleben Broker-Neustart

✅ **Asynchrone Verarbeitung**
   - Message Broker entkoppelt Services
   - Kein direktes Blocking bei Service-Ausfall

✅ **Retry Logic**
   - Spring AMQP mit automatischen Wiederholungen
   - At-Least-Once Delivery garantiert

✅ **Timeout Handling**
   - RestTemplate Config verhindert hängende Requests
   - 5 Sekunden Timeout für Payment Service

✅ **Horizontal Scaling**
   - Stateless Services
   - Beliebige Skalierung möglich

---

## 📊 Resilience-Szenarien

### Szenario 1: Payment Service offline
```
❌ Payment Service nicht erreichbar
→ Order Service fängt Fehler ab
→ Freundliche Fehlermeldung an Client
→ Keine Bestellung wird durchgeführt
```

### Szenario 2: Kitchen Service offline
```
✅ Order Service publiziert trotzdem
→ RabbitMQ puffert Nachrichten
→ Kitchen Service startet wieder
→ Alle gepufferten Orders werden verarbeitet
```

### Szenario 3: Hohe Last
```
🚀 Skalierung auf 3 Kitchen Instances
→ RabbitMQ verteilt Last automatisch
→ Parallele Verarbeitung von Orders
→ Kein Bottleneck bei vielen Bestellungen
```

---

## 🚀 Demo-Szenarien

### 1. Erfolgreiche Bestellung (Happy Path)

**Via Frontend:**
```
1. Browser öffnen: http://localhost:3000
2. Bestellung aufgeben
3. Status in Echtzeit verfolgen
```

**Via API:**
```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "pizza": "Margherita",
    "quantity": 2,
    "address": "Musterstrasse 123, 8000 Zürich",
    "customerName": "Max Mustermann"
  }'
```

---

## 🚀 Demo-Szenarien (2)

### 2. Payment Service Resilience

**Test: Payment Service offline**
```bash
# Payment Service stoppen
docker compose stop payment-service

# Bestellung versuchen
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"pizza": "Margherita", "quantity": 1, 
       "address": "Test", "customerName": "User"}'

# Erwartung: Freundliche Fehlermeldung
# "Payment system is currently unavailable"

# Payment Service wieder starten
docker compose start payment-service
```

---

## 🚀 Demo-Szenarien (3)

### 3. Message Buffering

**Test: Kitchen Service offline**
```bash
# Kitchen Service stoppen
docker compose stop kitchen-service

# 5 Bestellungen senden
for i in {1..5}; do
  curl -X POST http://localhost:8080/orders \
    -H "Content-Type: application/json" \
    -d "{\"pizza\": \"Margherita\", \"quantity\": 1,
         \"address\": \"Test $i\", \"customerName\": \"User $i\"}"
done

# Kitchen Service wieder starten
docker compose start kitchen-service

# Logs beobachten: Alle 5 Orders werden verarbeitet
docker compose logs -f kitchen-service
```

---

## 🚀 Demo-Szenarien (4)

### 4. Horizontale Skalierung

**Test: 3 Kitchen Instances**
```bash
# System mit 3 Kitchen Services starten
docker compose up --scale kitchen-service=3 -d

# 10 Bestellungen parallel senden
for i in {1..10}; do
  curl -X POST http://localhost:8080/orders \
    -H "Content-Type: application/json" \
    -d "{\"pizza\": \"Margherita\", \"quantity\": 1,
         \"address\": \"Test $i\", \"customerName\": \"User $i\"}" &
done

# Logs zeigen: Load Balancing über alle 3 Instances
docker compose logs kitchen-service | grep "Received order"
```

---

## 📡 API Contracts

### POST /orders (Order Service)

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

## 📡 API Contracts (2)

### RabbitMQ Messages

**order.placed Event:**
```json
{
  "orderId": "123e4567-e89b-12d3-a456-426614174000",
  "pizza": "Margherita",
  "quantity": 2,
  "address": "Musterstrasse 123, 8000 Zürich",
  "customerName": "Max Mustermann",
  "timestamp": "2026-01-09T10:30:00"
}
```

**order.ready Event:**
```json
{
  "orderId": "123e4567-e89b-12d3-a456-426614174000",
  "pizza": "Margherita",
  "quantity": 2,
  "preparedAt": "2026-01-09T10:35:00"
}
```

---

## 📊 Monitoring & Observability

### Health Endpoints
- `GET /orders/health` – Order Service Status
- `GET /health` – Payment Service Status
- `GET /deliveries/health` – Delivery Service Status

### RabbitMQ Management UI
- **URL:** http://localhost:15672
- **Login:** guest / guest
- **Features:**
  - Queue Monitoring
  - Message Rates
  - Consumer Status
  - Memory Usage

### Structured Logging
- **INFO:** Normale Operations
- **DEBUG:** RabbitMQ-Details
- **ERROR:** Fehler und Exceptions
- **Tracing:** Order IDs in allen Logs

---

## 🧪 Testing

### Integration Tests
- End-to-End Tests für gesamten Flow
- Automatische Ausführung via GitHub Actions
- Bei jedem Push zu `main` oder `develop`
- Täglich um 2:00 UTC

**Lokale Ausführung:**
```bash
./integration-test.sh
```

**Der Test Script:**
1. Startet alle Services mit Docker Compose
2. Wartet auf Health Checks aller Services
3. Führt End-to-End Tests durch
4. Räumt automatisch auf

---

## 🚢 Installation & Start

### Voraussetzungen
- Docker & Docker Compose installiert

### Standard Setup
```bash
# Alle Services starten
docker compose up --build

# Frontend öffnen
open http://localhost:3000
```

### Mit Skalierung
```bash
# 3 Kitchen Service Instanzen
docker compose up --build --scale kitchen-service=3
```

---

## 📚 Zentrale Pizza Models Library

**Problem:** Jeder Service definiert eigene Models → Inkonsistenzen

**Lösung:** Gemeinsame `pizza-models` Library

### Vorteile
✅ **Konsistenz** – Alle Services nutzen exakt dieselben Definitionen
✅ **Wartbarkeit** – Zentrale Änderungen statt Duplikation
✅ **Type Safety** – Compile-Time-Fehler bei Inkompatibilität
✅ **Automatische Updates** – Producer-Änderungen → Consumer

### Enthaltene Modelle
- OrderRequest, OrderResponse
- OrderPlacedEvent, OrderReadyEvent
- PaymentRequest, PaymentResponse

---

## 🔑 Key Features

### 1. Asynchrone Kommunikation
- **Entkopplung:** Services müssen nicht gleichzeitig verfügbar sein
- **Pufferung:** RabbitMQ speichert Messages persistent
- **Resilience:** Kein Datenverlust bei Ausfällen

### 2. Horizontale Skalierung
- **Competing Consumers:** Multiple Kitchen Instances
- **Load Balancing:** Automatisch durch RabbitMQ
- **Stateless:** Beliebige Skalierung möglich

### 3. Fehlertoleranz
- **Timeout Handling:** 5s für Payment Service
- **Graceful Degradation:** Freundliche Fehlermeldungen
- **Retry Logic:** Automatische Wiederholungen

---

## 🎯 Lessons Learned

### Was funktioniert gut
✅ Asynchrone Kommunikation über RabbitMQ ist sehr robust
✅ Docker Compose vereinfacht Entwicklung massiv
✅ Shared Models Library verhindert Breaking Changes
✅ Competing Consumers Pattern ermöglicht einfache Skalierung

### Herausforderungen
⚠️ Debugging über mehrere Services ist komplex
⚠️ Distributed Tracing wäre hilfreich (z.B. Zipkin)
⚠️ Monitoring könnte erweitert werden (Prometheus/Grafana)
⚠️ End-to-End Testing erfordert komplettes Setup

---

## 🔮 Mögliche Erweiterungen

### Kurzfristig
- **API Gateway** – Einheitlicher Einstiegspunkt
- **Service Discovery** – Dynamische Service-Registrierung
- **Distributed Tracing** – Request-Verfolgung über Services

### Mittelfristig
- **Authentication & Authorization** – Sicherheit
- **Rate Limiting** – Schutz vor Überlastung
- **Caching Layer** – Performance-Optimierung (Redis)

### Langfristig
- **Kubernetes Deployment** – Production-Ready Orchestrierung
- **Database per Service** – Echte Service-Isolation
- **Event Sourcing** – Vollständige Event-Historie

---

## 📈 Architektur-Highlights

### Microservices Best Practices
✅ **Single Responsibility** – Jeder Service eine klare Aufgabe
✅ **Loose Coupling** – Minimale Abhängigkeiten
✅ **High Cohesion** – Zusammengehörige Funktionen gebündelt
✅ **API-First Design** – Contracts vor Implementierung

### Cloud-Native Patterns
✅ **Containerization** – Docker für alle Services
✅ **Orchestration** – Docker Compose (Dev), K8s-ready
✅ **Health Checks** – Liveness & Readiness Probes
✅ **12-Factor App** – Configuration via Environment

---

## 🔗 Verfügbare Dienste

Nach dem Start sind folgende Dienste erreichbar:

| Service | URL | Beschreibung |
|---------|-----|--------------|
| **Frontend** | http://localhost:3000 | Web-Interface für Bestellungen |
| **Order Service** | http://localhost:8080 | REST API Endpunkt |
| **Payment Service** | http://localhost:8081 | Zahlungsabwicklung |
| **Kitchen Service** | http://localhost:8082 | Asynchrone Verarbeitung |
| **Delivery Service** | http://localhost:8083 | Status & Tracking |
| **RabbitMQ UI** | http://localhost:15672 | Admin Panel (guest/guest) |

---

## 🎓 Zusammenfassung

### Was haben wir gebaut?
Eine **vollständige Microservices-Architektur** für einen Pizza-Lieferdienst mit:
- 4 unabhängigen Services
- Synchroner (REST) und asynchroner (AMQP) Kommunikation
- Resilience & Fehlertoleranz
- Horizontaler Skalierbarkeit

### Was haben wir gelernt?
- **Event-Driven Architecture** in der Praxis
- **Message Broker** als zentrale Komponente
- **Docker Compose** für komplexe Setups
- **Microservice-Patterns** in Aktion

### Warum ist das relevant?
Diese Architektur ist **produktionsreif** und demonstriert moderne Cloud-Native-Prinzipien, die in der Industrie weit verbreitet sind.

---

## ❓ Fragen?

**Dokumentation:**
- README.md – Getting Started & API Docs
- ARCHITECTURE.md – Detaillierte Architektur
- INTEGRATION_TESTS.md – Testing Guide
- SHARED_MODELS_IMPLEMENTATION.md – Models Library

**Demo:**
- Live-Demonstration verfügbar
- Alle Test-Szenarien können vorgeführt werden

**Kontakt:**
- GitHub: https://github.com/simonstreuli/321-monorepo

---

## 🙏 Vielen Dank!

**Team:**
- Simon – Order Service
- Noris – Payment Service
- Fran – Kitchen Service
- Mouad – Delivery Service

**Projekt:** Distributed Pizza Platform
**Technologien:** Java 21, Spring Boot, RabbitMQ, Docker
**Pattern:** Microservices, Event-Driven Architecture, Competing Consumers

---
