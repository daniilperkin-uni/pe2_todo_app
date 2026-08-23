package de.unistuttgart.iste.ese.api.todo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Persistence for {@link PriorityCorrection} feedback rows.
 */
@Repository
public interface PriorityCorrectionRepository extends JpaRepository<PriorityCorrection, Long> {
}
