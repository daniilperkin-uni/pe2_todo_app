package de.unistuttgart.iste.ese.api.todo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Incoming payload when a user corrects a predicted priority.
 */
public class PriorityCorrectionCreateDTO {

    @NotBlank(message = "Todo title must not be blank")
    private String todoTitle;

    @NotNull(message = "Predicted priority must not be null")
    private String predictedPriority;

    @NotNull(message = "Corrected priority must not be null")
    private String correctedPriority;

    private String category;

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
