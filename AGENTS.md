# AGENTS.md — pe2_todo_app

Fullstack Todo application: Spring Boot 4.1.1 REST API + Vue 3/TypeScript frontend, Docker Compose.

## Build & Run

### Docker (recommended)

```bash
cp .env.example .env    # once: DB_USERNAME and DB_PASSWORD are required
docker compose up --build
```

Starts backend (API), frontend (nginx), and MariaDB.
Without `.env` the compose run aborts with an explicit message, because there are no hardcoded credentials any more.

### Backend dev

```bash
cd api
export DB_USERNAME=<user> DB_PASSWORD=<password>   # required, see .env.example
./mvnw spring-boot:run
```

Requires MariaDB running on localhost:3306 or use Docker: `docker compose up database`

### Frontend dev

```bash
cd frontend
npm install
npm run dev
```

Vite dev server runs on <http://localhost:5173>.

### Tests

```bash
# Backend
cd api && ./mvnw clean test   # ./mvnw verify adds Checkstyle + JaCoCo >= 80 % (as in CI)

# Frontend
cd frontend && npm run test          # Vitest
cd frontend && npm run type-check    # vue-tsc
cd frontend && npm run lint:ci       # ESLint, exactly the CI gate
cd frontend && npm run build         # type-check + vite build
```

## Architecture

### Backend (Spring Boot 4.1.1, Java 21)

- **Layering:** `Controller → Service → Repository`
- `TodoController` / `AssigneeController` — thin HTTP wiring, DTO conversion only
- `TodoService` / `AssigneeService` — `@Service`, `@Transactional`, all business logic
- `TodoRepository` / `AssigneeRepository` — Spring Data JPA
- `GlobalExceptionHandler` — `@RestControllerAdvice`, RFC 7807 ProblemDetail
- `TodoClassifier` — JPMML-based ML priority classifier
- DTOs: `TodoDTO`, `TodoCreateUpdateDTO`, `AssigneeDTO`, `AssigneeCreateUpdateDTO`, `TodoStatsDTO`, `PriorityCorrection*DTO` (Bean Validation)
- Also: `CsvDownloadController`, `PriorityCorrectionController`

### Frontend (Vue 3.5, TypeScript 5.9, Vite 7)

- `views/` — page-level components (TodosView, CreateUpdateTodoView, BoardView, TodoStatsView, AssigneesView, AssigneeDetailsView, CreateAssigneeView, NotFoundView)
- `components/` — reusable components (TodoList, TodoItem, TodoForm, AssigneeList, AssigneeForm)
- `services/apiService.ts` — centralized API client (throws on failed requests; no offline fallback)
- `assets/base.css` — design tokens for every colour (including the chart palette) and the `prefers-reduced-motion` fallback
- `types/` — TypeScript interfaces mirroring backend DTOs
- `router/` — Vue Router configuration (`/` redirects to `/todos`, unknown paths render NotFoundView)
- Component tests live next to the component (`*.test.ts`, Vitest + jsdom)

### Database

- MariaDB via Docker Compose
- `spring.jpa.hibernate.ddl-auto=update` (application.properties and Docker Compose; no separate prod profile)
- Credentials via environment variables (see `.env.example`); `DB_USERNAME` and `DB_PASSWORD` have no defaults, so a missing value fails at startup instead of falling back to `root/root`

## Rules

1. **No business logic in controllers.** Extract into `@Service` classes.
2. **Always use constructor injection.** No `@Autowired` field injection.
3. **Every multi-aggregate mutation must be `@Transactional`.**
4. **Never hardcode DB credentials.** Use environment variables + `.env.example`.
5. **CORS must use explicit origins**, not `allowedOriginPatterns("*")`.
6. **Frontend: never use `v-html`.** Always use `{{ }}` interpolation.
7. **Due-date validation:** a new due date must be in the future on create AND update; an unchanged (already past) due date is accepted on update so overdue todos stay finishable.
8. **Every public method has Javadoc** (backend) or JSDoc (frontend).

## Known Issues Fixed

- `Assignee.java` had dead `Logger` field (never initialized — NPE risk). Removed.
- `ApiVersion1.java` meta-annotation was unused. Removed.
- `ddl-auto=create-drop` destroyed DB on shutdown. Changed to `update`.
- `deleteAssignee` was not `@Transactional`. Fixed.
- CORS was `allowedOriginPatterns("*")`. Restricted to explicit origins.
- `@ManyToMany(fetch = EAGER)` caused N+1. Changed to `LAZY`.
- `updateTodo` read the finished state after `convertToEntity` had already mutated the same instance, so finishing a recurring todo through PUT spawned no successor. The state is now captured before the conversion.
- `validateDueDate` rejected the unchanged due date on update, which made every overdue todo impossible to complete or edit. The rule now only applies when the due date actually changes.
- `frontend/src/config.ts` was dead config; the Dockerfile/compose build arg `VITE_API_BASE_URL` was never read. Both removed.

## Environment Variables

See `.env.example` for all configurable variables.
