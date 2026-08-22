# ToDo Webanwendung (PE2 Projekt)

Dieses Projekt ist eine Full-Stack-Webanwendung zur Verwaltung von ToDo-Listen, die im Rahmen eines Universitätsprojekts entstanden ist. Es besteht aus einem Spring Boot Backend, das eine REST API bereitstellt, und einem Vue.js Frontend (Single-Page Application, SPA). Die Benutzeroberfläche des Frontends verwendet Deutsch für allgemeine Elemente und Englisch für Entitätsnamen (Assignees, Todos) zur besseren Klarheit. Die gesamte Anwendung ist mit Docker containerisiert und wird mit Docker Compose für eine einfache Einrichtung und Bereitstellung orchestriert.

## Verwendete Technologien

*   **Backend:** Spring Boot 3.5.16, Java 21, Maven, MariaDB (Laufzeit), H2 (Tests), JUnit 5, Springdoc OpenAPI
*   **Frontend:** Vue 3, TypeScript, Vite, Vue Router, AgnosticUI (für Komponenten), ESLint, Prettier
*   **Datenbank:** MariaDB
*   **Containerisierung:** Docker, Docker Compose
*   **Build Tools:** Maven (Backend), npm/Vite (Frontend)

## Voraussetzungen

Um dieses Projekt auszuführen, stellen Sie sicher, dass die folgende Software installiert ist:

1.  **JDK 21:** (z.B. OpenJDK, Oracle JDK)
2.  **Maven 3.x:**
3.  **Node.js (LTS empfohlen) und npm:** (z.B. von nodejs.org)
4.  **Docker Desktop:** (oder eine kompatible Docker-Umgebung für MariaDB)

## Einrichtung und Ausführung

Sie können die Anwendung entweder mit Docker Compose (empfohlen für eine vollständige Einrichtung) oder durch separates Starten von Backend und Frontend ausführen.

### Option 1: Verwendung von Docker Compose (Empfohlen)

Diese Option startet sowohl die MariaDB-Datenbank als auch das Spring Boot-Backend und baut und serviert auch das Frontend.

1.  **Navigieren Sie zum Projekt-Root-Verzeichnis:**
    ```bash
    cd pe2_todo_app
    ```
2.  **Starten Sie die Dienste:**
    ```bash
    docker-compose up --build
    ```
    Dies wird:
    *   Das Backend-Docker-Image erstellen.
    *   Das Frontend-Docker-Image erstellen.
    *   Den MariaDB-Container starten.
    *   Die Backend-Spring Boot-Anwendung starten (intern auf Port `8080` lauschend, über Nginx auf Port `80` exponiert).
    *   Den Nginx-Server für das Frontend starten (auf Port `80` des Hosts exponiert).

3.  **Greifen Sie auf die Anwendung zu:**
    Öffnen Sie Ihren Browser und navigieren Sie zu `http://localhost`.

### Option 2: Backend und Frontend separat ausführen (Entwicklung)

#### 1. MariaDB-Datenbank starten

Starten Sie die MariaDB-Datenbank mit Docker:

```bash
docker run -d --name pe2-mariadb -p 3306:3306 -e MARIADB_ROOT_PASSWORD=root -e MARIADB_DATABASE=pe2 mariadb:latest
```

#### 2. Backend starten (Spring Boot)

1.  **Navigieren Sie in das `api`-Verzeichnis:**
    ```bash
    cd api
    ```
2.  **Führen Sie die Spring Boot-Anwendung aus:**
    ```bash
    ./mvnw spring-boot:run
    ```
    Das Backend startet auf `http://localhost:8080`.

#### 3. Frontend starten (Vue.js)

1.  **Navigieren Sie in das `frontend`-Verzeichnis:**
    ```bash
    cd frontend
    ```
2.  **Abhängigkeiten installieren:**
    ```bash
    npm install
    ```
3.  **Starten Sie den Entwicklungsserver:**
    ```bash
    npm run dev
    ```
    Der Frontend-Entwicklungsserver startet typischerweise auf `http://localhost:5173`. API-Anfragen werden über eine Proxy-Konfiguration in `frontend/vite.config.ts` an das Backend auf `http://localhost:8080` weitergeleitet.

## API REST Ressourcen

Das Backend stellt die folgenden REST-Endpunkte bereit:

*   **Bei separater Ausführung:** Zugänglich unter `http://localhost:8080/api/v1/...`
*   **Bei Verwendung von Docker Compose:** Zugänglich unter `http://localhost/api/v1/...` (über Nginx geleitet)

### Assignees

