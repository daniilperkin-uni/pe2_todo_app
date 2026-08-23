package de.unistuttgart.iste.ese.api.todo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * A single piece of human feedback about the todo model's predictions.
 *
 * <p>Recorded when a user rejects a predicted {@link Priority} via the
 * thumbs-down control and names the priority they consider correct. The rows
 * are training-data candidates for a future retrain of {@link TodoClassifier}
 * and are exposed read-only through the corrections stats endpoint.
 */
@Entity
@Table(name = "priority_corrections")
public class PriorityCorrection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Title of the todo the prediction was made for. */
    @NotBlank
    private String todoTitle;

    /** The priority the system had assigned. */
    @NotNull
    private String predictedPriority;

    /** The priority the user says is correct. */
    @NotNull
    private String correctedPriority;

    /** Category of the todo at correction time (context for later analysis). */
    private String category;

    public Long getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @throws IllegalStateException if the id has already been assigned (the id is
     *     JPA-generated and must not be mutated after persist)
     */
    public void setId(Long id) {
        if (this.id != null) {
            throw new IllegalStateException("PriorityCorrection id is already set to " + this.id + " and cannot be mutated.");
        }
        this.id = id;
    }

    public String getTodoTitle() {
        return todoTitle;
    }

    public void setTodoTitle(String todoTitle) {
        this.todoTitle = todoTitle;
    }

    public String getPredictedPriority() {
        return predictedPriority;
    }

    public void setPredictedPriority(String predictedPriority) {
        this.predictedPriority = predictedPriority;
    }

    public String getCorrectedPriority() {
        return correctedPriority;
    }

    public void setCorrectedPriority(String correctedPriority) {
        this.correctedPriority = correctedPriority;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
