package de.unistuttgart.iste.ese.api.csv;

import de.unistuttgart.iste.ese.api.todo.Todo;
import de.unistuttgart.iste.ese.api.todo.TodoRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Builds the CSV export of all todos.
 *
 * <p>Keeping the export format here rather than in the controller follows the
 * project layering rule ({@code Controller -> Service -> Repository}) and makes
 * the format testable without a servlet response.
 */
@Service
public class TodoCsvService {

    /** Column order of the export; also the header row. */
    private static final CSVFormat TODOS_FORMAT = CSVFormat.DEFAULT.builder()
        .setHeader("id", "title", "description", "finished", "assignees",
                   "createdDate", "dueDate", "finishedDate", "category", "priority")
        .build();

    private final TodoRepository todoRepository;

    /**
     * Constructor for dependency injection.
     *
     * @param todoRepository repository for accessing todo persistence
     */
    public TodoCsvService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    /**
     * Renders every todo as one CSV record, header row first.
     *
     * <p>Assignees are joined with {@code +} in first-name/last-name form, which
     * is the format the course template expects.
     *
     * @return the complete CSV document
     * @throws IOException when rendering the in-memory document fails
     */
    @Transactional(readOnly = true)
    public String generateTodosCsv() throws IOException {
        List<Todo> todos = todoRepository.findAll();
        StringWriter buffer = new StringWriter();
        try (CSVPrinter csvPrinter = new CSVPrinter(buffer, TODOS_FORMAT)) {
            for (Todo todo : todos) {
                String assignees = todo.getAssigneeList().stream()
                    .map(assignee -> assignee.getPrename() + " " + assignee.getName())
                    .collect(Collectors.joining("+"));

                csvPrinter.printRecord(
                    todo.getId(),
                    todo.getTitle(),
                    todo.getDescription(),
                    todo.isFinished(),
                    assignees,
                    todo.getCreatedDate(),
                    todo.getDueDate(),
                    todo.getFinishedDate(),
                    todo.getCategory(),
                    todo.getPriority()
                );
            }
        }
        return buffer.toString();
    }
}
