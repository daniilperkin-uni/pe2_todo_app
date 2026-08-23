package de.unistuttgart.iste.ese.api.todo;

import de.unistuttgart.iste.ese.api.todo.dto.PriorityCorrectionCreateDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for classifier feedback: recording priority corrections and
 * reading aggregated correction statistics.
 *
 * <p>Thin HTTP wiring only - all logic lives in {@link TodoService}.
 */
@RestController
@RequestMapping("/api/v1/todos/priority-corrections")
public class PriorityCorrectionController {

    private final TodoService todoService;

    /**
     * Constructor for dependency injection.
     *
     * @param todoService the service handling todo business logic, including
     *     priority correction persistence
     */
    public PriorityCorrectionController(TodoService todoService) {
        this.todoService = todoService;
    }

    /**
     * Records a priority correction from classifier feedback.
     *
     * @param correctionDTO the correction data (title, predicted and corrected priority)
     * @return the persisted correction with HTTP status {@code 201 Created}
     */
    @PostMapping
    public ResponseEntity<PriorityCorrection> createCorrection(
            @Valid @RequestBody PriorityCorrectionCreateDTO correctionDTO) {
        return new ResponseEntity<>(todoService.recordPriorityCorrection(correctionDTO), HttpStatus.CREATED);
    }

    /**
     * Retrieves aggregated correction statistics.
     *
     * @return total corrections plus counts per transition and per target priority
     */
    @GetMapping("/stats")
    public PriorityCorrectionStatsDTO getCorrectionStats() {
        return todoService.getPriorityCorrectionStats();
    }
}
