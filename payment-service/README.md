# Payment Service

Ein microservice für die Verarbeitung von Zahlungen in der Pizza-Lieferplattform. Der Service verarbeitet Zahlungsanfragen, simuliert Transaktionen und bietet Resilience-Features wie konfigurierbare Fehlerquoten und Verarbeitungsverzögerungen.

## Übersicht

Der Payment Service ist ein Spring Boot 3.2-Microservice, der folgende Funktionalität bereitstellt:

- **Zahlungsverarbeitung**: Verarbeitet Zahlungsanfragen mit Validierung
- **Transaktions-IDs**: Generiert eindeutige Transaktions-IDs für jede Zahlung
- **Resilience-Tests**: Simuliert zufällige Zahlungsausfälle und Verzögerungen
- **Health-Checks**: Bietet einen Health-Endpoint für Monitoring
- **Fehlerbehandlung**: Umfassende Exception-Handling mit aussagekräftigen Fehlermeldungen

## Technologie-Stack

- **Java 21**
- **Spring Boot 3.2.1**
- **Maven 3.9**
- **Lombok** (für Code-Vereinfachung)
- **Spring Boot Actuator** (für Monitoring)
- **Spring Validation** (für Input-Validierung)

## Voraussetzungen

- Java 21 oder höher
- Maven 3.6+
- Docker (optional, für Container-Deployment)

## Installation und Setup

### Lokal ausführen

1. **Abhängigkeiten installieren**
   ```bash
   mvn clean install
   ```

2. **Service starten**
   ```bash
   mvn spring-boot:run
   ```

   Der Service startet dann auf Port **8081**

### Mit Docker

1. **Docker-Image bauen**
   ```bash
   docker build -t payment-service:1.0.0 .
   ```

2. **Container starten**
   ```bash
   docker run -p 8081:8081 payment-service:1.0.0
   ```

## Konfiguration

Die Konfiguration erfolgt über die `application.yml` Datei:

```yaml
server:
  port: 8081                    # Port des Services

spring:
  application:
    name: payment-service      # Name der Anwendung

payment:
  failure:
    rate: 0.2                   # 20% Fehlerquote für Test-Simulation
  delay:
    min: 100                    # Minimale Verarbeitungsverzögerung in ms
    max: 500                    # Maximale Verarbeitungsverzögerung in ms

logging:
  level:
    com.pizza.payment: INFO     # Log-Level
```

### Umgebungsvariablen

Die Konfiguration kann auch via Umgebungsvariablen überschrieben werden:

```bash
# Fehlerquote ändern (z.B. 30%)
export PAYMENT_FAILURE_RATE=0.3

# Verzögerung ändern
export PAYMENT_DELAY_MIN=50
export PAYMENT_DELAY_MAX=1000
```

## API-Endpoints

### 1. Zahlung verarbeiten

**POST** `/pay`

Verarbeitet eine Zahlungsanfrage und gibt das Transaktionsergebnis zurück.

**Request-Body:**
```json
{
  "orderId": "ORD-12345",
  "customerName": "Max Mustermann",
  "amount": 29.99
}
```

**Response (Erfolg - HTTP 200):**
```json
{
  "transactionId": "TXN-550e8400-e29b-41d4-a716-446655440000",
  "success": true,
  "message": "Payment processed successfully"
}
```

**Response (Fehler - HTTP 402):**
```json
{
  "transactionId": null,
  "success": false,
  "message": "Payment failed (simulated failure)"
}
```

**Validierungsregeln:**
- `orderId`: Erforderlich, darf nicht leer sein
- `customerName`: Erforderlich, darf nicht leer sein
- `amount`: Erforderlich, muss positiv sein (> 0)

**Response (Validierungsfehler - HTTP 400):**
```json
{
  "orderId": "Order ID is required",
  "amount": "Amount must be positive"
}
```

### 2. Health-Check

**GET** `/health`

Prüft, ob der Service läuft und bereit ist.

**Response (HTTP 200):**
```
Payment Service is running
```

## Verwendungsbeispiele

### Mit curl

```bash
# Zahlung verarbeiten
curl -X POST http://localhost:8081/pay \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD-12345",
    "customerName": "Max Mustermann",
    "amount": 29.99
  }'

# Health-Check
curl http://localhost:8081/health
```

### Mit Java/Spring (RestTemplate)

```java
RestTemplate restTemplate = new RestTemplate();

PaymentRequest request = new PaymentRequest(
    "ORD-12345",
    "Max Mustermann",
    29.99
);

ResponseEntity<PaymentResponse> response = restTemplate.postForEntity(
    "http://localhost:8081/pay",
    request,
    PaymentResponse.class
);

if (response.getStatusCode() == HttpStatus.OK && 
    response.getBody().isSuccess()) {
    System.out.println("Zahlung erfolgreich: " + 
        response.getBody().getTransactionId());
}
```

## Datenmodelle

### PaymentRequest

