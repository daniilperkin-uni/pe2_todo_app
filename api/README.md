# Spring Boot RESTful API

## How to Start

Um MariaDB zu starten: in CMD: `docker run -d --name pe2-mariadb -p 3306:3306 -e MARIADB_ROOT_PASSWORD=root -e MARIADB_DATABASE=pe2 mariadb:latest`

Um Anwendung zu starten: in CMD: `cd api && ./mvnw spring-boot:run`

Dafür müssen die Datenbank-Zugangsdaten als Umgebungsvariablen gesetzt sein
(`DB_USERNAME`, `DB_PASSWORD`, optional `DB_URL` - siehe `.env.example`); es gibt
keine hartcodierten Standardwerte mehr.

Um alle Prüfungen auszuführen (CI-Gate): in CMD: `./mvnw verify` (Tests, Checkstyle, JaCoCo-Coverage >= 80 %)

Um nur die Tests auszuführen: in CMD: `./mvnw test`

## Lokale Endpunkte

Nachdem start von Anwendung, ist die API unter `http://localhost:8080/api/v1/...` erreichbar.

Zum besseren Testen nutzen Sie Swagger UI: `http://localhost:8080/swagger-ui/index.html`
