package de.unistuttgart.iste.ese.api.todo;

import org.dmg.pmml.FieldName;
import org.dmg.pmml.Model;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.*;
import org.jpmml.model.PMMLUtil;
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
            PMML pmml = PMMLUtil.unmarshal(inputStream);
            Model model = pmml.getModels().get(0);
            ModelEvaluatorFactory modelEvaluatorFactory = ModelEvaluatorFactory.newInstance();
            this.evaluator = modelEvaluatorFactory.newModelEvaluator(pmml, model);
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
                
                if (targetValue instanceof Computable) {
                     return ((Computable) targetValue).getResult().toString();
                } else if (targetValue != null) {
                     return targetValue.toString();
                }
            }
            return deterministicFallback(title);
        } catch (Exception e) {
            LOGGER.error("Error during classification", e);
            return deterministicFallback(title);
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