| Methode | Pfad                       | Beschreibung                            |
|---------|----------------------------|-----------------------------------------|
| `GET`   | `/assignees`               | Ruft alle Assignees ab.                 |
| `GET`   | `/assignees/{id}`          | Ruft einen spezifischen Assignee ab.    |
| `POST`  | `/assignees`               | Erstellt einen neuen Assignee.          |
| `PUT`   | `/assignees/{id}`          | Aktualisiert einen bestehenden Assignee.|
| `DELETE`| `/assignees/{id}`          | Löscht einen Assignee.                  |

### ToDos

| Methode | Pfad                       | Beschreibung                            |
|---------|----------------------------|-----------------------------------------|
| `GET`   | `/todos`                   | Ruft alle ToDos ab.                     |
| `GET`   | `/todos/{id}`              | Ruft ein spezifisches ToDo ab.          |
| `POST`  | `/todos`                   | Erstellt ein neues ToDo.                |
| `PUT`   | `/todos/{id}`              | Aktualisiert ein bestehendes ToDo.      |
| `DELETE`| `/todos/{id}`              | Löscht ein ToDo.                        |

## Überprüfung der Abgabe

Um die Implementierung für AB04 zu überprüfen, folgen Sie bitte diesen Schritten:

### 1. Backend-Verifizierung

1.  **Backend Unit Tests ausführen:**
    Stellt sicher, dass die gesamte Backend-Logik korrekt ist.
    ```bash
    cd api
    ./mvnw test
    ```
    _Erwartetes Ergebnis:_ `BUILD SUCCESS` mit `Tests run: 29, Failures: 0, Errors: 0, Skipped: 0`.

2.  **Checkstyle-Analyse ausführen:**
    Überprüft die Einhaltung des Coding-Styles.
    ```bash
    cd api
    ./mvnw org.apache.maven.plugins:maven-checkstyle-plugin:3.3.1:check -Dcheckstyle.config.location=PE2CheckStyle.xml
    ```
    _Erwartetes Ergebnis:_ `BUILD SUCCESS` mit `0 errors`.

### 2. Frontend-Verifizierung

1.  **Frontend-Abhängigkeiten installieren:**
    ```bash
    npm ci --prefix frontend
    ```
    _Erwartetes Ergebnis:_ Abhängigkeiten erfolgreich installiert.

2.  **Frontend Type-Check ausführen:**
    ```bash
    npm run type-check --prefix frontend
    ```
    _Erwartetes Ergebnis:_ Keine TypeScript-Fehler.

3.  **Frontend-Linting ausführen:**
    ```bash
    npm run lint --prefix frontend
    ```
    _Erwartetes Ergebnis:_ Keine ESLint-Fehler.

4.  **Frontend für Produktion bauen:**
    ```bash
    npm run build --prefix frontend
    ```
    _Erwartetes Ergebnis:_ `BUILD SUCCESS` mit generierten Produktions-Assets in `frontend/dist/`.

### 3. Gesamte Anwendungsüberprüfung (mit Docker Compose)

1.  **Dienste bauen und starten:**
    Dies baut die Docker-Images und startet MariaDB, Backend und Frontend (Nginx).
    ```bash
    docker compose up --build
    ```
    _Erwartetes Ergebnis:_ Alle Dienste starten erfolgreich ohne Fehler.

2.  **Auf die Anwendung zugreifen:**
    Öffnen Sie Ihren Browser und navigieren Sie zu `http://localhost`.

3.  **Frontend-Funktionalität überprüfen:**
    *   **Kategorie-Anzeige:** Navigieren Sie zu den ToDo-Listen- und Detailseiten. Bestätigen Sie, dass jedes ToDo-Element seine `category` anzeigt (z.B. "work", "private").
    *   **Filtern und Sortieren:** Verwenden Sie die Suchleiste, um nach Titeln zu filtern, und das Sortier-Dropdown, um nach Priorität, Fälligkeitsdatum usw. zu sortieren.
    *   **CSV-Download:** Suchen Sie auf der ToDo-Listenseite die Schaltfläche "Download CSV" und klicken Sie darauf.
        _Erwartetes Ergebnis:_ Eine Datei namens `todos.csv` sollte heruntergeladen werden, die alle ToDo-Daten mit dem korrekten Header (`id,title,description,finished,assignees,createdDate,dueDate,finishedDate,category,priority`) und korrekt formatierten Assignee- und Datumsfeldern enthält.

4.  **Datenpersistenz überprüfen:**
    *   Erstellen Sie ein neues ToDo-Element über das Frontend.
    *   Stoppen Sie die Docker Compose-Dienste: `docker compose down`
    *   Starten Sie die Docker Compose-Dienste erneut: `docker compose up -d`
    *   Aktualisieren Sie das Frontend in Ihrem Browser.
    _Erwartetes Ergebnis:_ Das vor dem Neustart der Dienste erstellte ToDo-Element sollte weiterhin vorhanden sein, was die Datenpersistenz beweist.
