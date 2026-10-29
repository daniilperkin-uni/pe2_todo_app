package de.unistuttgart.iste.ese.api.csv;

import de.unistuttgart.iste.ese.api.assignee.Assignee;
import de.unistuttgart.iste.ese.api.todo.Priority;
import de.unistuttgart.iste.ese.api.todo.Todo;
import de.unistuttgart.iste.ese.api.todo.TodoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TodoCsvService}.
 *
 * <p>Pure Mockito, no servlet response: the export format is asserted directly,
 * which is what moving the rendering out of the controller made possible.
 */
class TodoCsvServiceTest {

    private final TodoRepository todoRepository = mock(TodoRepository.class);
    private final TodoCsvService todoCsvService = new TodoCsvService(todoRepository);

    private Assignee assignee(String prename, String name) {
        Assignee assignee = new Assignee();
        assignee.setPrename(prename);
        assignee.setName(name);
        assignee.setEmail(prename.toLowerCase() + "@uni-stuttgart.de");
        return assignee;
    }

    private Todo todo(long id, String title, boolean finished, Assignee... assignees) {
        Todo todo = new Todo();
        todo.setId(id);
        todo.setTitle(title);
        todo.setDescription("desc " + id);
        todo.setPriority(Priority.MEDIUM);
        todo.setFinished(finished);
        todo.setDueDate(LocalDate.of(2026, 12, 1));
        todo.setCategory("work");
        setCreatedDate(todo, LocalDate.of(2026, 11, 1));
        for (Assignee assignee : assignees) {
            todo.getAssigneeList().add(assignee);
        }
        return todo;
    }

    // createdDate is populated by @PrePersist on save and has no public setter,
    // so the fixture arranges it reflectively (same approach as
    // TodoStatsServiceTest).
    private void setCreatedDate(Todo todo, LocalDate date) {
        try {
            var field = Todo.class.getDeclaredField("createdDate");
            field.setAccessible(true);
            field.set(todo, date);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    @DisplayName("renders the documented header and one record per todo")
    void rendersHeaderAndOneRecordPerTodo() throws Exception {
        when(todoRepository.findAll()).thenReturn(List.of(
            todo(1L, "Buy milk", false, assignee("Anna", "Schmidt"), assignee("Ben", "Doe")),
            todo(2L, "Ship release", true)
        ));

        String[] lines = todoCsvService.generateTodosCsv().split("\r?\n");

        assertThat(lines).hasSize(3);
        assertThat(lines[0]).isEqualTo(
            "id,title,description,finished,assignees,createdDate,dueDate,finishedDate,category,priority");
        assertThat(lines[1]).isEqualTo(
            "1,Buy milk,desc 1,false,Anna Schmidt+Ben Doe,2026-11-01,2026-12-01,,work,MEDIUM");
        assertThat(lines[2]).isEqualTo(
            "2,Ship release,desc 2,true,,2026-11-01,2026-12-01,,work,MEDIUM");
    }

    @Test
    @DisplayName("an empty repository renders the header row only")
    void rendersHeaderOnlyWhenEmpty() throws Exception {
        when(todoRepository.findAll()).thenReturn(List.of());

        String[] lines = todoCsvService.generateTodosCsv().split("\r?\n");

        assertThat(lines).hasSize(1);
        assertThat(lines[0]).startsWith("id,title,description,finished,assignees");
    }
}
