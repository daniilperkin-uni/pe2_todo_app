package de.unistuttgart.iste.ese.api.todo;

import de.unistuttgart.iste.ese.api.assignee.Assignee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {
    /**
     * Findet alle Todos, die den angegebenen Zuständigen enthalten
     */
    List<Todo> findByAssigneeListContaining(Assignee assignee);
}
