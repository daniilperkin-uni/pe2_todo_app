package de.unistuttgart.iste.ese.api.todo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.List;

public class TodoCreateUpdateDTO {
    @NotBlank(message = "Title must not be blank")
    private String title;

    private String description;
    private boolean finished;
    private String priority; // Als String, da JSON Enums nicht direkt kennt

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    private List<Long> assigneeIdList; // Optional

    // Getter und Setter

    /**
     * Gibt den Titel des Todos zurück
     */
    public String getTitle() {
        return title;
    }

    /**
     * Setzt den Titel des Todos
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gibt die Beschreibung des Todos zurück
     */
    public String getDescription() {
        return description;
    }

    /**
     * Setzt die Beschreibung des Todos
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gibt den Status des Todos zurück (erledigt oder nicht)
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * Setzt den Status des Todos (erledigt oder nicht)
     */
    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    /**
     * Gibt die Priorität des Todos zurück
     */
    public String getPriority() {
        return priority;
    }

    /**
     * Setzt die Priorität des Todos
     */
    public void setPriority(String priority) {
        this.priority = priority;
    }

    /**
     * Gibt das Fälligkeitsdatum des Todos zurück
     */
    public LocalDate getDueDate() {
        return dueDate;
    }

    /**
     * Setzt das Fälligkeitsdatum des Todos
     */
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    /**
     * Gibt die Liste der zugewiesenen Bearbeiter-IDs zurück
     */
    public List<Long> getAssigneeIdList() {
        return assigneeIdList;
    }

    /**
     * Setzt die Liste der zugewiesenen Bearbeiter-IDs
     */
    public void setAssigneeIdList(List<Long> assigneeIdList) {
        this.assigneeIdList = assigneeIdList;
    }
}
