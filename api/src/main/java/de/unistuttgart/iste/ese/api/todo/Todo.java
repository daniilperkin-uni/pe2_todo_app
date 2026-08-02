package de.unistuttgart.iste.ese.api.todo;

import com.fasterxml.jackson.annotation.JsonFormat;
import de.unistuttgart.iste.ese.api.assignee.Assignee;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

// Repräsentiert eine Todo in der Datenbank
@Entity
@Table(name = "todos")
public class Todo {

    // Eindeutige ID der Todo
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Titel der Todo
    @NotBlank(message = "Title must not be blank")
    @Column(nullable = false)
    private String title;

    // Beschreibung der Todo
    private String description;

    // Status, ob die Todo erledigt ist
    @Column(nullable = false)
    private boolean finished = false;

    // Priorität der Todo
    @NotNull(message = "Priority must not be null")
    @Enumerated(EnumType.STRING)
    private Priority priority;

    // Erstellungsdatum der Todo
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(nullable = false)
    private LocalDate createdDate;

    // Fälligkeitsdatum der Todo
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    // Datum der Erledigung der Todo
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate finishedDate;

    // Liste der zuständigen Personen für diese Todo
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinTable(
        name = "todo_assignee",
        joinColumns = @JoinColumn(name = "todo_id"),
        inverseJoinColumns = @JoinColumn(name = "assignee_id")
    )
    private Set<Assignee> assigneeList = new HashSet<>();

    // Kategorie der Todo (z.B. "work", "private")
    private String category;

    /**
     * Setzt das Erstellungsdatum vor dem Speichern
     */
    @PrePersist
    protected void onCreate() {
        createdDate = LocalDate.now();
    }

    // Getter und Setter

    /**
     * Gibt die ID des Todos zurück
     */
    public Long getId() {
        return id;
    }

    /**
     * Setzt die ID des Todos
     *
     * @throws IllegalStateException if the id has already been assigned (the id is
     *     JPA-generated and must not be mutated after persist)
     */
    public void setId(Long id) {
        if (this.id != null) {
            throw new IllegalStateException("Todo id is already set to " + this.id + " and cannot be mutated.");
        }
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
    public Priority getPriority() {
        return priority;
    }

    /**
     * Setzt die Priorität des Todos
     */
    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    /**
     * Gibt das Erstellungsdatum des Todos zurück
     */
    public LocalDate getCreatedDate() {
        return createdDate;
    }

    /**
     * Setzt das Erstellungsdatum des Todos
     *
     * @throws IllegalStateException if the created date has already been set, as it
     *     is an audit timestamp populated by {@link #onCreate()} via @PrePersist and
     *     must not be overridden
     */
    public void setCreatedDate(LocalDate createdDate) {
        if (this.createdDate != null) {
            throw new IllegalStateException(
                "createdDate is set by @PrePersist and cannot be overridden.");
        }
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
     * Gibt die Liste der zugewiesenen Personen zurück
     */
    public Set<Assignee> getAssigneeList() {
        return assigneeList;
    }

    /**
     * Setzt die Liste der zugewiesenen Personen
     */
    public void setAssigneeList(Set<Assignee> assigneeList) {
        this.assigneeList = assigneeList;
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
