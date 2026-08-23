package de.unistuttgart.iste.ese.api.todo;

import de.unistuttgart.iste.ese.api.todo.dto.TodoCreateUpdateDTO;
import de.unistuttgart.iste.ese.api.todo.dto.TodoDTO;
import de.unistuttgart.iste.ese.api.todo.dto.TodoStatsDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for the {@link Todo} resource.
 *
 * <p>This controller is intentionally thin: it only performs HTTP wiring and
 * delegates all business logic to {@link TodoService}.
 */
@RestController
@RequestMapping("/api/v1/todos")
public class TodoController {

    private final TodoService todoService;

    /**
     * Constructor for dependency injection.
     *
     * @param todoService the service handling todo business logic
     */
    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    /**
     * Retrieves all todos.
     *
     * @return a list of all todos as DTOs
     */
    @GetMapping
    public List<TodoDTO> getAllTodos() {
        return todoService.getAllTodos();
    }

    /**
     * Retrieves a single todo by its identifier.
     *
     * @param id the todo identifier
     * @return the matching todo as DTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<TodoDTO> getTodoById(@PathVariable Long id) {
        return ResponseEntity.ok(todoService.getTodoById(id));
    }

    /**
     * Creates a new todo.
     *
     * @param todoDTO the validated data for the new todo
     * @return the created todo as DTO with HTTP status {@code 201 Created}
     */
    @PostMapping
    public ResponseEntity<TodoDTO> createTodo(@Valid @RequestBody TodoCreateUpdateDTO todoDTO) {
        return new ResponseEntity<>(todoService.createTodo(todoDTO), HttpStatus.CREATED);
    }

    /**
     * Updates an existing todo.
     *
     * @param id      the identifier of the todo to update
     * @param todoDTO the validated new data
     * @return the updated todo as DTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<TodoDTO> updateTodo(@PathVariable Long id, @Valid @RequestBody TodoCreateUpdateDTO todoDTO) {
        return ResponseEntity.ok(todoService.updateTodo(id, todoDTO));
    }

    /**
     * Retrieves aggregated statistics over all todos.
     *
     * @return completion rate, average days to finish, and per-priority,
     *     per-category and per-assignee counts
     */
    @GetMapping("/stats")
    public TodoStatsDTO getTodoStats() {
        return todoService.getTodoStats();
    }

    /**
     * Deletes a todo by its identifier.
     *
     * @param id the identifier of the todo to delete
     * @return an empty response with HTTP status {@code 200 OK}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodo(@PathVariable Long id) {
        todoService.deleteTodo(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Transitions a todo to a new Kanban workflow status.
     *
     * <p>The body contains the target {@code status} (OPEN, IN_PROGRESS or DONE).
     * When the new status is DONE the {@code finished} flag is set and
     * {@code finishedDate} populated; transitioning away from DONE clears them.
     *
     * @param id     the identifier of the todo to transition
     * @param status the new workflow status ({@code TodoStatus})
     * @return the updated todo as DTO
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<TodoDTO> transitionStatus(@PathVariable Long id, @RequestBody TodoStatus status) {
        return ResponseEntity.ok(todoService.transitionTodoStatus(id, status.name()));
    }
}
