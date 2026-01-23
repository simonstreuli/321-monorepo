# 🎤 Präsentations-Anleitung

Diese Datei erklärt, wie Sie die Präsentationen für das **Distributed Pizza Platform** Projekt nutzen können.

## 📁 Verfügbare Präsentations-Dateien

### 1. `PRESENTATION.md`
**Format:** Standard Markdown mit Folien-Trennern (`---`)

**Geeignet für:**
- Lesen im Editor (VS Code, etc.)
- Als Handout zum Verteilen
- Konvertierung in andere Formate
- GitHub-Anzeige

**Verwendung:**
- Direkt in jedem Markdown-fähigen Editor öffnen
- Auf GitHub direkt anzeigen lassen

### 2. `PRESENTATION_SLIDES.md`
**Format:** Marp-Markdown (Markdown Presentation Ecosystem)

**Geeignet für:**
- Live-Präsentationen
- Professionelle Slides mit Formatierung
- Export zu PDF/HTML/PowerPoint

**Verwendung:** Siehe unten für Marp-Anleitung

## 🚀 Marp-Präsentation verwenden

### Option 1: VS Code Extension (Empfohlen)

1. **Marp Extension installieren:**
   - In VS Code: Extensions → Suche nach "Marp for VS Code"
   - Oder direkt: https://marketplace.visualstudio.com/items?itemName=marp-team.marp-vscode

2. **Präsentation öffnen:**
   - `PRESENTATION_SLIDES.md` in VS Code öffnen

3. **Vorschau starten:**
   - Klick auf "Open Preview" Icon (oben rechts)
   - Oder: `Cmd/Ctrl + K, V`

4. **Vollbild-Präsentation:**
   - In der Vorschau: Klick auf "Slide Mode" Icon
   - Oder: `F5` in der Vorschau

5. **Export zu PDF/HTML:**
   - Command Palette: `Cmd/Ctrl + Shift + P`
   - Tippe: "Marp: Export slide deck"
   - Wähle Format: PDF, HTML, PPTX

### Option 2: Marp CLI (Kommandozeile)

1. **Installation:**
```bash
npm install -g @marp-team/marp-cli
```

2. **HTML generieren:**
```bash
marp PRESENTATION_SLIDES.md -o presentation.html
```

3. **PDF generieren:**
```bash
marp PRESENTATION_SLIDES.md -o presentation.pdf
```

4. **Live-Server starten:**
```bash
marp -s PRESENTATION_SLIDES.md
```
Öffnet http://localhost:8080

### Option 3: Online (Marp Web)

Besuche https://web.marp.app und lade `PRESENTATION_SLIDES.md` hoch.

## 📊 Präsentations-Struktur

### Hauptthemen

1. **Einführung** (Folie 1-3)
   - Projektübersicht
   - Team-Vorstellung

2. **Architektur** (Folie 4-6)
   - System-Diagramm
   - Bestellablauf
   - Design Patterns

3. **Technologie** (Folie 7-8)
   - Tech-Stack
   - Resilience Features

4. **Demo-Szenarien** (Folie 9-11)
   - Payment Service offline
   - Kitchen Service offline
   - Horizontale Skalierung

5. **API & Monitoring** (Folie 12-14)
   - API Contracts
   - Events
   - Monitoring Tools

6. **Testing & Betrieb** (Folie 15-17)
   - Integration Tests
   - Installation & Start
   - Shared Models

7. **Zusammenfassung** (Folie 18-21)
   - Key Features
   - Lessons Learned
   - Erweiterungen
   - Architektur-Highlights

8. **Demo & Q&A** (Folie 22-25)
   - Live Demo
   - Fragen
   - Danke

## 🎨 Präsentations-Tipps

### Zeitplanung (30 Minuten)
- **5 Min:** Einführung & Team (Folien 1-3)
- **8 Min:** Architektur & Patterns (Folien 4-8)
- **10 Min:** Live-Demo (Folien 9-11, 22)
- **5 Min:** Monitoring & Testing (Folien 12-17)
- **2 Min:** Zusammenfassung (Folien 18-21)
- **5 Min:** Q&A (Folie 24)

