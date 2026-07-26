package de.unistuttgart.iste.ese.api.assignee;

import de.unistuttgart.iste.ese.api.assignee.dto.AssigneeCreateUpdateDTO;
import de.unistuttgart.iste.ese.api.assignee.dto.AssigneeDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for the {@link Assignee} resource.
 *
 * <p>This controller is intentionally thin: it only performs HTTP wiring and
 * delegates all business logic to {@link AssigneeService}.
 */
@RestController
@RequestMapping("/api/v1/assignees")
public class AssigneeController {

    private final AssigneeService assigneeService;

    /**
     * Constructor for dependency injection.
     *
     * @param assigneeService the service handling assignee business logic
     */
    public AssigneeController(AssigneeService assigneeService) {
        this.assigneeService = assigneeService;
    }

    /**
     * Retrieves all assignees.
     *
     * @return a list of all assignees as DTOs
     */
    @GetMapping
    public List<AssigneeDTO> getAllAssignees() {
        return assigneeService.getAllAssignees();
    }

    /**
     * Retrieves a single assignee by its identifier.
     *
     * @param id the assignee identifier
     * @return the matching assignee as DTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<AssigneeDTO> getAssigneeById(@PathVariable Long id) {
        return ResponseEntity.ok(assigneeService.getAssigneeById(id));
    }

    /**
     * Creates a new assignee.
     *
     * @param assigneeDTO the validated data for the new assignee
     * @return the created assignee as DTO with HTTP status {@code 201 Created}
     */
    @PostMapping
    public ResponseEntity<AssigneeDTO> createAssignee(@Valid @RequestBody AssigneeCreateUpdateDTO assigneeDTO) {
        return new ResponseEntity<>(assigneeService.createAssignee(assigneeDTO), HttpStatus.CREATED);
    }

    /**
     * Updates an existing assignee.
     *
     * @param id          the identifier of the assignee to update
     * @param assigneeDTO the validated new data
     * @return the updated assignee as DTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<AssigneeDTO> updateAssignee(@PathVariable Long id, @Valid @RequestBody AssigneeCreateUpdateDTO assigneeDTO) {
        return ResponseEntity.ok(assigneeService.updateAssignee(id, assigneeDTO));
    }

    /**
     * Deletes an assignee and removes it from all associated todos.
     *
     * @param id the identifier of the assignee to delete
     * @return an empty response with HTTP status {@code 200 OK}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignee(@PathVariable Long id) {
        assigneeService.deleteAssignee(id);
        return ResponseEntity.ok().build();
    }
}
