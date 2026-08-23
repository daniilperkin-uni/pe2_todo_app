package de.unistuttgart.iste.ese.api.todo.dto;

import java.util.List;
import java.util.Map;

/**
 * Aggregated statistics over all todos.
 *
 * <p>Computed purely in {@code TodoService} from the repository contents -
 * no database-side aggregation - so the numbers stay trivially testable.
 */
public class TodoStatsDTO {

    private long totalTodos;
    private long finishedTodos;

    /** Average days between createdDate and finishedDate for finished todos; null when none finished. */
    private Double averageDaysToFinish;

    /** Count of todos per priority (LOW/MEDIUM/HIGH). */
    private Map<String, Long> todosPerPriority;

    /** Count of todos per category as assigned by the ML classifier. */
    private Map<String, Long> todosPerCategory;

    /** One row per assignee that owns at least one todo. */
    private List<AssigneeCount> todosPerAssignee;

    public long getTotalTodos() {
        return totalTodos;
    }

    public void setTotalTodos(long totalTodos) {
        this.totalTodos = totalTodos;
    }

    public long getFinishedTodos() {
        return finishedTodos;
    }

    public void setFinishedTodos(long finishedTodos) {
        this.finishedTodos = finishedTodos;
    }

    /**
     * Completion rate in percent (0-100); 0 when there are no todos.
     */
    public double getCompletionRate() {
        return totalTodos == 0 ? 0.0 : Math.round(1000.0 * finishedTodos / totalTodos) / 10.0;
    }

    public Double getAverageDaysToFinish() {
        return averageDaysToFinish;
    }

    public void setAverageDaysToFinish(Double averageDaysToFinish) {
        this.averageDaysToFinish = averageDaysToFinish;
    }

    public Map<String, Long> getTodosPerPriority() {
        return todosPerPriority;
    }

    public void setTodosPerPriority(Map<String, Long> todosPerPriority) {
        this.todosPerPriority = todosPerPriority;
    }

    public Map<String, Long> getTodosPerCategory() {
        return todosPerCategory;
    }

    public void setTodosPerCategory(Map<String, Long> todosPerCategory) {
        this.todosPerCategory = todosPerCategory;
    }

    public List<AssigneeCount> getTodosPerAssignee() {
        return todosPerAssignee;
    }

    public void setTodosPerAssignee(List<AssigneeCount> todosPerAssignee) {
        this.todosPerAssignee = todosPerAssignee;
    }

    /**
     * Number of todos owned by a single assignee.
     */
    public static class AssigneeCount {

        private Long assigneeId;
        private String prename;
        private String name;
        private long count;

        public AssigneeCount(Long assigneeId, String prename, String name, long count) {
            this.assigneeId = assigneeId;
            this.prename = prename;
            this.name = name;
            this.count = count;
        }

        public Long getAssigneeId() {
            return assigneeId;
        }

        public void setAssigneeId(Long assigneeId) {
            this.assigneeId = assigneeId;
        }

        public String getPrename() {
            return prename;
        }

        public void setPrename(String prename) {
            this.prename = prename;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }
    }
}
