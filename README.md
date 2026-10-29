# ToDo Webanwendung (PE2 Projekt)

[![Build](https://github.com/daniilperkin-uni/pe2_todo_app/actions/workflows/build.yaml/badge.svg)](https://github.com/daniilperkin-uni/pe2_todo_app/actions/workflows/build.yaml)
[![Lint](https://github.com/daniilperkin-uni/pe2_todo_app/actions/workflows/lint.yaml/badge.svg)](https://github.com/daniilperkin-uni/pe2_todo_app/actions/workflows/lint.yaml)

Full-Stack-Webanwendung zur Verwaltung von ToDos und Assignees.
Ein Spring Boot Backend stellt eine REST API bereit, ein Vue 3 Frontend (SPA) nutzt sie.
Mit Docker Compose läuft alles inklusive MariaDB mit einem Befehl.

## Live-Ansicht

Auf der Uni-Showcase-Website gibt es eine interaktive Projektseite mit Screenshots und dem Klassifikator als Live-Widget:
<https://daniilperkin-uni.github.io/uni-old-projects/#pe2_todo_app>

## Screenshots

Aufgenommen aus der laufenden Anwendung (Demo-Daten: fiktive Assignees und ToDos).

| ToDo-Liste (hell) | Kanban-Board (dunkel) |
| --- | --- |
| ![ToDo-Liste mit Filtern, Prioritäten und Fälligkeiten](docs/screenshots/todos-light.webp) | ![Kanban-Board mit OPEN, IN_PROGRESS und DONE](docs/screenshots/board-dark.webp) |

| Statistiken | Assignees |
| --- | --- |
| ![Statistik-Dashboard mit Abschlussquote und Verteilungen](docs/screenshots/stats-light.webp) | ![Assignee-Verwaltung mit E-Mail-Domain-Prüfung](docs/screenshots/assignees-light.webp) |

## Features

- ToDos und Assignees verwalten (CRUD), filtern, sortieren und als CSV exportieren
- Kanban-Status (`OPEN`, `IN_PROGRESS`, `DONE`)
- Wiederkehrende ToDos (`WEEKLY`, `MONTHLY`): beim Abschließen entsteht das nächste ToDo
- Statistiken: Abschlussquote, durchschnittliche Dauer, Verteilung nach Priorität, Kategorie und Assignee
- Klassifikator: ein PMML-Modell ordnet jedes ToDo `work` oder `private` zu
- Prioritäts-Feedback: Korrekturen werden gespeichert und ausgewertet
- Assignee-E-Mails müssen auf eine Uni-Stuttgart-Domain enden (`stud.`, `iste.`, `ipvs.`, `sec.`), geprüft in Backend und Formular
- Helles und dunkles Design mit Umschalter im Header; ohne eigene Wahl folgt die App der Systemeinstellung
- Health- und Prometheus-Endpunkte über Spring Boot Actuator

## Technologien

- **Backend:** Spring Boot 4.1, Java 21, Maven, springdoc-openapi 3, JPMML, MariaDB (Laufzeit), H2 (Tests)
- **Frontend:** Vue 3, TypeScript, Vite, Vue Router, AgnosticUI, Vitest, ESLint
- **Betrieb:** Docker, Docker Compose, Nginx

## Schnellstart mit Docker Compose

```bash
cp .env.example .env   # einmalig: DB_USERNAME und DB_PASSWORD eintragen
docker compose up --build
```

Das startet MariaDB, das Backend und Nginx mit dem gebauten Frontend.
Ohne `.env` bricht `docker compose` mit einer klaren Meldung ab, weil es keine
hartcodierten Zugangsdaten mehr gibt.
Die App läuft dann unter <http://localhost>, die API unter `http://localhost/api/v1/...`.
Daten bleiben über `docker compose down` / `up` hinweg erhalten.

`DB_USERNAME` muss in dieser Betriebsart `root` bleiben: der MariaDB-Container
legt nur das Root-Konto an (`MARIADB_ROOT_PASSWORD`), weitere Datenbankbenutzer
existieren dort nicht. Gegen eine eigene Datenbank außerhalb von Compose ist
jeder vorhandene Benutzer möglich.
Der Health-Endpunkt ist über <http://localhost/actuator/health> erreichbar.
Die Prometheus-Metriken bleiben bewusst im Compose-Netz
(`http://api:8080/actuator/prometheus`), damit ein öffentliches Deployment keine
Interna preisgibt.

## Lokale Entwicklung

Voraussetzungen: JDK 21, Node.js 22 und Docker (für MariaDB).

```bash
docker run -d --name pe2-mariadb -p 3306:3306 \
  -e MARIADB_ROOT_PASSWORD=changeme -e MARIADB_DATABASE=pe2 mariadb:11.8
export DB_USERNAME=root DB_PASSWORD=changeme   # required, no defaults in the app
cd api && ./mvnw spring-boot:run        # http://localhost:8080
cd frontend && npm install && npm run dev  # http://localhost:5173
```

Der Vite-Dev-Server leitet `/api` an `http://localhost:8080` weiter (`frontend/vite.config.ts`).
Die OpenAPI-Dokumentation liegt unter <http://localhost:8080/swagger-ui>.

## REST API

Alle Pfade liegen unter `/api/v1`.

| Methode | Pfad | Beschreibung |
| --- | --- | --- |
| `GET`, `POST` | `/assignees` | Assignees auflisten / anlegen |
| `GET`, `PUT`, `DELETE` | `/assignees/{id}` | Assignee lesen / ändern / löschen |
| `GET`, `POST` | `/todos` | ToDos auflisten / anlegen (mit Klassifikation) |
| `GET`, `PUT`, `DELETE` | `/todos/{id}` | ToDo lesen / ändern / löschen |
| `PATCH` | `/todos/{id}/status` | Kanban-Status setzen |
| `GET` | `/todos/stats` | Statistiken |
| `POST` | `/todos/priority-corrections` | Prioritätskorrektur speichern |
| `GET` | `/todos/priority-corrections/stats` | Korrekturstatistik |
| `GET` | `/csv-downloads/todos` | CSV-Export |

## Qualitätsprüfungen

```bash
cd api && ./mvnw verify   # Tests, Checkstyle (PE2CheckStyle.xml), JaCoCo >= 80 %
cd frontend && npm ci && npm run lint:ci && npm run format:check && npm run type-check && npm test && npm run build
```

Die gleichen Prüfungen laufen in GitHub Actions (`.github/workflows`).

## Lizenz

Das Projekt basiert auf der Vorlage des PE2-Kurses. Es gilt die mitgelieferte
[MIT-Lizenz](LICENSE), © 2021 Justus Bogner (aus der Kursvorlage übernommen).
