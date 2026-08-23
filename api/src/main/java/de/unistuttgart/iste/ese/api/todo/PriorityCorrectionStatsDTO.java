package de.unistuttgart.iste.ese.api.todo;

import java.util.HashMap;
import java.util.Map;

/**
 * Aggregated counts over recorded priority corrections.
 */
public class PriorityCorrectionStatsDTO {

    private long totalCorrections;

    /** Corrections grouped by predicted -> corrected transition, e.g. "LOW->HIGH". */
    private Map<String, Long> transitions = new HashMap<>();

    /** Corrections per corrected (target) priority - what users say it should be. */
    private Map<String, Long> correctionsPerPriority = new HashMap<>();

    public long getTotalCorrections() {
        return totalCorrections;
    }

    public void setTotalCorrections(long totalCorrections) {
        this.totalCorrections = totalCorrections;
    }

    public Map<String, Long> getTransitions() {
        return transitions;
    }

    public void setTransitions(Map<String, Long> transitions) {
        this.transitions = transitions;
    }

    public Map<String, Long> getCorrectionsPerPriority() {
        return correctionsPerPriority;
    }

    public void setCorrectionsPerPriority(Map<String, Long> correctionsPerPriority) {
        this.correctionsPerPriority = correctionsPerPriority;
    }
}
