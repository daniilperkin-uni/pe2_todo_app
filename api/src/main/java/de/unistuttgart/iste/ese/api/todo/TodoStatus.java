package de.unistuttgart.iste.ese.api.todo;

/**
 * Workflow status of a {@link Todo} on the kanban board.
 *
 * <ul>
 *   <li>{@link #OPEN} - not started yet</li>
 *   <li>{@link #IN_PROGRESS} - currently being worked on</li>
 *   <li>{@link #DONE} - completed (mirrors the boolean finished flag)</li>
 * </ul>
 */
public enum TodoStatus {
    OPEN,
    IN_PROGRESS,
    DONE
}
