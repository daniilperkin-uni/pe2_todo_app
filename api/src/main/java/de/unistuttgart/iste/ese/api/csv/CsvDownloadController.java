package de.unistuttgart.iste.ese.api.csv;

import de.unistuttgart.iste.ese.api.todo.Todo;
import de.unistuttgart.iste.ese.api.todo.TodoRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for downloading entities as CSV files.
 */
@RestController
@RequestMapping("/api/v1/csv-downloads")
public class CsvDownloadController {

    private final TodoRepository todoRepository;

    /**
     * Constructor for dependency injection.
     *
     * @param todoRepository repository for accessing todo persistence
     */
    public CsvDownloadController(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    /**
     * Generates and streams a CSV file containing all todos.
     *
     * @param response the HTTP servlet response used to write the CSV stream
     * @throws IOException when writing to the response writer fails
     */
    @GetMapping("/todos")
    public void downloadTodosCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"todos.csv\"");

        List<Todo> todos = todoRepository.findAll();

        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
            .setHeader("id", "title", "description", "finished", "assignees",
                       "createdDate", "dueDate", "finishedDate", "category", "priority")
            .build();

        try (Writer writer = response.getWriter();
             CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat)) {
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
    }
}
