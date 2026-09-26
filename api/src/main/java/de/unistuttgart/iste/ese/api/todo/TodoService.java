package de.unistuttgart.iste.ese.api.todo;

import de.unistuttgart.iste.ese.api.assignee.Assignee;
import de.unistuttgart.iste.ese.api.assignee.AssigneeRepository;
import de.unistuttgart.iste.ese.api.assignee.dto.AssigneeDTO;
import de.unistuttgart.iste.ese.api.todo.dto.TodoCreateUpdateDTO;
import de.unistuttgart.iste.ese.api.todo.dto.TodoDTO;
import de.unistuttgart.iste.ese.api.todo.dto.TodoStatsDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private final PriorityCorrectionRepository priorityCorrectionRepository;

    /**
     * Constructor for dependency injection.
     *
     * @param todoRepository     repository for todo persistence
     * @param assigneeRepository repository for assignee persistence
     * @param todoClassifier     ML-based title classifier used to derive the category
     * @param priorityCorrectionRepository repository for classifier feedback rows
     */
    public TodoService(TodoRepository todoRepository, AssigneeRepository assigneeRepository,
                       TodoClassifier todoClassifier, PriorityCorrectionRepository priorityCorrectionRepository) {
        this.todoRepository = todoRepository;
        this.assigneeRepository = assigneeRepository;
        this.todoClassifier = todoClassifier;
        this.priorityCorrectionRepository = priorityCorrectionRepository;
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
        dto.setStatus(todo.getStatus().name());
        dto.setCreatedDate(todo.getCreatedDate());
        dto.setDueDate(todo.getDueDate());
        dto.setFinishedDate(todo.getFinishedDate());
        dto.setCategory(todo.getCategory());
        dto.setRecurrenceRule(todo.getRecurrenceRule().name());
        dto.setNextOccurrenceDate(todo.getNextOccurrenceDate());

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

        if (dto.getPriority() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Priority must not be null. Must be LOW, MEDIUM, or HIGH.");
        }
        try {
            todo.setPriority(Priority.valueOf(dto.getPriority().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid priority value. Must be LOW, MEDIUM, or HIGH.");
        }

        // Parse the workflow status from the DTO (additive: null defaults to OPEN)
        if (dto.getStatus() != null) {
            try {
                TodoStatus status = TodoStatus.valueOf(dto.getStatus().toUpperCase());
                todo.setStatus(status);
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid status value. Must be OPEN, IN_PROGRESS, or DONE.");
            }
        }

        // Parse the recurrence rule from the DTO (additive: null defaults to NONE)
        if (dto.getRecurrenceRule() != null) {
            try {
                todo.setRecurrenceRule(RecurrenceRule.valueOf(dto.getRecurrenceRule().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid recurrence rule. Must be NONE, WEEKLY, or MONTHLY.");
            }
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
     * <p>Applies the due-date validation of {@link #createTodo} whenever the
     * due date differs from the stored one; an unchanged (possibly past) due
     * date is accepted so overdue todos remain editable and finishable.
     * Finishing a recurring todo spawns its next occurrence.
     *
     * @param id      the identifier of the todo to update
     * @param todoDTO the new data
     * @return the updated todo as DTO
     * @throws ResponseStatusException with status {@code 404} when the todo does not exist,
     *     or {@code 400} for validation failures
     */
    public TodoDTO updateTodo(Long id, TodoCreateUpdateDTO todoDTO) {
        Todo existingTodo = todoRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Todo not found with id " + id));
        // The future-due-date rule only applies to a due date that is actually
        // being changed. An overdue todo keeps its (past) due date when it is
        // ticked finished from the list, so validating the unchanged value
        // would make overdue todos impossible to complete or edit.
        if (!Objects.equals(todoDTO.getDueDate(), existingTodo.getDueDate())) {
            validateDueDate(todoDTO);
        }
        // convertToEntity mutates the passed instance in place, therefore the
        // previous finished state must be captured before the conversion -
        // otherwise the just-finished transition below can never be detected.
        boolean wasFinished = existingTodo.isFinished();
        Todo updatedTodo = convertToEntity(todoDTO, existingTodo);
        boolean justFinished = !wasFinished && updatedTodo.isFinished();
        Todo savedTodo = todoRepository.save(updatedTodo);
        if (justFinished) {
            spawnNextRecurrence(savedTodo);
        }
        return convertToDTO(savedTodo);
    }

    /**
     * Transitions a todo to a new workflow status.
     *
     * <p>Valid Kanban workflow transitions are defined by the order in
     * {@link TodoStatus}: OPEN → IN_PROGRESS → DONE. A todo may move to any
     * earlier state (re-opening). Moving to DONE sets the finished flag and
     * finishedDate; moving away from DONE clears them. Moving to OPEN or
     * IN_PROGRESS clears them.
     *
     * @param id     the identifier of the todo to update
     * @param status the new workflow status
     * @return the updated todo as DTO
     * @throws ResponseStatusException with status {@code 404} when the todo does not exist,
     *     or {@code 400} when the status string is not a valid TodoStatus value
     */
    public TodoDTO transitionTodoStatus(Long id, String status) {
        TodoStatus newStatus;
        try {
            newStatus = TodoStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Invalid status value. Must be OPEN, IN_PROGRESS, or DONE.");
        }

        Todo todo = todoRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Todo not found with id " + id));

        // Synchronize the finished flag with the DONE status
        if (newStatus == TodoStatus.DONE) {
            todo.setFinished(true);
            if (todo.getFinishedDate() == null) {
                todo.setFinishedDate(LocalDate.now());
            }
        } else {
            todo.setFinished(false);
            todo.setFinishedDate(null);
        }

        todo.setStatus(newStatus);
        Todo savedTodo = todoRepository.save(todo);
        // Finishing via the kanban board also spawns the next recurrence.
        if (newStatus == TodoStatus.DONE) {
            spawnNextRecurrence(savedTodo);
        }
        return convertToDTO(savedTodo);
    }

    /**
     * Returns todos grouped and counted by status for the kanban board.
     *
     * @return a map from status name to the list of todo DTOs in that column
     */
    @Transactional(readOnly = true)
    public java.util.Map<TodoStatus, List<TodoDTO>> getTodosByStatus() {
        java.util.Map<TodoStatus, List<TodoDTO>> result = new java.util.EnumMap<>(TodoStatus.class);
        for (TodoStatus s : TodoStatus.values()) {
            result.put(s, List.of());
        }
        java.util.Map<TodoStatus, List<TodoDTO>> grouped = todoRepository.findAll().stream()
            .collect(Collectors.groupingBy(
                Todo::getStatus,
                java.util.stream.Collectors.mapping(this::convertToDTO, Collectors.toList())
            ));
        result.putAll(grouped);
        return result;
    }

    /**
     * Aggregates statistics over all todos: completion rate, average
     * created-to-finished duration, and per-priority, per-category and
     * per-assignee counts.
     *
     * <p>All aggregation happens in Java over one repository read - no
     * database-side grouping - which keeps the logic unit-testable without
     * a database.
     *
     * @return the aggregated statistics; never {@code null}
     */
    @Transactional(readOnly = true)
    public TodoStatsDTO getTodoStats() {
        List<Todo> allTodos = todoRepository.findAll();
        TodoStatsDTO stats = new TodoStatsDTO();
        stats.setTotalTodos(allTodos.size());

        List<Todo> finished = allTodos.stream()
            .filter(Todo::isFinished)
            .toList();
        stats.setFinishedTodos(finished.size());

        // Average duration from creation to completion, in whole days.
        java.util.OptionalDouble averageDays = finished.stream()
            .filter(todo -> todo.getFinishedDate() != null && todo.getCreatedDate() != null)
            .mapToLong(todo -> java.time.temporal.ChronoUnit.DAYS.between(
                todo.getCreatedDate(), todo.getFinishedDate()))
            .average();
        stats.setAverageDaysToFinish(averageDays.isPresent() ? averageDays.getAsDouble() : null);

        stats.setTodosPerPriority(allTodos.stream()
            .collect(Collectors.groupingBy(
                todo -> todo.getPriority().name(),
                java.util.stream.Collectors.counting())));

        stats.setTodosPerCategory(allTodos.stream()
            .collect(Collectors.groupingBy(
                todo -> todo.getCategory() != null ? todo.getCategory() : "unclassified",
                java.util.stream.Collectors.counting())));

        // Count each assignee across all todos, then map to display rows.
        java.util.Map<Assignee, Long> assigneeCounts = allTodos.stream()
            .flatMap(todo -> todo.getAssigneeList().stream())
            .collect(Collectors.groupingBy(assignee -> assignee, Collectors.counting()));

        stats.setTodosPerAssignee(assigneeCounts.entrySet().stream()
            .map(entry -> new TodoStatsDTO.AssigneeCount(
                entry.getKey().getId(),
                entry.getKey().getPrename(),
                entry.getKey().getName(),
                entry.getValue()))
            .sorted(java.util.Comparator.comparingLong(TodoStatsDTO.AssigneeCount::getCount).reversed())
            .collect(Collectors.toList()));

        return stats;
    }

    /**
     * Persists a priority correction recorded through classifier feedback.
     *
     * <p>Both priorities are validated against {@link Priority}; a correction
     * naming an unknown value is rejected with {@code 400}.
     *
     * @param correctionDTO the correction data
     * @return the persisted {@link PriorityCorrection}
     * @throws ResponseStatusException with status {@code 400} when either
     *     priority is not one of LOW, MEDIUM, HIGH
     */
    public PriorityCorrection recordPriorityCorrection(de.unistuttgart.iste.ese.api.todo.dto.PriorityCorrectionCreateDTO correctionDTO) {
        String predicted = validatePriorityName(correctionDTO.getPredictedPriority(), "predicted");
        String corrected = validatePriorityName(correctionDTO.getCorrectedPriority(), "corrected");

        PriorityCorrection correction = new PriorityCorrection();
        correction.setTodoTitle(correctionDTO.getTodoTitle());
        correction.setPredictedPriority(predicted);
        correction.setCorrectedPriority(corrected);
        correction.setCategory(correctionDTO.getCategory());
        return priorityCorrectionRepository.save(correction);
    }

    /**
     * Validates that the given name is a known {@link Priority}.
     *
     * @param name  the raw priority string from the request
     * @param which label used in the error message ("predicted" or "corrected")
     * @return the normalized (upper-case) priority name
     * @throws ResponseStatusException with status {@code 400} when unknown
     */
    private String validatePriorityName(String name, String which) {
        try {
            return Priority.valueOf(name.toUpperCase()).name();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Invalid " + which + " priority. Must be LOW, MEDIUM, or HIGH.");
        }
    }

    /**
     * Aggregates the recorded corrections: total count, counts per
     * predicted-to-corrected transition and per corrected target priority.
     *
     * @return the aggregated statistics; never {@code null}
     */
    @Transactional(readOnly = true)
    public PriorityCorrectionStatsDTO getPriorityCorrectionStats() {
        List<PriorityCorrection> all = priorityCorrectionRepository.findAll();
        PriorityCorrectionStatsDTO stats = new PriorityCorrectionStatsDTO();
        stats.setTotalCorrections(all.size());

        Map<String, Long> transitions = new java.util.TreeMap<>();
        Map<String, Long> perPriority = new java.util.TreeMap<>();
        for (PriorityCorrection c : all) {
            transitions.merge(c.getPredictedPriority() + "->" + c.getCorrectedPriority(), 1L, Long::sum);
            perPriority.merge(c.getCorrectedPriority(), 1L, Long::sum);
        }
        stats.setTransitions(transitions);
        stats.setCorrectionsPerPriority(perPriority);
        return stats;
    }

    /**
     * Computes the due date of the next occurrence after the given base date.
     *
     * <p>WEEKLY adds seven days. MONTHLY adds one month with month-end
     * rollover: Jan 31 -> Feb 28 (or 29), because {@link LocalDate#plusMonths}
     * clamps to the last valid day of the target month.
     *
     * @param rule  the recurrence cadence; must not be {@code null}
     * @param after the date the next occurrence is computed from
     * @return the next occurrence date, or {@code null} when the rule is NONE
     */
    static LocalDate computeNextOccurrence(RecurrenceRule rule, LocalDate after) {
        return switch (rule) {
            case WEEKLY -> after.plusWeeks(1);
            case MONTHLY -> after.plusMonths(1);
            case NONE -> null;
        };
    }

    /**
     * Spawns the next instance when a recurring todo is finished.
     *
     * <p>The finished todo keeps its finished state; a fresh copy with the
     * same title, description, priority, assignees, category and recurrence
     * rule is created with dueDate = nextOccurrenceDate and finished = false.
     * Non-recurring todos produce no successor.
     *
     * @param finishedTodo the todo that was just marked finished; must not be {@code null}
     */
    private void spawnNextRecurrence(Todo finishedTodo) {
        if (finishedTodo.getRecurrenceRule() == null || finishedTodo.getRecurrenceRule() == RecurrenceRule.NONE) {
            return;
        }
        LocalDate baseDate = finishedTodo.getNextOccurrenceDate() != null
            ? finishedTodo.getNextOccurrenceDate()
            : finishedTodo.getDueDate();
        if (baseDate == null) {
            // Nothing to anchor the next occurrence to; skip silently rather
            // than fail the finishing operation.
            return;
        }

        LocalDate nextDue = computeNextOccurrence(finishedTodo.getRecurrenceRule(), baseDate);
        Todo next = new Todo();
        next.setTitle(finishedTodo.getTitle());
        next.setDescription(finishedTodo.getDescription());
        next.setPriority(finishedTodo.getPriority());
        next.setRecurrenceRule(finishedTodo.getRecurrenceRule());
        next.setNextOccurrenceDate(nextDue);
        next.setDueDate(nextDue);
        next.setAssigneeList(finishedTodo.getAssigneeList());
        next.setCategory(finishedTodo.getCategory());
        next.setStatus(TodoStatus.OPEN);
        todoRepository.save(next);
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
