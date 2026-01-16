# Kitchen Service

Microservice für die Verwaltung der Pizzaherstellung in der Pizza-Lieferplattform. Der Service empfängt Bestellungs-Events über RabbitMQ, verarbeitet die Pizzaherstellung und publiziert anschließend Ready-Events.

## Überblick

| Eigenschaft       | Wert              |
| ----------------- | ----------------- |
| **Port**          | 8082              |
| **Java Version**  | 21                |
| **Framework**     | Spring Boot 3.2.1 |
| **Message Queue** | RabbitMQ (AMQP)   |

## Features

- ✅ Event-getriebene Architektur über RabbitMQ
- ✅ Asynchrone Bestellungsverarbeitung
- ✅ Simulierte Pizzazubereitung (5-10 Sekunden)
- ✅ Competing Consumers Pattern für horizontale Skalierung
- ✅ Detailliertes Logging mit Instance-Identifikation
- ✅ Health Checks über Spring Actuator

## Schnellstart

### Voraussetzungen

- Java 21+
- Maven 3.8+
- RabbitMQ (lokal oder Docker)

### Build & Start

```bash
# Build
mvn clean package

# Entwicklung starten
mvn spring-boot:run

# Mit Docker starten
docker build -t kitchen-service .
docker run -p 8082:8082 -e SPRING_RABBITMQ_HOST=host.docker.internal kitchen-service
```

## Konfiguration

Hauptkonfiguration in `src/main/resources/application.yml`:

```yaml
server:
  port: 8082

spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest

kitchen:
  preparation:
    time:
      min: 5000 # Minimale Zubereitungszeit in ms
      max: 10000 # Maximale Zubereitungszeit in ms
```

Umgebungsvariablen:

- `SPRING_RABBITMQ_HOST` - RabbitMQ Host
- `SPRING_RABBITMQ_PORT` - RabbitMQ Port
- `KITCHEN_PREPARATION_TIME_MIN` - Minimale Zubereitungszeit
- `KITCHEN_PREPARATION_TIME_MAX` - Maximale Zubereitungszeit

## Architektur

### Event-Verarbeitung

```
order.placed.queue
        ↓
  [Kitchen Service]
    - Event empfangen
    - Validierung
    - Zubereitungssimulation
    - Event publizieren
        ↓
order.ready.queue
```

### Dateien

```
src/main/java/com/pizza/kitchen/
├── KitchenServiceApplication.java    # Spring Boot Entry Point
├── config/
│   └── RabbitMQConfig.java          # Queue & Exchange Konfiguration
├── model/
│   ├── OrderPlacedEvent.java        # Eingehendes Event
│   └── OrderReadyEvent.java         # Ausgehendes Event
└── service/
    └── KitchenService.java          # Hauptlogik
```

## API & Monitoring

**Health Check:**

```
GET http://localhost:8082/actuator/health
```

**Info Endpoint:**

```
GET http://localhost:8082/actuator/info
```

## Skalierung

Der Service unterstützt das **Competing Consumers Pattern**:

```yaml
spring:
  rabbitmq:
    listener:
      simple:
        concurrency: 1 # Min. parallele Consumer
        max-concurrency: 3 # Max. parallele Consumer
```

Mehrere Instanzen können parallel laufen. RabbitMQ verteilt Bestellungen automatisch.

## Logging

Beispiel-Ausgabe:

```
[kitchen-1] Received order 12345 - 2 x Margherita for Max Mustermann
[kitchen-1] Preparing order 12345 - estimated time: 7234 ms
[kitchen-1] Order 12345 is ready!
[kitchen-1] Published order.ready event for order 12345
```

## Fehlerbehandlung

- Unterbrochene Verarbeitung: Wird geloggt und sauber beendet
- RabbitMQ Ausfälle: Automatisches Retry durch Spring AMQP
- Unerwartete Fehler: Vollständiges Logging mit Stack-Trace

## Abhängigkeiten

**Erforderlich:**

- RabbitMQ Server

**Optional:**

- Docker für Containerisierung

## Support

Bei Fragen oder Problemen, bitte prüfen:

1. RabbitMQ ist erreichbar
2. Queues `order.placed` und `order.ready` existieren
3. Logs für detaillierte Fehlerinformationen
