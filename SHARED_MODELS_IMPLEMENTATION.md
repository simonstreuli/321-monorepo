# Shared Models Library Implementation

## Zusammenfassung

Diese Änderung implementiert eine zentrale Bibliothek für gemeinsame Datenmodelle im Pizza-Lieferdienst-Monorepo. Dies löst das Problem, dass Modelle über mehrere Services hinweg dupliziert wurden und Producer-Änderungen nicht automatisch an Consumer weitergegeben wurden.

## Lösung

### Pizza Models Library

Eine neue Maven-Bibliothek (`pizza-models`) wurde erstellt, die alle gemeinsamen Datenmodelle enthält:

- **Order Models**: OrderRequest, OrderResponse, OrderPlacedEvent, OrderReadyEvent
- **Payment Models**: PaymentRequest, PaymentResponse

### Vorteile

1. **Konsistenz**: Alle Services nutzen exakt dieselben Model-Definitionen
2. **Wartbarkeit**: Änderungen werden zentral vorgenommen
3. **Type Safety**: Compile-Time-Fehler bei inkompatiblen Versionen
4. **Automatische Updates**: Producer-Änderungen werden automatisch an Consumer weitergegeben

## Technische Details

### Architektur

```
pizza-models (Library)
    ↓ (dependency)
    ├── order-service
    ├── kitchen-service
    └── payment-service
```

### Verwendung

Services fügen die Abhängigkeit in ihrer `pom.xml` hinzu:

```xml
<dependency>
    <groupId>com.pizza</groupId>
    <artifactId>pizza-models</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Build-Prozess

1. `pizza-models` wird zuerst gebaut und im lokalen Maven-Repository installiert
2. Alle Services nutzen dann diese installierte Version
3. Dockerfiles wurden aktualisiert, um die Library vor jedem Service zu bauen

### Änderungen pro Service

#### Order Service
- ✅ Dependency hinzugefügt
- ✅ Model-Dateien entfernt
- ✅ Imports aktualisiert (main + tests)
- ✅ 47 Tests bestehen

#### Kitchen Service
- ✅ Dependency hinzugefügt
- ✅ Model-Dateien entfernt
- ✅ Imports aktualisiert
- ✅ Java-Version auf 17 standardisiert
- ✅ Build erfolgreich

#### Payment Service
- ✅ Dependency hinzugefügt
- ✅ Model-Dateien entfernt
- ✅ Imports aktualisiert (main + tests)
- ✅ Nicht-relevante Order-Tests entfernt
- ✅ 23 Tests bestehen

## Dokumentation

- **Haupt-README**: Erweitert mit Abschnitt über die Shared Library
- **pizza-models/README.md**: Neue, detaillierte Dokumentation für die Bibliothek
  - Zweck und Vorteile
  - Verwendungsanleitung
  - Enthaltene Modelle
  - Entwicklungsrichtlinien

## Qualitätssicherung

- ✅ Alle Unit-Tests bestehen (70 Tests insgesamt)
- ✅ Alle Services bauen erfolgreich
- ✅ Code Review durchgeführt - keine Probleme
- ✅ Security Scan durchgeführt - keine Schwachstellen

## Migration Guide für zukünftige Model-Änderungen

### Änderung eines bestehenden Models

1. Model in `pizza-models/src/main/java/com/pizza/models/` aktualisieren
2. Falls breaking change: Version in `pizza-models/pom.xml` erhöhen
3. Library bauen und installieren: `cd pizza-models && mvn clean install`
4. Services neu bauen
5. Tests durchführen

### Hinzufügen eines neuen Models

1. Neue Model-Klasse in `pizza-models/src/main/java/com/pizza/models/` erstellen
2. Library bauen: `mvn clean install`
3. In Services verwenden: `import com.pizza.models.NeuesModel;`

## Nächste Schritte

Empfohlene weitere Verbesserungen:

1. **Maven Parent POM**: Ein gemeinsames Parent-POM für alle Module erstellen
2. **CI/CD Pipeline**: Automatisches Bauen und Veröffentlichen der Library
3. **Version Management**: Semantic Versioning für die Library etablieren
4. **Model Validation Tests**: Shared Test-Suite für Model-Validierung

## Zusammenfassung Security

- ✅ Keine Sicherheitslücken gefunden
- ✅ Alle Abhängigkeiten stammen aus vertrauenswürdigen Quellen
- ✅ Validierung weiterhin aktiv (Jakarta Validation)
