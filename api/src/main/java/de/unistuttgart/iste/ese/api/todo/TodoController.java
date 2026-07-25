package de.unistuttgart.iste.ese.api.todo;

import de.unistuttgart.iste.ese.api.assignee.Assignee;
import de.unistuttgart.iste.ese.api.assignee.AssigneeRepository;
import de.unistuttgart.iste.ese.api.assignee.dto.AssigneeDTO;
import de.unistuttgart.iste.ese.api.todo.dto.TodoCreateUpdateDTO;
import de.unistuttgart.iste.ese.api.todo.dto.TodoDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

// REST-Controller für die Verwaltung von Todos
@RestController
@RequestMapping("/api/v1/todos")
public class TodoController {

    private final TodoRepository todoRepository;
    private final AssigneeRepository assigneeRepository;
    private final TodoClassifier todoClassifier;

    /**
     * Konstruktor für Dependency Injection
     */
    public TodoController(TodoRepository todoRepository, AssigneeRepository assigneeRepository, TodoClassifier todoClassifier) {
        this.todoRepository = todoRepository;
        this.assigneeRepository = assigneeRepository;
        this.todoClassifier = todoClassifier;
    }

    /**
     * Konvertiert ein Todo-Entity in ein Todo-DTO
     */
    private TodoDTO convertToDTO(Todo todo) {
        TodoDTO dto = new TodoDTO();
        dto.setId(todo.getId());
        dto.setTitle(todo.getTitle());
        dto.setDescription(todo.getDescription());
        dto.setFinished(todo.isFinished());
        dto.setPriority(todo.getPriority().name());
        dto.setCreatedDate(todo.getCreatedDate());
        dto.setDueDate(todo.getDueDate());
        dto.setFinishedDate(todo.getFinishedDate());
        dto.setCategory(todo.getCategory());

        List<AssigneeDTO> assigneeDTOs = todo.getAssigneeList().stream()
            .map(assignee -> {
                AssigneeDTO aDto = new AssigneeDTO();
                aDto.setId(assignee.getId());
                aDto.setPrename(assignee.getPrename());
                aDto.setName(assignee.getName());
                aDto.setEmail(assignee.getEmail());
                return aDto;
            }).collect(Collectors.toList());
        dto.setAssigneeList(assigneeDTOs);
        return dto;
    }

    /**
     * Konvertiert ein DTO in ein Todo-Entity
     */
    private Todo convertToEntity(TodoCreateUpdateDTO dto, Todo existingTodo) {
        Todo todo = (existingTodo != null) ? existingTodo : new Todo();
        todo.setTitle(dto.getTitle());
        todo.setDescription(dto.getDescription());
        todo.setDueDate(dto.getDueDate());

        try {
            todo.setPriority(Priority.valueOf(dto.getPriority().toUpperCase()));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid priority value. Must be LOW, MEDIUM, or HIGH.");
        }

        // Behandele die finished-Logik für PUT
        if (existingTodo != null) {
            boolean wasFinished = existingTodo.isFinished();
            boolean isNowFinished = dto.isFinished();
            if (!wasFinished && isNowFinished) {
                todo.setFinished(true);
                todo.setFinishedDate(LocalDate.now());
            } else if (wasFinished && !isNowFinished) {
                // Optional: Erlaube das Zurücksetzen des Status
                todo.setFinished(false);
                todo.setFinishedDate(null);
            } else {
                todo.setFinished(isNowFinished);
            }
        } else {
            // Respect finished state on creation
            if (dto.isFinished()) {
                todo.setFinished(true);
                todo.setFinishedDate(LocalDate.now());
            } else {
                todo.setFinished(false);
            }
        }

        // Setze die Assignees
        Set<Assignee> assignees = new HashSet<>();
        if (dto.getAssigneeIdList() != null) {
            // Prüfe auf Eindeutigkeit der IDs
            if (dto.getAssigneeIdList().size() != new HashSet<>(dto.getAssigneeIdList()).size()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignee IDs must be unique.");
            }
            for (Long assigneeId : dto.getAssigneeIdList()) {
                Assignee assignee = assigneeRepository.findById(assigneeId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignee not found with id " + assigneeId));
                assignees.add(assignee);
            }
        }
        todo.setAssigneeList(assignees);
        todo.setCategory(todoClassifier.classify(todo.getTitle())); // Set category based on title
        return todo;
    }


    /**
     * Ruft alle Todos ab
     */
    @GetMapping
    public List<TodoDTO> getAllTodos() {
        return todoRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Ruft ein Todo anhand seiner ID ab
     */
    @GetMapping("/{id}")
    public ResponseEntity<TodoDTO> getTodoById(@PathVariable Long id) {
        Optional<Todo> todo = todoRepository.findById(id);
        if (todo.isPresent()) {
            return ResponseEntity.ok(convertToDTO(todo.get()));
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Todo not found with id " + id);
        }
    }

    /**
     * Erstellt ein neues Todo
     */
    @PostMapping
    public ResponseEntity<TodoDTO> createTodo(@Valid @RequestBody TodoCreateUpdateDTO todoDTO) {
        if (todoDTO.getDueDate() != null && !todoDTO.getDueDate().isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Due date must be in the future");
        }
        Todo newTodo = convertToEntity(todoDTO, null); // null, da es ein neues Todo ist
        Todo savedTodo = todoRepository.save(newTodo);
        return new ResponseEntity<>(convertToDTO(savedTodo), HttpStatus.CREATED);
    }

    /**
     * Aktualisiert ein vorhandenes Todo
     */
    @PutMapping("/{id}")
    public ResponseEntity<TodoDTO> updateTodo(@PathVariable Long id, @Valid @RequestBody TodoCreateUpdateDTO todoDTO) {
        return todoRepository.findById(id)
            .map(existingTodo -> {
                Todo updatedTodo = convertToEntity(todoDTO, existingTodo);
                Todo savedTodo = todoRepository.save(updatedTodo);
                return ResponseEntity.ok(convertToDTO(savedTodo));
            })
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Todo not found with id " + id));
    }

    /**
     * Löscht ein Todo anhand seiner ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodo(@PathVariable Long id) {
        return todoRepository.findById(id)
            .map(todo -> {
                todoRepository.delete(todo);
                return ResponseEntity.ok().<Void>build();
            })
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Todo not found with id " + id));
    }
}
