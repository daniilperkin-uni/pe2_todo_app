package de.unistuttgart.iste.ese.api.todo;

import de.unistuttgart.iste.ese.api.assignee.Assignee;
import de.unistuttgart.iste.ese.api.assignee.AssigneeRepository;
import de.unistuttgart.iste.ese.api.todo.dto.TodoStatsDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TodoService#getTodoStats()}.
 *
 * Pure Mockito, no Spring context: the repositories are mocked so the
 * aggregation logic itself is what is under test.
 */
class TodoStatsServiceTest {

    private TodoRepository todoRepository;
    private AssigneeRepository assigneeRepository;
    private TodoClassifier todoClassifier;
    private TodoService todoService;

    @BeforeEach
    void setUp() {
        todoRepository = mock(TodoRepository.class);
        assigneeRepository = mock(AssigneeRepository.class);
        todoClassifier = new TodoClassifier();
        todoService = new TodoService(todoRepository, assigneeRepository, todoClassifier);
    }

    private Todo todo(String title, Priority priority, boolean finished,
                      LocalDate created, LocalDate due, LocalDate finishedDate,
                      String category, Assignee... assignees) {
        Todo t = new Todo();
        t.setTitle(title);
        t.setPriority(priority);
        t.setFinished(finished);
        if (finishedDate != null) {
            // createdDate is guarded by the entity setter; use reflection-free
            // construction by setting it before any persist would happen.
            setCreatedDate(t, created);
            t.setFinishedDate(finishedDate);
        } else {
            setCreatedDate(t, created);
        }
        t.setDueDate(due);
        t.setCategory(category);
        for (Assignee a : assignees) {
            t.getAssigneeList().add(a);
        }
        return t;
    }

    private void setCreatedDate(Todo t, LocalDate date) {
        try {
            var field = Todo.class.getDeclaredField("createdDate");
            field.setAccessible(true);
            field.set(t, date);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private Assignee assignee(long id, String prename, String name) {
        Assignee a = new Assignee();
        try {
            var field = Assignee.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(a, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        a.setPrename(prename);
        a.setName(name);
        return a;
    }

    @Test
    @DisplayName("empty repository yields zeroed stats")
    void emptyRepository() {
        when(todoRepository.findAll()).thenReturn(List.of());

        TodoStatsDTO stats = todoService.getTodoStats();

        assertEquals(0, stats.getTotalTodos());
        assertEquals(0, stats.getFinishedTodos());
        assertEquals(0.0, stats.getCompletionRate());
        assertNull(stats.getAverageDaysToFinish());
    }

    @Test
    @DisplayName("completion rate is finished/total in percent")
    void completionRate() {
        when(todoRepository.findAll()).thenReturn(List.of(
            todo("a", Priority.LOW, true, LocalDate.of(2026, 1, 1), null, LocalDate.of(2026, 1, 3), "work"),
            todo("b", Priority.HIGH, false, LocalDate.of(2026, 1, 1), null, null, "private")
        ));

        TodoStatsDTO stats = todoService.getTodoStats();

        assertEquals(2, stats.getTotalTodos());
        assertEquals(1, stats.getFinishedTodos());
        assertEquals(50.0, stats.getCompletionRate());
    }

    @Test
    @DisplayName("averageDaysToFinish averages created->finished over finished todos only")
    void averageDaysToFinish() {
        when(todoRepository.findAll()).thenReturn(List.of(
            // 2 days from creation to finish
            todo("a", Priority.LOW, true, LocalDate.of(2026, 1, 1), null, LocalDate.of(2026, 1, 3), "work"),
            // 4 days
            todo("b", Priority.MEDIUM, true, LocalDate.of(2026, 2, 1), null, LocalDate.of(2026, 2, 5), "work"),
            // unfinished - must not contribute
            todo("c", Priority.HIGH, false, LocalDate.of(2026, 3, 1), null, null, "private")
        ));

        TodoStatsDTO stats = todoService.getTodoStats();

        assertEquals(3.0, stats.getAverageDaysToFinish());
    }

    @Test
    @DisplayName("todosPerPriority counts each priority bucket")
    void todosPerPriority() {
        when(todoRepository.findAll()).thenReturn(List.of(
            todo("a", Priority.LOW, false, LocalDate.now(), null, null, "work"),
            todo("b", Priority.LOW, false, LocalDate.now(), null, null, "work"),
            todo("c", Priority.HIGH, false, LocalDate.now(), null, null, "private")
        ));

        TodoStatsDTO stats = todoService.getTodoStats();

        assertEquals(2L, stats.getTodosPerPriority().get("LOW"));
        assertEquals(1L, stats.getTodosPerPriority().get("HIGH"));
        assertNull(stats.getTodosPerPriority().get("MEDIUM"));
    }

    @Test
    @DisplayName("todosPerCategory groups by category and buckets nulls as 'unclassified'")
    void todosPerCategory() {
        when(todoRepository.findAll()).thenReturn(List.of(
            todo("a", Priority.LOW, false, LocalDate.now(), null, null, "work"),
            todo("b", Priority.LOW, false, LocalDate.now(), null, null, "work"),
            todo("c", Priority.HIGH, false, LocalDate.now(), null, null, null)
        ));

        TodoStatsDTO stats = todoService.getTodoStats();

        assertEquals(2L, stats.getTodosPerCategory().get("work"));
        assertEquals(1L, stats.getTodosPerCategory().get("unclassified"));
    }

    @Test
    @DisplayName("todosPerAssignee counts each assignee across all todos, sorted desc")
    void todosPerAssignee() {
        Assignee alice = assignee(1L, "Alice", "Smith");
        Assignee bob = assignee(2L, "Bob", "Jones");

        when(todoRepository.findAll()).thenReturn(List.of(
            todo("a", Priority.LOW, false, LocalDate.now(), null, null, "work", alice),
            todo("b", Priority.LOW, false, LocalDate.now(), null, null, "work", alice, bob),
            todo("c", Priority.HIGH, false, LocalDate.now(), null, null, "private", alice)
        ));

        TodoStatsDTO stats = todoService.getTodoStats();

        assertEquals(2, stats.getTodosPerAssignee().size());
        assertEquals("Alice", stats.getTodosPerAssignee().getFirst().getPrename());
        assertEquals(3L, stats.getTodosPerAssignee().getFirst().getCount());
        assertEquals("Bob", stats.getTodosPerAssignee().getLast().getPrename());
        assertEquals(1L, stats.getTodosPerAssignee().getLast().getCount());
    }

    @Test
    @DisplayName("an assignee with no todos does not appear in todosPerAssignee")
    void unassignedAssigneeNotListed() {
        when(todoRepository.findAll()).thenReturn(List.of());

        TodoStatsDTO stats = todoService.getTodoStats();

        assertEquals(0, stats.getTodosPerAssignee().size());
        Mockito.verifyNoInteractions(assigneeRepository);
    }

    @Test
    @DisplayName("set-based assignee list still dedupes per-todo membership")
    void assigneeMembershipIsSetBased() {
        Assignee alice = assignee(1L, "Alice", "Smith");
        Todo single = todo("solo", Priority.LOW, false, LocalDate.now(), null, null, "work");
        single.setAssigneeList(Set.of(alice));
        when(todoRepository.findAll()).thenReturn(List.of(single));

        TodoStatsDTO stats = todoService.getTodoStats();

        assertEquals(1, stats.getTodosPerAssignee().size());
        assertEquals(1L, stats.getTodosPerAssignee().getFirst().getCount());
    }
}
