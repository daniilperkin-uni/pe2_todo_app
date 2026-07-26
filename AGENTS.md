# AGENTS.md — pe2_todo_app

Fullstack Todo application: Spring Boot 3.2.5 REST API + Vue 3/TypeScript frontend, Docker Compose.

## Build & Run

### Docker (recommended)
```bash
docker compose up --build
```
Starts backend (API), frontend (nginx), and MariaDB.

### Backend dev
```bash
cd api
./mvnw spring-boot:run
```
Requires MariaDB running on localhost:3306 or use Docker: `docker compose up database`

### Frontend dev
```bash
cd frontend
npm install
npm run dev
```
Vite dev server runs on http://localhost:5173.

### Tests
```bash
# Backend
cd api && ./mvnw clean test

# Frontend
cd frontend && npm run test
```

## Architecture

### Backend (Spring Boot 3.2.5, Java 21)
- **Layering:** `Controller → Service → Repository`
- `TodoController` / `AssigneeController` — thin HTTP wiring, DTO conversion only
- `TodoService` / `AssigneeService` — `@Service`, `@Transactional`, all business logic
- `TodoRepository` / `AssigneeRepository` — Spring Data JPA
- `GlobalExceptionHandler` — `@RestControllerAdvice`, RFC 7807 ProblemDetail
- `TodoClassifier` — JPMML-based ML priority classifier
- DTOs: `TodoDTO`, `TodoCreateUpdateDTO`, `AssigneeDTO`, `AssigneeCreateUpdateDTO` (Bean Validation)

### Frontend (Vue 3.5, TypeScript 5.9, Vite 7)
- `views/` — page-level components (TodosView, CreateUpdateTodoView, AssigneesView)
- `components/` — reusable components (TodoList, TodoItem, TodoForm, AssigneeList)
- `services/apiService.ts` — centralized API client
- `types/` — TypeScript interfaces mirroring backend DTOs
- `router/` — Vue Router configuration

### Database
- MariaDB via Docker Compose
- `spring.jpa.hibernate.ddl-auto=update` (dev), `validate` (prod)
- Credentials via environment variables (see `.env.example`)

## Rules

1. **No business logic in controllers.** Extract into `@Service` classes.
2. **Always use constructor injection.** No `@Autowired` field injection.
3. **Every multi-aggregate mutation must be `@Transactional`.**
4. **Never hardcode DB credentials.** Use environment variables + `.env.example`.
5. **CORS must use explicit origins**, not `allowedOriginPatterns("*")`.
6. **Frontend: never use `v-html`.** Always use `{{ }}` interpolation.
7. **Due-date validation applies to both create AND update.**
8. **Every public method has Javadoc** (backend) or JSDoc (frontend).

## Known Issues Fixed
- `Assignee.java` had dead `Logger` field (never initialized — NPE risk). Removed.
- `ApiVersion1.java` meta-annotation was unused. Removed.
- `ddl-auto=create-drop` destroyed DB on shutdown. Changed to `update`.
- `deleteAssignee` was not `@Transactional`. Fixed.
- CORS was `allowedOriginPatterns("*")`. Restricted to explicit origins.
- `@ManyToMany(fetch = EAGER)` caused N+1. Changed to `LAZY`.

## Environment Variables
See `.env.example` for all configurable variables.
