# Integration Tests Documentation

## Übersicht

Dieses Dokument beschreibt das Integration Test System für die Distributed Pizza Platform. Die Integration Tests starten alle Services zusammen mit Docker Compose und validieren das Ende-zu-Ende Verhalten des gesamten Systems.

## Zweck

Die Integration Tests stellen sicher, dass:
- Alle Services korrekt starten und miteinander kommunizieren können
- Die REST API Kommunikation zwischen Services funktioniert
- Die asynchrone RabbitMQ Kommunikation funktioniert
- Der komplette Bestellablauf von Order → Payment → Kitchen → Delivery funktioniert
- Das Frontend erreichbar ist

## Test-Komponenten

### 1. Integration Test Script (`integration-test.sh`)

Ein Bash-Script das:
- Alle Services mit `docker compose up -d` startet
- Auf die Bereitschaft aller Services wartet (Health Checks)
- Eine Serie von End-to-End Tests ausführt
- Die Services nach den Tests sauber herunterfährt

**Verwendung:**

```bash
# Lokale Ausführung
./integration-test.sh

# Mit detailliertem Output
bash -x integration-test.sh
```

### 2. GitHub Actions Workflow (`.github/workflows/integration-test.yml`)

Ein automatisierter CI/CD Workflow der:
- Bei jedem Push zu `main` oder `develop` läuft
- Bei jedem Pull Request läuft
- Manuell ausgelöst werden kann
- Täglich um 2 Uhr nachts läuft (für kontinuierliche Überwachung)

**Status:** Der Workflow-Status ist im Repository README sichtbar.

## Test-Szenarien

### Test 1: Health Checks
**Zweck:** Validiert dass alle Services starten und ihre Health Endpoints erreichbar sind.

**Services geprüft:**
- Order Service (`http://localhost:8080/orders/health`)
- Payment Service (`http://localhost:8081/health`)
- Delivery Service (`http://localhost:8083/deliveries/health`)

**Erfolg:** Alle Health Endpoints geben HTTP 200 zurück.

### Test 2: Create Order - Success Flow
**Zweck:** Testet den kompletten Bestellablauf.

**Ablauf:**
1. POST Request an `/orders` mit gültigen Bestelldaten
2. Validiert dass eine Order ID zurückgegeben wird
3. Überprüft den Status der Antwort

**Testdaten:**
```json
{
  "pizza": "Margherita",
  "quantity": 2,
  "address": "Teststrasse 123, 8000 Zürich",
  "customerName": "Integration Test User"
}
```

**Erfolg:** Order wird erstellt und Order ID wird zurückgegeben.

### Test 3: Verify RabbitMQ Message Flow
**Zweck:** Überprüft dass RabbitMQ korrekt konfiguriert ist und Messages verarbeitet werden.

**Prüfungen:**
- RabbitMQ Management UI ist erreichbar
- Queue `order.placed` existiert
- Messages werden in Queues gespeichert

**Erfolg:** RabbitMQ Queues sind vorhanden und funktionieren.

### Test 4: Multiple Orders (Load Test)
**Zweck:** Testet das System unter leichter Last.

**Ablauf:**
- Erstellt 3 Bestellungen nacheinander
- Validiert dass alle erfolgreich verarbeitet werden

**Erfolg:** Alle 3 Bestellungen werden erfolgreich erstellt.

### Test 5: Delivery Service Integration
**Zweck:** Validiert die End-to-End Integration bis zum Delivery Service.

**Ablauf:**
1. Wartet auf Kitchen Service Verarbeitung (15 Sekunden)
2. Fragt Delivery Status ab (`GET /deliveries`)
3. Validiert dass Delivery Service antwortet

**Erfolg:** Delivery Service ist erreichbar und antwortet mit validen Daten.

### Test 6: Frontend Accessibility
**Zweck:** Stellt sicher dass das Frontend verfügbar ist.

**Prüfung:** HTTP GET Request an `http://localhost:3000`

**Erfolg:** Frontend gibt HTTP 200 zurück.

## Zeitübersicht

| Phase | Geschätzte Dauer |
|-------|------------------|
| Services starten | 30-60 Sekunden |
| Health Checks warten | 30-120 Sekunden |
| Tests ausführen | 30-60 Sekunden |
| **Total** | **2-4 Minuten** |

## Lokale Ausführung

### Voraussetzungen
- Docker installiert
- Docker Compose installiert
- Ports 3000, 5672, 8080, 8081, 8082, 8083, 15672 verfügbar

### Schritte

1. **Repository klonen:**
   ```bash
   git clone https://github.com/simonstreuli/321-monorepo.git
   cd 321-monorepo
   ```

2. **Integration Tests ausführen:**
   ```bash
   ./integration-test.sh
   ```
   
   Falls das Script nicht ausführbar ist:
   ```bash
   chmod +x integration-test.sh
   ./integration-test.sh
   ```

3. **Optional: Nur Services starten (ohne Tests):**
   ```bash
   docker compose up
   ```

4. **Services stoppen:**
   ```bash
   docker compose down -v
   ```

## CI/CD Integration

### Automatische Ausführung

Der Integration Test Workflow läuft automatisch:
- **Bei Push:** Nach jedem Push zu `main` oder `develop`
- **Bei Pull Request:** Für jeden PR zu `main` oder `develop`
- **Geplant:** Täglich um 2:00 UTC
- **Manuell:** Via GitHub Actions "Run workflow" Button

### Workflow Status prüfen

1. Gehe zu GitHub Repository
2. Klicke auf "Actions" Tab
3. Wähle "Integration Tests - All Services" Workflow
4. Sieh den Status der letzten Runs

### Fehlerbehebung in CI

Wenn Tests in CI fehlschlagen:

1. **Logs ansehen:**
   - Klicke auf den fehlgeschlagenen Workflow Run
   - Erweitere den Step "Run Integration Tests"
   - Prüfe die Container Logs im "Display container logs on failure" Step

2. **Häufige Probleme:**
   - **Timeout:** Services brauchen länger zum Starten → Erhöhe Timeout
   - **Port Konflikte:** Sollte nicht in CI passieren, aber lokal möglich
   - **Image Pull Fehler:** Prüfe dass alle Docker Images verfügbar sind
   - **RabbitMQ Verbindung:** Prüfe dass RabbitMQ Health Check erfolgreich ist

## Erweiterte Konfiguration

### Umgebungsvariablen

Services können über Environment Variables in `docker-compose.yml` konfiguriert werden:

```yaml
environment:
  PAYMENT_FAILURE_RATE: 0.2      # Payment Service Fehlerrate
  KITCHEN_PREPARATION_TIME_MIN: 5000  # Kitchen Min Zeit (ms)
  KITCHEN_PREPARATION_TIME_MAX: 10000 # Kitchen Max Zeit (ms)
```

### Skalierungs-Tests

Teste mit mehreren Kitchen Service Instanzen:

```bash
# Ändere integration-test.sh
docker compose up -d --scale kitchen-service=3
```

## Monitoring während Tests

### 1. RabbitMQ Management UI
**URL:** http://localhost:15672  
**Credentials:** guest / guest

**Was zu prüfen:**
- Queues → Anzahl Messages
- Connections → Anzahl aktive Verbindungen
- Channels → Message Rates

### 2. Service Logs Live ansehen

```bash
# Alle Services
docker compose logs -f

# Einzelne Services
docker compose logs -f order-service
docker compose logs -f kitchen-service
docker compose logs -f payment-service
docker compose logs -f delivery-service
```

### 3. Container Status

```bash
# Alle Container Status
docker compose ps

# Resource Usage
docker stats
```

## Test Output Beispiel

Erfolgreiche Test-Ausführung:

```
=====================================
Integration Test - Pizza Platform
=====================================

[INFO] Starting all services with Docker Compose...
[INFO] Waiting for RabbitMQ to be ready...
[INFO] RabbitMQ is ready!
[INFO] Waiting for Payment Service to be ready...
[INFO] Payment Service is ready!
[INFO] Waiting for Order Service to be ready...
[INFO] Order Service is ready!
[INFO] Waiting for Delivery Service to be ready...
[INFO] Delivery Service is ready!
[INFO] Waiting for Frontend to be ready...
[INFO] Frontend is ready!

[INFO] All services are up and running!

======================================
Running Integration Tests
======================================

[INFO] Test 1: Health Checks
[INFO] ✓ All health checks passed

[INFO] Test 2: Create Order - Success Flow
[INFO] ✓ Order created successfully with ID: abc-123-def
   Response: {"orderId":"abc-123-def","status":"SUCCESS","message":"Order placed successfully!"}

[INFO] Test 3: Verify RabbitMQ Message Flow
[INFO] ✓ RabbitMQ queue 'order.placed' exists

[INFO] Test 4: Create Multiple Orders
[INFO] ✓ Successfully created 3 orders

[INFO] Test 5: Delivery Service Integration
[INFO] ✓ Delivery service is responding
   Active deliveries: [...]

[INFO] Test 6: Frontend Service
[INFO] ✓ Frontend is accessible

======================================
[INFO] All Integration Tests PASSED!
======================================
```

## Best Practices

1. **Regelmäßig ausführen:** Täglich oder bei jedem Code-Change
2. **Logs prüfen:** Auch wenn Tests passen, Logs auf Warnings prüfen
3. **Performance beobachten:** Achte auf zunehmende Test-Dauer
4. **Clean State:** Jeder Test-Run startet mit frischen Containern
5. **Isolierung:** Tests sollten unabhängig von externer State sein

## Erweiterungsmöglichkeiten

### Zukünftige Test-Szenarien

1. **Resilience Tests:**
   - Service Ausfall und Recovery
   - Network Delays simulieren
   - RabbitMQ Neustart während Betrieb

2. **Performance Tests:**
   - Last-Tests mit vielen gleichzeitigen Bestellungen
   - Response Time Messungen
   - Memory Usage Monitoring

3. **Security Tests:**
   - API Endpoint Security
   - Authentication/Authorization (wenn implementiert)
   - Input Validation

4. **Chaos Engineering:**
   - Zufällige Service Ausfälle
   - Network Partitions
   - Resource Constraints

## Support

Bei Fragen oder Problemen:
1. Prüfe die Logs der fehlgeschlagenen Services
2. Verifiziere dass alle Docker Images verfügbar sind
3. Stelle sicher dass alle Ports verfügbar sind
4. Kontaktiere das Entwicklungsteam

## Zusammenhang mit Unit Tests

| Test Typ | Scope | Wann ausführen | Dauer |
|----------|-------|----------------|-------|
| **Unit Tests** | Einzelne Klassen/Methoden | Bei jedem Build | Sekunden |
| **Integration Tests** | Service-zu-Service | Bei Merges | Minuten |
| **End-to-End Tests** | Komplettes System | Vor Release | Minuten |

Die Integration Tests in diesem Dokument sind **End-to-End Tests** die das komplette System validieren.

## Referenzen

- [README.md](README.md) - Projekt Übersicht
- [ARCHITECTURE.md](ARCHITECTURE.md) - System Architektur
- [order-service/TEST.md](order-service/TEST.md) - Order Service Unit Tests
- [payment-service/TEST.md](payment-service/TEST.md) - Payment Service Unit Tests
- [docker-compose.yml](docker-compose.yml) - Service Konfiguration
