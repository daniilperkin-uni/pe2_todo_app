package de.unistuttgart.iste.ese.api.todo.dto;

import de.unistuttgart.iste.ese.api.assignee.dto.AssigneeDTO;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.List;

public class TodoDTO {
    private Long id;
    private String title;
    private String description;
    private boolean finished;
    private String priority;
    private List<AssigneeDTO> assigneeList;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate finishedDate;
    private String category;

    // Getter und Setter

    /**
     * Gibt die ID des Todos zurück
     */
    public Long getId() {
        return id;
    }

    /**
     * Setzt die ID des Todos
     */
    public void setId(Long id) {
        this.id = id;
    }

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
     * Gibt die Liste der zugewiesenen Personen zurück
     */
    public List<AssigneeDTO> getAssigneeList() {
        return assigneeList;
    }

    /**
     * Setzt die Liste der zugewiesenen Personen
     */
    public void setAssigneeList(List<AssigneeDTO> assigneeList) {
        this.assigneeList = assigneeList;
    }

    /**
     * Gibt das Erstellungsdatum des Todos zurück
     */
    public LocalDate getCreatedDate() {
        return createdDate;
    }

    /**
     * Setzt das Erstellungsdatum des Todos
     */
    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
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
     * Gibt das Abschlussdatum des Todos zurück
     */
    public LocalDate getFinishedDate() {
        return finishedDate;
    }

    /**
     * Setzt das Abschlussdatum des Todos
     */
    public void setFinishedDate(LocalDate finishedDate) {
        this.finishedDate = finishedDate;
    }

    /**
     * Gibt die Kategorie des Todos zurück
     */
    public String getCategory() {
        return category;
    }

    /**
     * Setzt die Kategorie des Todos
     */
    public void setCategory(String category) {
        this.category = category;
    }
}
