package de.unistuttgart.iste.ese.api.todo;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link TodoClassifier}.
 *
 * Covers both code paths without needing Spring or a servlet stack:
 * - the live path: init() unmarshals the bundled /model.pmml and classify()
 *   evaluates through the JPMML evaluator (or falls back if a single title
 *   makes the model throw),
 * - the fallback path: an instance whose init() was never called has no
 *   evaluator, so classify() must return the deterministic keyword result.
 */
class TodoClassifierTest {

    private TodoClassifier classifierWithModel;
    private TodoClassifier classifierWithoutModel;

    @BeforeEach
    void setUp() {
        classifierWithModel = new TodoClassifier();
        classifierWithModel.init();
        classifierWithoutModel = new TodoClassifier();
    }

    private Object evaluatorOf(TodoClassifier classifier) throws Exception {
        Field evaluatorField = TodoClassifier.class.getDeclaredField("evaluator");
        evaluatorField.setAccessible(true);
        return evaluatorField.get(classifier);
    }

    private void assertValidCategory(String category) {
        assertNotNull(category, "classifier must never return null");
        assertTrue(category.equals("work") || category.equals("private"),
            "expected 'work' or 'private' but got: " + category);
    }

    @Test
    @DisplayName("init() builds the JPMML evaluator from the bundled model.pmml")
    void initBuildsEvaluatorFromBundledModel() throws Exception {
        assertNotNull(evaluatorOf(classifierWithModel),
            "init() should have loaded /model.pmml from src/main/resources");
    }

    @Test
    @DisplayName("an instance whose init() was never called has no evaluator")
    void missingInitLeavesEvaluatorNull() throws Exception {
        assertNull(evaluatorOf(classifierWithoutModel));
    }

    @Test
    @DisplayName("fallback maps work keywords to 'work'")
    void fallbackMapsWorkKeywords() {
        assertEquals("work", classifierWithoutModel.classify("Finish report until friday"));
        assertEquals("work", classifierWithoutModel.classify("team meeting"));
        assertEquals("work", classifierWithoutModel.classify("fix login bug"));
        assertEquals("work", classifierWithoutModel.classify("urgent: submit taxes"));
    }

    @Test
    @DisplayName("fallback maps titles without work keywords to 'private'")
    void fallbackMapsNonKeywordsToPrivate() {
        assertEquals("private", classifierWithoutModel.classify("buy milk"));
        assertEquals("private", classifierWithoutModel.classify("gym session"));
        assertEquals("private", classifierWithoutModel.classify("call grandma"));
    }

    @Test
    @DisplayName("fallback keyword matching is case-insensitive")
    void fallbackIsCaseInsensitive() {
        assertEquals("work", classifierWithoutModel.classify("DEADLINE extension request"));
        assertEquals("work", classifierWithoutModel.classify("Urgent doctor appointment paperwork"));
    }

    @Test
    @DisplayName("fallback returns 'private' for null and blank titles")
    void fallbackHandlesNullOrBlankTitles() {
        assertEquals("private", classifierWithoutModel.classify(null));
        assertEquals("private", classifierWithoutModel.classify(""));
        assertEquals("private", classifierWithoutModel.classify("   "));
    }

    @Test
    @DisplayName("live model path returns a valid category for arbitrary titles")
    void liveModelReturnsValidCategories() {
        // Titles chosen to include ones the PMML text-index model evaluates
        // directly and ones known to make it throw internally (the controller
        // tests log 'Error during classification' for similar input) - either
        // way classify() must return a usable category, never throw.
        assertValidCategory(classifierWithModel.classify("annual budget planning"));
        assertValidCategory(classifierWithModel.classify("buy oat milk"));
        assertValidCategory(classifierWithModel.classify("!"));
        assertValidCategory(classifierWithModel.classify("ä ö ü unicode title"));
    }

    @Test
    @DisplayName("uninitialized instance falls back deterministically on any input")
    void uninitializedInstanceNeverThrows() {
        assertValidCategory(classifierWithoutModel.classify("annual budget planning"));
        assertValidCategory(classifierWithoutModel.classify(null));
    }
}
