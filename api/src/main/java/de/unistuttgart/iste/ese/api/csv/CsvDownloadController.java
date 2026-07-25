package de.unistuttgart.iste.ese.api.csv;

import de.unistuttgart.iste.ese.api.todo.Todo;
import de.unistuttgart.iste.ese.api.todo.TodoRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/csv-downloads")
public class CsvDownloadController {

    @Autowired
    private TodoRepository todoRepository;

    @GetMapping("/todos")
    public void downloadTodosCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"todos.csv\"");

        List<Todo> todos = todoRepository.findAll();

        try (Writer writer = response.getWriter();
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                .withHeader("id", "title", "description", "finished", "assignees",
                            "createdDate", "dueDate", "finishedDate", "category", "priority"))) {

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