| Feld | Typ | Erforderlich | Beschreibung |
|------|-----|------------|-------------|
| `orderId` | String | Ja | Eindeutige Bestellungs-ID |
| `customerName` | String | Ja | Name des Kunden |
| `amount` | double | Ja | Zahlungsbetrag in Euro (muss > 0 sein) |

### PaymentResponse

| Feld | Typ | Beschreibung |
|------|-----|-------------|
| `transactionId` | String | Eindeutige Transaktions-ID (UUID) |
| `success` | boolean | Gibt an, ob die Zahlung erfolgreich war |
| `message` | String | Statusmeldung oder Fehlermeldung |

## Fehlerbehandlung

Der Service behandelt verschiedene Fehlerszenarien:

1. **Validierungsfehler (HTTP 400)**
   - Ungültige oder fehlende Eingabefelder
   - Detaillierte Fehlermeldungen pro Feld

2. **Zahlungsfehler (HTTP 402)**
   - Simulation von Zahlungsausfällen basierend auf `failure.rate`
   - Kann durch Konfiguration angepasst werden

3. **Interne Fehler (HTTP 500)**
   - Unerwartete Server-Fehler
   - Werden geloggt mit vollständigem Stack-Trace

## Monitoring und Logging

Der Service nutzt **SLF4J** mit folgenden Log-Leveln:

- **INFO**: Zahlungsanfragen und -ergebnisse
- **WARN**: Fehlgeschlagene Zahlungen
- **ERROR**: Unerwartete Fehler
- **DEBUG**: Verarbeitungsverzögerungen

Log-Output Beispiel:
```
2024-01-16 10:30:45.123  INFO  c.p.payment.controller.PaymentController : Received payment request for order ORD-12345
2024-01-16 10:30:45.456  INFO  c.p.payment.service.PaymentService : Processing payment for order ORD-12345 with amount 29.99
2024-01-16 10:30:45.678  DEBUG c.p.payment.service.PaymentService : Payment processing delayed by 234 ms
2024-01-16 10:30:45.912  INFO  c.p.payment.service.PaymentService : Payment processed successfully for order ORD-12345
```

## Verzeichnisstruktur

```
payment-service/
├── Dockerfile              # Docker-Build-Datei (Multi-Stage)
├── pom.xml                 # Maven-Konfiguration
├── README.md              # Diese Datei
└── src/
    └── main/
        ├── java/com/pizza/payment/
        │   ├── PaymentServiceApplication.java   # Spring Boot Entry Point
        │   ├── controller/
        │   │   └── PaymentController.java       # REST-Controller
        │   ├── model/
        │   │   ├── PaymentRequest.java          # Input-Modell
        │   │   └── PaymentResponse.java         # Output-Modell
        │   └── service/
        │       └── PaymentService.java          # Business-Logik
        └── resources/
            └── application.yml                   # Anwendungs-Konfiguration
```

## Troubleshooting

### Port 8081 ist bereits in Verwendung

```bash
# Unter Linux/Mac: Prozess auf Port 8081 finden und beenden
lsof -i :8081
kill -9 <PID>

# Unter Windows:
netstat -ano | findstr :8081
taskkill /PID <PID> /F

# Oder anderen Port in application.yml setzen:
# server.port: 8082
```

### Beispiel: Integration mit Order Service

```java
@Service
public class OrderService {
    
    private final PaymentClient paymentClient;
    
    public void createOrder(Order order) {
        // Zahlung verarbeiten
        PaymentResponse paymentResponse = paymentClient.processPayment(
            new PaymentRequest(
                order.getId(),
                order.getCustomerName(),
                order.getTotalAmount()
            )
        );
        
        if (paymentResponse.isSuccess()) {
            // Bestellung bestätigen
            order.setTransactionId(paymentResponse.getTransactionId());
            saveOrder(order);
        } else {
            throw new PaymentException("Payment failed");
        }
    }
}
```

## Build und Deployment

### Maven Build

```bash
# Tests ausführen und JAR bauen
mvn clean package

# JAR direkt ausführen
java -jar target/payment-service-1.0.0.jar
```

### Docker Deployment

Das Dockerfile nutzt ein **Multi-Stage-Build-Pattern**:

1. **Build-Stage**: Maven kompiliert den Code
2. **Runtime-Stage**: Nur die JRE wird verwendet (schlankeres Image)

```bash
# Image bauen
docker build -t payment-service:1.0.0 .

# Container mit Port-Mapping starten
docker run -p 8081:8081 \
  -e PAYMENT_FAILURE_RATE=0.1 \
  payment-service:1.0.0
```

## Lizenz

Teil des Pizza-Lieferplattform-Projekts (M321)

## Support und Fragen

Bei Fragen oder Problemen:
1. Log-Dateien überprüfen
2. Health-Endpoint testen: `curl http://localhost:8081/health`
3. Konfiguration in `application.yml` überprüfen
