package de.unistuttgart.iste.ese.api.todo;

import de.unistuttgart.iste.ese.api.assignee.Assignee;
import de.unistuttgart.iste.ese.api.assignee.AssigneeRepository;
import de.unistuttgart.iste.ese.api.assignee.dto.AssigneeDTO;
import de.unistuttgart.iste.ese.api.todo.dto.TodoCreateUpdateDTO;
import de.unistuttgart.iste.ese.api.todo.dto.TodoDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service layer encapsulating all business logic for {@link Todo} entities.
 *
 * <p>Handles priority parsing, finished-state transitions, due-date validation,
 * duplicate-assignee detection, assignee resolution, ML category classification
 * and DTO/entity mapping. All public methods execute within a transaction.
 */
@Service
@Transactional
public class TodoService {

    private final TodoRepository todoRepository;
    private final AssigneeRepository assigneeRepository;
    private final TodoClassifier todoClassifier;

    /**
     * Constructor for dependency injection.
     *
     * @param todoRepository     repository for todo persistence
     * @param assigneeRepository repository for assignee persistence
     * @param todoClassifier     ML-based title classifier used to derive the category
     */
    public TodoService(TodoRepository todoRepository, AssigneeRepository assigneeRepository, TodoClassifier todoClassifier) {
        this.todoRepository = todoRepository;
        this.assigneeRepository = assigneeRepository;
        this.todoClassifier = todoClassifier;
    }

    /**
     * Converts a {@link Todo} entity into its DTO representation, including the
     * assigned assignees.
     *
     * @param todo the entity to convert; must not be {@code null}
     * @return the populated {@link TodoDTO}
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
     * Applies the DTO values onto the given entity (or a new entity when
     * {@code existingTodo} is {@code null}), resolving assignees, parsing the
     * priority, deriving the finished-state transition and classifying the
     * category.
     *
     * @param dto           the incoming data; must not be {@code null}
     * @param existingTodo  the entity to update, or {@code null} to create a new one
     * @return the prepared (unsaved) {@link Todo}
     * @throws ResponseStatusException when the priority is invalid, assignee IDs
     *     are duplicated or an assignee cannot be resolved
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

        // Handle finished-state transitions for updates (existing todo)
        if (existingTodo != null) {
            boolean wasFinished = existingTodo.isFinished();
            boolean isNowFinished = dto.isFinished();
            if (!wasFinished && isNowFinished) {
                todo.setFinished(true);
                todo.setFinishedDate(LocalDate.now());
            } else if (wasFinished && !isNowFinished) {
                // Allow resetting the finished state
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

        // Resolve and assign assignees
        Set<Assignee> assignees = new HashSet<>();
        if (dto.getAssigneeIdList() != null) {
            // Reject duplicate assignee IDs
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
        todo.setCategory(todoClassifier.classify(todo.getTitle()));
        return todo;
    }

    /**
     * Validates that the due date, when present, is strictly in the future.
     *
     * @param todoDTO the DTO to validate
     * @throws ResponseStatusException with status {@code 400} when the due date is not in the future
     */
    private void validateDueDate(TodoCreateUpdateDTO todoDTO) {
        if (todoDTO.getDueDate() != null && !todoDTO.getDueDate().isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Due date must be in the future");
        }
    }

    /**
     * Retrieves all todos.
     *
     * @return a list of all todos as DTOs; never {@code null}
     */
    @Transactional(readOnly = true)
    public List<TodoDTO> getAllTodos() {
        return todoRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Retrieves a single todo by its identifier.
     *
     * @param id the todo identifier
     * @return the matching todo as DTO
     * @throws ResponseStatusException with status {@code 404} when no todo exists for the id
     */
    @Transactional(readOnly = true)
    public TodoDTO getTodoById(Long id) {
        return todoRepository.findById(id)
            .map(this::convertToDTO)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Todo not found with id " + id));
    }

    /**
     * Creates a new todo.
     *
     * <p>Validates that the due date (when provided) lies in the future.
     *
     * @param todoDTO the data for the new todo
     * @return the created todo as DTO
     * @throws ResponseStatusException with status {@code 400} when the due date is
     *     not in the future, the priority is invalid or assignees cannot be resolved
     */
    public TodoDTO createTodo(TodoCreateUpdateDTO todoDTO) {
        validateDueDate(todoDTO);
        Todo newTodo = convertToEntity(todoDTO, null);
        Todo savedTodo = todoRepository.save(newTodo);
        return convertToDTO(savedTodo);
    }

    /**
     * Updates an existing todo.
     *
     * <p>Applies the same due-date validation as {@link #createTodo}.
     *
     * @param id      the identifier of the todo to update
     * @param todoDTO the new data
     * @return the updated todo as DTO
     * @throws ResponseStatusException with status {@code 404} when the todo does not exist,
     *     or {@code 400} for validation failures
     */
    public TodoDTO updateTodo(Long id, TodoCreateUpdateDTO todoDTO) {
        validateDueDate(todoDTO);
        Todo existingTodo = todoRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Todo not found with id " + id));
        Todo updatedTodo = convertToEntity(todoDTO, existingTodo);
        Todo savedTodo = todoRepository.save(updatedTodo);
        return convertToDTO(savedTodo);
    }

    /**
     * Deletes a todo by its identifier.
     *
     * @param id the identifier of the todo to delete
     * @throws ResponseStatusException with status {@code 404} when the todo does not exist
     */
    public void deleteTodo(Long id) {
        Todo todo = todoRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Todo not found with id " + id));
        todoRepository.delete(todo);
    }
}
