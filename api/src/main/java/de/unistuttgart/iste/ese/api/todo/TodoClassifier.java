package de.unistuttgart.iste.ese.api.todo;

import org.dmg.pmml.FieldName;
import org.jpmml.evaluator.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TodoClassifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(TodoClassifier.class);
    private Evaluator evaluator;

    @PostConstruct
    public void init() {
        try (InputStream inputStream = getClass().getResourceAsStream("/model.pmml")) {
            if (inputStream == null) {
                LOGGER.error("model.pmml not found in resources!");
                return;
            }
            this.evaluator = new LoadingModelEvaluatorBuilder()
                .load(inputStream)
                .build();
            this.evaluator.verify();
            LOGGER.info("PMML model loaded successfully.");
        } catch (Exception e) {
            LOGGER.error("Failed to load PMML model", e);
        }
    }

    public String classify(String title) {
        if (evaluator == null) {
            LOGGER.warn("Evaluator not initialized. Using deterministic fallback.");
            return deterministicFallback(title);
        }

        try {
            Map<FieldName, FieldValue> arguments = new LinkedHashMap<>();
            List<? extends InputField> inputFields = evaluator.getInputFields();

            if (!inputFields.isEmpty()) {
                // Map the title to the first input field of the model
                InputField inputField = inputFields.get(0);
                FieldValue inputValue = inputField.prepare(title);
                arguments.put(inputField.getName(), inputValue);
            }

            Map<FieldName, ?> results = evaluator.evaluate(arguments);
            List<? extends TargetField> targetFields = evaluator.getTargetFields();
            
            if (!targetFields.isEmpty()) {
                TargetField targetField = targetFields.get(0);
                FieldName targetFieldName = targetField.getName();
                Object targetValue = results.get(targetFieldName);
                
                Object label = targetValue instanceof Computable computable
                    ? computable.getResult() : targetValue;
                String category = toCategory(label);
                if (category != null) {
                    return category;
                }
            }
            return deterministicFallback(title);
        } catch (Exception e) {
            LOGGER.error("Error during classification", e);
            return deterministicFallback(title);
        }
    }

    /**
     * The model's target field is named "['private' 'work']" and encodes the
     * class as an integer index into that list.
     */
    private static String toCategory(Object label) {
        if (label == null) {
            return null;
        }
        switch (label.toString()) {
            case "0":
            case "private":
                return "private";
            case "1":
            case "work":
                return "work";
            default:
                return null;
        }
    }

    private String deterministicFallback(String title) {
        if (title == null || title.isBlank()) {
            return "private";
        }
        String lowerCaseTitle = title.toLowerCase();
        if (lowerCaseTitle.contains("deadline") || lowerCaseTitle.contains("meeting") ||
            lowerCaseTitle.contains("task") || lowerCaseTitle.contains("work") ||
            lowerCaseTitle.contains("bug") || lowerCaseTitle.contains("fix") ||
            lowerCaseTitle.contains("urgent") || lowerCaseTitle.contains("report")) {
            return "work";
        }
        return "private";
    }
}
