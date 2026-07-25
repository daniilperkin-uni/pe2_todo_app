package de.unistuttgart.iste.ese.api.assignee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssigneeRepository extends JpaRepository<Assignee, Long> {
    /**
     * Überprüft, ob ein Zuständiger mit der angegebenen E-Mail-Adresse existiert
     */
    boolean existsByEmail(String email);
}
