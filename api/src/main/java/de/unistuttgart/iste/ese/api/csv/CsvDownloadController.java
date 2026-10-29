package de.unistuttgart.iste.ese.api.csv;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;

/**
 * REST controller for downloading entities as CSV files.
 *
 * <p>Thin HTTP wiring only: the export itself is rendered by
 * {@link TodoCsvService}.
 */
@RestController
@RequestMapping("/api/v1/csv-downloads")
public class CsvDownloadController {

    private final TodoCsvService todoCsvService;

    /**
     * Constructor for dependency injection.
     *
     * @param todoCsvService service rendering the todo CSV export
     */
    public CsvDownloadController(TodoCsvService todoCsvService) {
        this.todoCsvService = todoCsvService;
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

        try (Writer writer = response.getWriter()) {
            writer.write(todoCsvService.generateTodosCsv());
        }
    }
}
