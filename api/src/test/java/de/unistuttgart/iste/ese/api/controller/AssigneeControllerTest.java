package de.unistuttgart.iste.ese.api.controller;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import static de.unistuttgart.iste.ese.api.controller.TestUtil.getEmail;
import static de.unistuttgart.iste.ese.api.controller.TestUtil.getId;
import static de.unistuttgart.iste.ese.api.controller.TestUtil.getName;
import static de.unistuttgart.iste.ese.api.controller.TestUtil.getPrename;
import static de.unistuttgart.iste.ese.api.controller.TestUtil.getRandomMail;
import static de.unistuttgart.iste.ese.api.controller.TestUtil.getRandomName;
import static de.unistuttgart.iste.ese.api.controller.TestUtil.getRandomPrename;
import static de.unistuttgart.iste.ese.api.controller.TestUtil.setEmail;
import static de.unistuttgart.iste.ese.api.controller.TestUtil.setId;
import static de.unistuttgart.iste.ese.api.controller.TestUtil.setName;
import static de.unistuttgart.iste.ese.api.controller.TestUtil.setPrename;
import static de.unistuttgart.iste.ese.api.controller.TestUtil.testAssigneeReq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AssigneeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("create a valid assignee (201)")
    public void createValidAssignee() throws Exception {
        createAssigneeSuccessful(testAssigneeReq());
    }

    @Test
    @DisplayName("retrieve the created assignee (200)")
    public void retrieveAssignee() throws Exception {
        JSONObject testAssignee = createAssigneeSuccessful(testAssigneeReq());

        mockMvc.perform(get("/api/v1/assignees/{id}", getId(testAssignee)))
               .andExpect(status().isOk())
               .andExpect(matchesAssignee(testAssignee));
    }

    @Test
    @DisplayName("retrieve all assignees (check for the newly created one)")
    public void retrieveAllAssignees() throws Exception {
        JSONObject testAssignee = createAssigneeSuccessful(testAssigneeReq());

        mockMvc.perform(get("/api/v1/assignees"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$.length()").value(1))
               .andExpect(jsonPath("$[0].id").value(getId(testAssignee)))
               .andExpect(jsonPath("$[0].prename").value(getPrename(testAssignee)))
               .andExpect(jsonPath("$[0].name").value(getName(testAssignee)))
               .andExpect(jsonPath("$[0].email").value(getEmail(testAssignee)));
    }

    @Test
    @DisplayName("edit the created assignee (200) and retrieve it with the change")
    public void editAssignee() throws Exception {
        JSONObject testAssignee = createAssigneeSuccessful(testAssigneeReq());

        setName(testAssignee, getRandomName());
        setPrename(testAssignee, getRandomPrename());
        setEmail(testAssignee, getRandomMail(getPrename(testAssignee), getName(testAssignee)));

        mockMvc.perform(put("/api/v1/assignees/{id}", getId(testAssignee))
                   .contentType(MediaType.APPLICATION_JSON)
                   .content(testAssignee.toString()))
               .andExpect(status().isOk())
               .andExpect(matchesAssignee(testAssignee));
    }

    @Test
    @DisplayName("delete the created assignee (200) and try to retrieve it (404)")
    public void deleteAssignee() throws Exception {
        JSONObject testAssignee = createAssigneeSuccessful(testAssigneeReq());

        mockMvc.perform(delete("/api/v1/assignees/{id}", getId(testAssignee)))
               .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/assignees/{id}", getId(testAssignee)))
               .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("delete for non-existing assignee fails (404)")
    public void deleteAssigneeNotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/assignees/1"))
               .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("retrieve for non-existing assignee fails (404)")
    public void retrieveAssigneeNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/assignees/1"))
               .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("edit for non-existing assignee fails (404)")
    public void editAssigneeNotFound() throws Exception {
        JSONObject testAssignee = testAssigneeReq();
        setId(testAssignee, 1L);

        mockMvc.perform(put("/api/v1/assignees/{id}", getId(testAssignee))
                   .contentType(MediaType.APPLICATION_JSON)
                   .content(testAssignee.toString()))
               .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("validation: assignee with empty prename fails (400)")
    public void createEmptyPrenameAssignee() throws Exception {
        JSONObject testAssignee = testAssigneeReq();
        setPrename(testAssignee, null);
        createAssignee(testAssignee, status().isBadRequest());

        testAssignee = testAssigneeReq();
        setPrename(testAssignee, "");
        createAssignee(testAssignee, status().isBadRequest());
    }

    @Test
    @DisplayName("validation: assignee with empty name fails (400)")
    public void createEmptyNameAssignee() throws Exception {
        JSONObject testAssignee = testAssigneeReq();
        setName(testAssignee, null);
        createAssignee(testAssignee, status().isBadRequest());

        testAssignee = testAssigneeReq();
        setName(testAssignee, "");
        createAssignee(testAssignee, status().isBadRequest());
    }

    @Test
    @DisplayName("validation: assignee with empty email fails (400)")
    public void createEmptyEmailAssignee() throws Exception {
        JSONObject testAssignee = testAssigneeReq();
        setEmail(testAssignee, "");
        createAssignee(testAssignee, status().isBadRequest());

        testAssignee = testAssigneeReq();
        setEmail(testAssignee, null);
        System.out.println(testAssignee);
        createAssignee(testAssignee, status().isBadRequest());
    }

    @Test
    @DisplayName("validation: assignee with valid non-uni email fails (400)")
    public void createInvalidEmailAssignee() throws Exception {
        JSONObject testAssignee = testAssigneeReq();
        setEmail(testAssignee, getPrename(testAssignee) + getName(testAssignee) + "@valid.email.de");
        createAssignee(testAssignee, status().isBadRequest());
    }

    @Test
    @DisplayName("validation: assignee with invalid uni email fails (400)")
    public void createNonUniEmailAssignee() throws Exception {
        JSONObject testAssignee = testAssigneeReq();
        setEmail(testAssignee, "@iste.uni-stuttgart.de");
        createAssignee(testAssignee, status().isBadRequest());
    }

    private JSONObject createAssignee(JSONObject testAssignee, ResultMatcher... resultMatchers) throws Exception {
        MockHttpServletResponse response = mockMvc.perform(post("/api/v1/assignees").contentType(MediaType.APPLICATION_JSON_VALUE).content(testAssignee.toString()))
                                                  .andExpectAll(resultMatchers)
                                                  .andReturn().getResponse();
        if ((response.getStatus() != 201 && response.getStatus() != 200) || response.getContentAsString().isEmpty()) {
            return null;
        }
        return new JSONObject(response.getContentAsString());
    }

    private JSONObject createAssigneeSuccessful(JSONObject testAssignee) throws Exception {
        return createAssignee(testAssignee,
            status().isCreated(),
            jsonPath("$.id").isNumber(),
            jsonPath("$.prename").value(testAssignee.getString("prename")),
            jsonPath("$.name").value(testAssignee.getString("name")),
            jsonPath("$.email").value(testAssignee.getString("email")));
    }

    private ResultMatcher matchesAssignee(JSONObject expected) throws Exception {
        List<ResultMatcher> matchers = new ArrayList<>();
        matchers.add(jsonPath("$.id").isNumber());
        matchers.add(jsonPath("$.id").value(expected.getLong("id")));
        matchers.add(jsonPath("$.prename").value(expected.getString("prename")));
        matchers.add(jsonPath("$.name").value(expected.getString("name")));
        matchers.add(jsonPath("$.email").value(expected.getString("email")));

        return result -> {
            for (ResultMatcher m : matchers) {
                m.match(result);
            }
        };
    }
}
