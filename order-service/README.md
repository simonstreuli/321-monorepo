# Dokumentation

## Übersicht

Dieses Verzeichnis enthält den Order-Service. Der Service ist mit Java und Spring Boot implementiert.

## Projektstruktur

- `src/main/java/com/pizza/order/` – Java-Quellcode
- `src/main/resources/` – Ressourcen und Konfigurationsdateien
- `Dockerfile` – Docker-Konfiguration
- `pom.xml` – Maven Build-Konfiguration

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

### POST /orders

Erstellt eine neue Bestellung. Erwartet ein JSON-Objekt mit den Bestelldaten (Pizza, Menge, Adresse etc.).
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
