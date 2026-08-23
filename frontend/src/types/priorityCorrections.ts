/** Aggregated priority-correction statistics from GET /api/v1/todos/priority-corrections/stats. */
export interface PriorityCorrectionStats {
  totalCorrections: number;
  /** Corrections per predicted->corrected transition, e.g. { "LOW->HIGH": 3 }. */
  transitions: Record<string, number>;
  /** Corrections per corrected (target) priority. */
  correctionsPerPriority: Record<string, number>;
}
