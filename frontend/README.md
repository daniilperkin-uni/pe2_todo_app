# Frontend

Dies ist ein Beispielprojekt für ein Single-Page-Application-Frontend mit Vue.js v3. Die Benutzeroberfläche ist während der Entwicklung unter <http://localhost:5173> erreichbar und normalerweise unter `http://localhost`, wenn sie über Docker Compose ausgeführt wird.

Siehe [HELP.md](HELP.md) für weitere Informationen.

## Übersicht

Das Frontend ist eine moderne Vue.js-Anwendung im Verzeichnis `frontend`. Es ist mit einer komponentenbasierte Architektur aufgebaut, die zwischen Views auf Seitenebene und wiederverwendbaren Komponenten unterscheidet. Es verwendet `vue-router` für die clientseitige Navigation (Hash-basiert) und ist in TypeScript geschrieben, um eine bessere Codequalität zu gewährleisten.

## Verwendete Technologien

*   **Vue.js 3:** Das Kern-Framework zum Erstellen der Benutzeroberfläche.
*   **Vite:** Ein schnelles Build-Tool für eine zügige Entwicklung.
*   **TypeScript:** Für statische Typisierung.
*   **Vue Router:** Für clientseitiges Routing und Navigation.
*   **ESLint und Prettier:** Für Code-Linting und Formatierung.
*   **AgnosticUI:** Eine Komponentenbibliothek für ein konsistentes UI-Styling.

## Projektstruktur

*   **Quellcode:** `frontend/src`
*   **Abhängigkeiten:** `frontend/package.json`
*   **Build-Konfiguration:** `frontend/vite.config.ts`
*   **API-Kommunikation:** Zentral verwaltet in `frontend/src/services/apiService.ts`, nutzt die `fetch`-API des Browsers und enthält einen einfachen Offline-Fallback.
*   **Styling:** Nutzt `agnostic-vue` und Scoped CSS.

## Starten (Entwicklung)

Um den Frontend-Entwicklungsserver separat auszuführen, folgen Sie diesen Schritten:

1.  **Navigieren Sie in das `frontend`-Verzeichnis:**
    ```bash
    cd frontend
    ```
2.  **Abhängigkeiten installieren:**
    ```bash
    npm install
    ```
3.  **Den Entwicklungsserver starten:**
    ```bash
    npm run dev
    ```
    Der Frontend-Entwicklungsserver startet normalerweise unter `http://localhost:5173`. API-Anfragen werden automatisch an das Backend (erwartet unter `http://localhost:8080`) weitergeleitet, wie in `vite.config.ts` konfiguriert.

## Bauen für Produktion

So erstellen Sie die produktionsbereiten statischen Assets:

1.  **Navigieren Sie in das `frontend`-Verzeichnis:**
    ```bash
    cd frontend
    ```
2.  **Produktions-Assets bauen:**
    ```bash
    npm run build
    ```
    Dieser Befehl generiert die statischen Assets im Verzeichnis `frontend/dist/`. Diese Assets werden dann von einem Nginx-Webserver in der dockerisierten Produktionsumgebung bereitgestellt.

## Code-Qualität

*   **Linting:** `npm run lint`, um nach ESLint-Problemen zu suchen.
*   **Formatierung:** `npm run format`, um den Code automatisch mit Prettier zu formatieren.

## Docker-Integration

Das Frontend verwendet ein mehrstufiges `Dockerfile` (`frontend/Dockerfile`). Es baut die Vue.js-Anwendung mit `vite` und stellt dann die statischen Assets über einen Nginx-Webserver bereit. Die Datei `frontend/nginx.conf` konfiguriert Nginx so, dass die Vue-App bereitgestellt wird und API-Anfragen von `/api` an den Backend-Dienst weitergeleitet werden.



## UI/UX Änderungen und Lokalisierung

*   **Suche und Filter:** Suche nach Titel und Sortierung nach verschiedenen Attributen (Titel, Priorität, Fälligkeitsdatum) in der ToDo-Übersicht implementiert.
*   **Aufgabentrennung:** Offene und erledigte Aufgaben werden jetzt in separaten Abschnitten angezeigt, um eine bessere Übersicht zu ermöglichen.
*   **Gemischtsprachige UI:** Die Benutzeroberfläche verwendet Deutsch für allgemeine UI-Elements (Labels, Nachrichten), während Kernentitätsnamen wie "Assignees" und "Todos" zur Konsistenz auf Englisch bleiben.
