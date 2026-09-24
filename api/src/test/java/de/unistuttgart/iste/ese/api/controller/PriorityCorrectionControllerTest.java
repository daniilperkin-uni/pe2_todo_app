package de.unistuttgart.iste.ese.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class PriorityCorrectionControllerTest {

    private static final String URL = "/api/v1/todos/priority-corrections";

    @Autowired private MockMvc mockMvc;

    private void postCorrection(String body, int expectedStatus) throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().is(expectedStatus));
    }

    @Test
    public void recordsCorrectionsAndAggregatesStats() throws Exception {
        postCorrection("{\"todoTitle\":\"a\",\"predictedPriority\":\"low\","
            + "\"correctedPriority\":\"HIGH\",\"category\":\"work\"}", 201);
        postCorrection("{\"todoTitle\":\"b\",\"predictedPriority\":\"LOW\","
            + "\"correctedPriority\":\"HIGH\"}", 201);
        postCorrection("{\"todoTitle\":\"c\",\"predictedPriority\":\"MEDIUM\","
            + "\"correctedPriority\":\"LOW\"}", 201);

        mockMvc.perform(get(URL + "/stats"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalCorrections").value(3))
            .andExpect(jsonPath("$.correctionsPerPriority.HIGH").value(2))
            .andExpect(jsonPath("$.correctionsPerPriority.LOW").value(1));
    }

    @Test
    public void rejectsUnknownPriority() throws Exception {
        postCorrection("{\"todoTitle\":\"a\",\"predictedPriority\":\"URGENT\","
            + "\"correctedPriority\":\"HIGH\"}", 400);
        postCorrection("{\"todoTitle\":\"a\",\"predictedPriority\":\"LOW\","
            + "\"correctedPriority\":\"nope\"}", 400);
    }

    @Test
    public void rejectsBlankTitle() throws Exception {
        postCorrection("{\"todoTitle\":\"\",\"predictedPriority\":\"LOW\","
            + "\"correctedPriority\":\"HIGH\"}", 400);
    }

    @Test
    public void emptyStats() throws Exception {
        mockMvc.perform(get(URL + "/stats"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalCorrections").value(0));
    }
}
