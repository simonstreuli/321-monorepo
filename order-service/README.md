# Dokumentation

## Übersicht

Dieses Verzeichnis enthält den Order-Service. Der Service ist mit Java und Spring Boot implementiert.

## API Versioning

Der Order-Service unterstützt jetzt mehrere API-Versionen:
- **Legacy**: `POST /orders` (für Rückwärtskompatibilität)
- **V1**: `POST /api/v1/orders` (gleiche Funktionalität wie Legacy)
- **V2**: `POST /api/v2/orders` (erweiterte Response mit Metadaten)

Siehe [API_VERSIONING.md](./API_VERSIONING.md) für Details.

## Docker Pipeline

Der Service verfügt über eine automatisierte GitHub Actions Pipeline, die Docker-Images baut und zu GitHub Container Registry pusht. Die Pipeline wird automatisch bei Änderungen an `order-service/**` oder `pizza-models/**` ausgeführt.

## Projektstruktur

- `src/main/java/com/pizza/order/` – Java-Quellcode
  - `controller/` – Legacy Controller (Rückwärtskompatibilität)
  - `controller/v1/` – V1 API Controller
  - `controller/v2/` – V2 API Controller (mit erweiterten Features)
- `src/main/resources/` – Ressourcen und Konfigurationsdateien
- `Dockerfile` – Docker-Konfiguration
- `pom.xml` – Maven Build-Konfiguration
- `API_VERSIONING.md` – Detaillierte API-Versionierungsdokumentation

## Starten des Order-Service

1. Voraussetzungen:
   - Java 17 oder höher
   - Maven
   - Docker (optional)
2. Build und Start:
   - Mit Maven: `mvn clean install && mvn spring-boot:run`
   - Mit Docker: `docker build -t order-service .` und dann `docker run -p 8080:8080 order-service`

## API-Endpunkte

Der Order-Service stellt folgende REST-API-Endpunkte zur Verfügung:

### POST /orders (Legacy)

Erstellt eine neue Bestellung. Erwartet ein JSON-Objekt mit den Bestelldaten (Pizza, Menge, Adresse etc.).

### POST /api/v1/orders

Erstellt eine neue Bestellung über die V1 API. Gleiche Funktionalität wie Legacy-Endpoint.

### POST /api/v2/orders

Erstellt eine neue Bestellung über die V2 API. Gibt eine erweiterte Response mit zusätzlichen Metadaten zurück (apiVersion, timestamp).

Für alle Endpunkte:
Beispiel-Request:

```json
{
  "pizza": "Margherita",
  "quantity": 2,
  "address": "Musterstrasse 1, 8000 Zürich"
}
```

Antwort: JSON mit Status und ggf. Fehler-/Erfolgsmeldung.

### GET /orders/health

Liefert einen einfachen Status-String, um zu prüfen, ob der Service läuft.

### Fehlerbehandlung

Bei ungültigen Requests oder internen Fehlern werden entsprechende Fehlermeldungen und Statuscodes zurückgegeben (z.B. 400 für Validierungsfehler, 500 für interne Fehler).

## Aufgaben des Order-Service

Der Order-Service nimmt Bestellungen entgegen, prüft die Eingabedaten, verarbeitet die Bestellung und stößt die Kommunikation mit den anderen Services (Payment, Kitchen, Delivery) über RabbitMQ an. Er ist das zentrale Bindeglied für neue Bestellungen im System.

## Konfiguration

Die Konfiguration erfolgt über `src/main/resources/application.yml`.
.

## Integration mit Payment, Delivery und Kitchen Service

Der Order-Service ist Teil eines grösseren Microservice-Systems, das ausserdem folgende Services umfasst:

- **Payment Service**
- **Delivery Service**
- **Kitchen Service**

Die Kommunikation zwischen diesen Services erfolgt asynchron über RabbitMQ (Message Broker). Dadurch können die Services unabhängig voneinander skalieren und sind lose gekoppelt.

### Verbindung und Kommunikation

1. **RabbitMQ-Setup:**

   - Stelle sicher, dass ein RabbitMQ-Server läuft und die Verbindungsdaten in der jeweiligen `application.yml` der Services korrekt eingetragen sind.
   - Beispiel-Konfiguration im `order-service/src/main/resources/application.yml`:
     ```yaml
     spring:
       rabbitmq:
         host: localhost
         port: 5672
         username: guest
         password: guest
     ```

2. **Austausch von Nachrichten:**

   - Der Order-Service sendet nach einer Bestellung ein Event (z.B. `OrderPlacedEvent`) an RabbitMQ.
   - Die anderen Services (Payment, Delivery, Kitchen) abonnieren die jeweiligen Queues/Topics und reagieren auf diese Events.
   - Beispiel: Der Payment Service empfängt das Event, prüft die Zahlung und sendet das Ergebnis zurück.

3. **Queues und Exchanges:**
   - Jeder Service hat eigene Queues und kann über Exchanges (z.B. `order.exchange`) Events empfangen oder senden.
   - Die genaue Queue- und Exchange-Konfiguration ist in den jeweiligen Service-Konfigurationsdateien dokumentiert.

Weitere Details zur Integration und zu den Events finden sich in den jeweiligen Service-Repositories.
