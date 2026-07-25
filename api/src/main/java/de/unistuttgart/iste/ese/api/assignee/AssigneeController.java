package de.unistuttgart.iste.ese.api.assignee;

import de.unistuttgart.iste.ese.api.assignee.dto.AssigneeCreateUpdateDTO;
import de.unistuttgart.iste.ese.api.assignee.dto.AssigneeDTO;
import de.unistuttgart.iste.ese.api.todo.Todo;
import de.unistuttgart.iste.ese.api.todo.TodoRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST-Controller für die Verwaltung von Zuständigen
 */
@RestController
@RequestMapping("/api/v1/assignees")
public class AssigneeController {

    @Autowired
    private TodoRepository todoRepository;

    private final AssigneeRepository assigneeRepository;

    /**
     * Konstruktor für Dependency Injection
     */
    public AssigneeController(AssigneeRepository assigneeRepository) {
        this.assigneeRepository = assigneeRepository;
    }

    /**
     * Konvertiert ein Assignee-Entity in ein Assignee-DTO
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
     * Konvertiert ein DTO in ein Assignee-Entity
     */
    private Assignee convertToEntity(AssigneeCreateUpdateDTO dto) {
        Assignee assignee = new Assignee();
        assignee.setPrename(dto.getPrename());
        assignee.setName(dto.getName());
        assignee.setEmail(dto.getEmail());
        return assignee;
    }

    /**
     * Ruft alle Zuständigen ab
     */
    @GetMapping
    public List<AssigneeDTO> getAllAssignees() {
        return assigneeRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Ruft einen Zuständigen anhand seiner ID ab
     */
    @GetMapping("/{id}")
    public ResponseEntity<AssigneeDTO> getAssigneeById(@PathVariable Long id) {
        Optional<Assignee> assignee = assigneeRepository.findById(id);
        if (assignee.isPresent()) {
            return ResponseEntity.ok(convertToDTO(assignee.get()));
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignee not found with id " + id);
        }
    }

    /**
     * Erstellt einen neuen Zuständigen
     */
    @PostMapping
    public ResponseEntity<AssigneeDTO> createAssignee(@Valid @RequestBody AssigneeCreateUpdateDTO assigneeDTO) {
        if (assigneeRepository.existsByEmail(assigneeDTO.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }
        Assignee newAssignee = convertToEntity(assigneeDTO);
        Assignee savedAssignee = assigneeRepository.save(newAssignee);
        return new ResponseEntity<>(convertToDTO(savedAssignee), HttpStatus.CREATED);
    }

    /**
     * Aktualisiert einen vorhandenen Zuständigen
     */
    @PutMapping("/{id}")
    public ResponseEntity<AssigneeDTO> updateAssignee(@PathVariable Long id, @Valid @RequestBody AssigneeCreateUpdateDTO assigneeDTO) {
        return assigneeRepository.findById(id)
            .map(assignee -> {
                // Prüfen, ob die neue E-Mail bereits von einem anderen Assignee verwendet wird
                if (!assignee.getEmail().equals(assigneeDTO.getEmail()) && assigneeRepository.existsByEmail(assigneeDTO.getEmail())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
                }
                assignee.setPrename(assigneeDTO.getPrename());
                assignee.setName(assigneeDTO.getName());
                assignee.setEmail(assigneeDTO.getEmail());
                Assignee updatedAssignee = assigneeRepository.save(assignee);
                return ResponseEntity.ok(convertToDTO(updatedAssignee));
            })
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignee not found with id " + id));
    }

    /**
     * Löscht einen Zuständigen und entfernt ihn aus allen zugehörigen Todos
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignee(@PathVariable Long id) {
        return assigneeRepository.findById(id)
            .map(assignee -> {

                List<Todo> todosToUpdate = todoRepository.findByAssigneeListContaining(assignee);

                for (Todo todo : todosToUpdate) {
                    todo.getAssigneeList().remove(assignee);
                }

                todoRepository.saveAll(todosToUpdate);

                assigneeRepository.delete(assignee);

                return ResponseEntity.ok().<Void>build();
            })
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignee not found with id " + id));
    }
}
