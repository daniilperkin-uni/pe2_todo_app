/** Aggregated todo statistics as returned by GET /api/v1/todos/stats. */
export interface TodoStats {
  totalTodos: number;
  finishedTodos: number;
  /** Completion rate in percent (0-100). */
  completionRate: number;
  /** Average days between createdDate and finishedDate; null when nothing finished yet. */
  averageDaysToFinish: number | null;
  todosPerPriority: Record<string, number>;
  todosPerCategory: Record<string, number>;
  todosPerAssignee: AssigneeCount[];
}

/** Number of todos owned by a single assignee. */
export interface AssigneeCount {
  assigneeId: number;
  prename: string;
  name: string;
  count: number;
}
