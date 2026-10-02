package de.unistuttgart.iste.ese.api.todo;

import de.unistuttgart.iste.ese.api.assignee.Assignee;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {

    /**
     * Findet alle Todos inklusive ihrer Zuständigen in einer einzigen Abfrage.
     *
     * <p>Der Fetch-Join ersetzt die N+1-Lazy-Loads, die beim DTO-Mapping und
     * beim CSV-Export sonst pro Todo eine eigene Abfrage auslösen würden.
     */
    @Override
    @EntityGraph(attributePaths = "assigneeList")
    List<Todo> findAll();

    /**
     * Findet alle Todos, die den angegebenen Zuständigen enthalten
     */
    List<Todo> findByAssigneeListContaining(Assignee assignee);
}
