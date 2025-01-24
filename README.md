# CAM-3FA081-_-G- HausFix

Hausfix ist eine Java-basierte Anwendung, die ein vollständiges CRUD-System (Create, Read, Update, Delete) mit REST-API-Unterstützung bietet. Es richtet sich an die Verwaltung von Kundendaten und Zählerständen, einschließlich der Integration einer relationalen Datenbank.

---

## **Inhalt**

1. [Projektbeschreibung](#projektbeschreibung)
2. [Technologien](#technologien)
3. [Projektstruktur](#projektstruktur)
4. [Installation und Einrichtung](#installation-und-einrichtung)
5. [Funktionen](#funktionen)
6. [REST-API-Endpunkte](#rest-api-endpunkte)
7. [Tests](#tests)
8. [Weiterentwicklung](#weiterentwicklung)

---

## **Projektbeschreibung**
Hausfix bietet eine Plattform für die Verwaltung von Kundendaten und die Verarbeitung von Zählerständen. Es nutzt eine REST-API zur Kommunikation und ermöglicht eine flexible Datenverarbeitung durch CRUD-Operationen.

---

## **Technologien**

- **Programmiersprache**: Java
- **Build-Tool**: Maven
- **Datenbank**: H2 (In-Memory)
- **Frameworks/Bibliotheken**: JUnit, Java HTTP Server
- **JSON-Validierung**: JSON Schema

---

## **Projektstruktur**

```
hausfix/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── hausfix/CRUD/              # CRUD-Operationen
│   │   │   ├── hausfix/Database/          # Datenbank-Management
│   │   │   ├── hausfix/entities/          # Datenmodelle
│   │   │   ├── hausfix/enums/             # Enumerationen
│   │   │   ├── hausfix/interfaces/        # Schnittstellen
│   │   │   ├── hausfix/rest/              # REST-API-Implementierung
│   │   │   └── hausfix/resourcen/         # Hilfsklassen und Ressourcen
│   │   ├── resources/                     # Konfigurationsdateien
│   ├── test/                              # Unit- und Integrationstests
├── target/                                # Build-Artefakte
├── .git/                                  # Git-Versionierung
├── pom.xml                                # Maven-Build-Datei
└── README.md                              # Dokumentation
```

---

## **Installation und Einrichtung**

### Voraussetzungen:
- Java 17 oder höher
- Maven 3.6 oder höher
- Ein Texteditor/IDE (z. B. IntelliJ IDEA)

### Schritte:
1. **Repository klonen**:
   ```bash
   git clone <repository-url>
   cd hausfix
   ```

2. **Abhängigkeiten installieren**:
   ```bash
   mvn clean install
   ```

3. **Anwendung starten**:
   ```bash
   mvn exec:java -Dexec.mainClass="hausfix.Main"
   ```

---

## **Funktionen**

1. **Kundenverwaltung**:
    - Hinzufügen, Bearbeiten, Löschen und Anzeigen von Kunden.

2. **Zählerstandsverwaltung**:
    - Verwalten von Zählerständen mit Typen (Strom, Wasser etc.).

3. **REST-API**:
    - Ermöglicht externe Interaktionen mit den Daten über Endpunkte.

4. **Datenbankmanagement**:
    - Automatische Tabellenverwaltung und Trunkierung.

---

## **REST-API-Endpunkte**

### Kunden:
- **GET** `/customers`: Liste aller Kunden abrufen
- **POST** `/customers`: Neuen Kunden hinzufügen
- **GET** `/customers/{id}`: Kunde mit spezifischer ID abrufen
- **PUT** `/customers/{id}`: Bestehenden Kunden aktualisieren
- **DELETE** `/customers/{id}`: Kunde löschen

### Zählerstände:
- **GET** `/readings`: Liste aller Zählerstände abrufen
- **POST** `/readings`: Neuen Zählerstand hinzufügen
- **GET** `/readings/{id}`: Zählerstand mit spezifischer ID abrufen
- **PUT** `/readings/{id}`: Zählerstand aktualisieren
- **DELETE** `/readings/{id}`: Zählerstand löschen

---

## **Tests**

Tests wurden mit JUnit implementiert und decken folgende Bereiche ab:
1. **CRUD-Operationen**
2. **REST-API-Endpunkte**
3. **Datenbank-Interaktionen**

Zum Ausführen der Tests:
```bash
mvn test
```

---

## **Weiterentwicklung**

### TODO's:
- Implementierung eines Frontends mit einem Framework wie React oder Angular.
- Integration eines Benutzer-Authentifizierungssystems.

---

**Kontakt:** Bei Fragen oder Vorschlägen wenden Sie sich an das Entwicklungsteam. Chris, Alper und Max.

