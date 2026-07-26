package de.unistuttgart.iste.ese.api.assignee;

import de.unistuttgart.iste.ese.api.assignee.dto.AssigneeCreateUpdateDTO;
import de.unistuttgart.iste.ese.api.assignee.dto.AssigneeDTO;
import de.unistuttgart.iste.ese.api.todo.Todo;
import de.unistuttgart.iste.ese.api.todo.TodoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer encapsulating all business logic for {@link Assignee} entities.
 *
 * <p>Operations that touch multiple aggregates (e.g. deleting an assignee and
 * updating the associated todos) run within a single transaction so that the
 * data store can never be left in an inconsistent state.
 */
@Service
@Transactional
public class AssigneeService {

    private final AssigneeRepository assigneeRepository;
    private final TodoRepository todoRepository;

    /**
     * Constructor for dependency injection.
     *
     * @param assigneeRepository repository for assignee persistence
     * @param todoRepository     repository for todo persistence (used during cascading deletes)
     */
    public AssigneeService(AssigneeRepository assigneeRepository, TodoRepository todoRepository) {
        this.assigneeRepository = assigneeRepository;
        this.todoRepository = todoRepository;
    }

    /**
     * Converts an {@link Assignee} entity into its DTO representation.
     *
     * @param assignee the entity to convert; must not be {@code null}
     * @return the populated {@link AssigneeDTO}
     */
    private AssigneeDTO convertToDTO(Assignee assignee) {
        AssigneeDTO dto = new AssigneeDTO();
        dto.setId(assignee.getId());
        dto.setPrename(assignee.getPrename());
        dto.setName(assignee.getName());
        dto.setEmail(assignee.getEmail());
        return dto;
    }

    /**
     * Converts a create/update DTO into a new {@link Assignee} entity.
     *
     * @param dto the incoming data; must not be {@code null}
     * @return a new, unsaved entity populated with the DTO values
     */
    private Assignee convertToEntity(AssigneeCreateUpdateDTO dto) {
        Assignee assignee = new Assignee();
        assignee.setPrename(dto.getPrename());
        assignee.setName(dto.getName());
        assignee.setEmail(dto.getEmail());
        return assignee;
    }

    /**
     * Retrieves all assignees.
     *
     * @return a list of all assignees as DTOs; never {@code null}
     */
    @Transactional(readOnly = true)
    public List<AssigneeDTO> getAllAssignees() {
        return assigneeRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Retrieves a single assignee by its identifier.
     *
     * @param id the assignee identifier
     * @return the matching assignee as DTO
     * @throws ResponseStatusException with status {@code 404} when no assignee exists for the id
     */
    @Transactional(readOnly = true)
    public AssigneeDTO getAssigneeById(Long id) {
        return assigneeRepository.findById(id)
            .map(this::convertToDTO)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignee not found with id " + id));
    }

    /**
     * Creates a new assignee.
     *
     * @param assigneeDTO the data for the new assignee
     * @return the created assignee as DTO
     * @throws ResponseStatusException with status {@code 400} when the email is already in use
     */
    public AssigneeDTO createAssignee(AssigneeCreateUpdateDTO assigneeDTO) {
        if (assigneeRepository.existsByEmail(assigneeDTO.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }
        Assignee newAssignee = convertToEntity(assigneeDTO);
        Assignee savedAssignee = assigneeRepository.save(newAssignee);
        return convertToDTO(savedAssignee);
    }

    /**
     * Updates an existing assignee.
     *
     * @param id          the identifier of the assignee to update
     * @param assigneeDTO the new data
     * @return the updated assignee as DTO
     * @throws ResponseStatusException with status {@code 404} when the assignee does not exist,
     *     or {@code 400} when the new email is already used by another assignee
     */
    public AssigneeDTO updateAssignee(Long id, AssigneeCreateUpdateDTO assigneeDTO) {
        Assignee assignee = assigneeRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignee not found with id " + id));
        // Reject the update when the new email belongs to a different assignee
        if (!assignee.getEmail().equals(assigneeDTO.getEmail()) && assigneeRepository.existsByEmail(assigneeDTO.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }
        assignee.setPrename(assigneeDTO.getPrename());
        assignee.setName(assigneeDTO.getName());
        assignee.setEmail(assigneeDTO.getEmail());
        Assignee updatedAssignee = assigneeRepository.save(assignee);
        return convertToDTO(updatedAssignee);
    }

    /**
     * Deletes an assignee and removes it from every todo it was assigned to.
     *
     * <p>This method is transactional so that the todo updates and the assignee
     * deletion either all succeed or all roll back, preventing inconsistent state.
     *
     * @param id the identifier of the assignee to delete
     * @throws ResponseStatusException with status {@code 404} when the assignee does not exist
     */
    public void deleteAssignee(Long id) {
        Assignee assignee = assigneeRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignee not found with id " + id));
        List<Todo> todosToUpdate = todoRepository.findByAssigneeListContaining(assignee);
        for (Todo todo : todosToUpdate) {
            todo.getAssigneeList().remove(assignee);
        }
        todoRepository.saveAll(todosToUpdate);
        assigneeRepository.delete(assignee);
    }
}