### Zeitplanung (15 Minuten)
- **3 Min:** Einführung (Folien 1-3)
- **5 Min:** Architektur (Folien 4-6)
- **5 Min:** Live-Demo (Folie 22)
- **2 Min:** Q&A

### Vorbereitung

**Vor der Präsentation:**
1. System starten: `docker compose up --build`
2. Frontend testen: http://localhost:3000
3. RabbitMQ UI öffnen: http://localhost:15672
4. Terminal für Demo-Commands bereit haben

**Demo-Commands vorbereiten:**
```bash
# In separater Terminal-Datei speichern
# 1. Erfolgreiche Bestellung
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"pizza": "Margherita", "quantity": 2, "address": "Demo-Strasse 1", "customerName": "Demo User"}'

# 2. Payment Service stoppen
docker compose stop payment-service

# 3. Kitchen Service offline/online
docker compose stop kitchen-service
# ... Bestellungen senden ...
docker compose start kitchen-service

# 4. Skalierung
docker compose up --scale kitchen-service=3 -d
```

### Live-Demo-Ablauf

1. **Happy Path zeigen:**
   - Frontend öffnen
   - Bestellung aufgeben
   - Status in Echtzeit verfolgen
   - RabbitMQ UI zeigen (Messages)

2. **Resilience demonstrieren:**
   - Payment Service stoppen
   - Bestellung versuchen → Fehlermeldung
   - Payment Service wieder starten

3. **Message Buffering zeigen:**
   - Kitchen Service stoppen
   - 3-5 Bestellungen senden
   - Kitchen Service starten
   - Logs beobachten

4. **Skalierung demonstrieren:**
   - 3 Kitchen Instances starten
   - 10 parallele Bestellungen
   - RabbitMQ UI: Load Distribution zeigen
   - Logs: Verschiedene Instance IDs

## 📝 Notizen für Präsentatoren

### Wichtige Punkte hervorheben

- **Asynchrone Kommunikation:** Hauptvorteil ist Entkopplung
- **RabbitMQ Durable Queues:** Kein Datenverlust möglich
- **Competing Consumers:** Automatisches Load Balancing
- **Shared Models:** Verhindert Breaking Changes

### Häufige Fragen

**Q: Warum nicht nur REST für alles?**
A: Asynchrone Kommunikation entkoppelt Services, ermöglicht Pufferung und bessere Skalierung.

**Q: Was passiert, wenn RabbitMQ ausfällt?**
A: Order Service kann keine Events publizieren. Mit Durable Queues sind bereits gesendete Messages sicher.

**Q: Wie wird die Reihenfolge von Messages garantiert?**
A: RabbitMQ garantiert FIFO innerhalb einer Queue pro Consumer.

**Q: Kann man das in Production einsetzen?**
A: Ja, mit Erweiterungen wie Kubernetes, Monitoring (Prometheus), Tracing (Zipkin) und Authentication.

## 🔗 Weitere Ressourcen

- **Marp Dokumentation:** https://marp.app
- **Projekt README:** `README.md`
- **Architektur-Details:** `ARCHITECTURE.md`
- **Integration Tests:** `INTEGRATION_TESTS.md`
- **Repository:** https://github.com/simonstreuli/321-monorepo

## 💡 Tipps für verschiedene Zielgruppen

### Technisches Publikum
- Fokus auf Patterns (Event-Driven, Competing Consumers)
- Code-Beispiele zeigen (Event-Strukturen)
- RabbitMQ Management UI im Detail
- Resilience-Szenarien ausführlich

### Business-Publikum
- Fokus auf Vorteile (Skalierbarkeit, Verfügbarkeit)
- Frontend-Demo im Vordergrund
- Weniger technische Details
- Use Cases betonen

### Studenten / Lernende
- Patterns erklären (warum, nicht nur wie)
- Live-Coding oder Config-Änderungen zeigen
- Troubleshooting demonstrieren
- Lessons Learned ausführlich besprechen

---

**Viel Erfolg bei Ihrer Präsentation! 🚀**
