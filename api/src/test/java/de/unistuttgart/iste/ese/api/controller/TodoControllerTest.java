package de.unistuttgart.iste.ese.api.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.unistuttgart.iste.ese.api.todo.Todo;
import de.unistuttgart.iste.ese.api.todo.TodoRepository;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import static de.unistuttgart.iste.ese.api.controller.TestUtil.getAssigneeList;
import static de.unistuttgart.iste.ese.api.controller.TestUtil.getCreatedDate;
import static de.unistuttgart.iste.ese.api.controller.TestUtil.getDescription;
import static de.unistuttgart.iste.ese.api.controller.TestUtil.getDueDate;
import static de.unistuttgart.iste.ese.api.controller.TestUtil.getFinished;
import static de.unistuttgart.iste.ese.api.controller.TestUtil.getId;
import static de.unistuttgart.iste.ese.api.controller.TestUtil.getPriority;
import static de.unistuttgart.iste.ese.api.controller.TestUtil.getRandomTodoDetails;
import static de.unistuttgart.iste.ese.api.controller.TestUtil.getTitle;
import static de.unistuttgart.iste.ese.api.controller.TestUtil.setAssigneeIdList;
import static de.unistuttgart.iste.ese.api.controller.TestUtil.setDescription;
import static de.unistuttgart.iste.ese.api.controller.TestUtil.setDueDate;
import static de.unistuttgart.iste.ese.api.controller.TestUtil.setFinished;
import static de.unistuttgart.iste.ese.api.controller.TestUtil.setPriority;
import static de.unistuttgart.iste.ese.api.controller.TestUtil.setTitle;
import static de.unistuttgart.iste.ese.api.controller.TestUtil.testAssigneeReq;
import static de.unistuttgart.iste.ese.api.controller.TestUtil.testTodoReq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class TodoControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private TodoRepository todoRepository;

    private List<JSONObject> assigneeList = new ArrayList<>();
    private JSONObject testTodo;

    @BeforeEach
    public void setUp() throws Exception {
        for (int i = 0; i < 3; i++) {
            assigneeList.add(createAssigneeSuccessful(testAssigneeReq()));
        }
        testTodo = testTodoReq();
        JSONArray assigneeIdList = new JSONArray(assigneeList.stream().map(jsonObject -> {
            try {
                return jsonObject.getLong("id");
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }).toList());
        setAssigneeIdList(testTodo, assigneeIdList);
    }

    @Test
    @DisplayName("create a valid todo (201)")
    public void createValidTodoEx2() throws Exception {
        createTodoSuccessful(testTodo);
    }

    @Test
    @DisplayName("retrieve the created todo")
    public void retrieveCreatedTodoEx2() throws Exception {
        JSONObject todoJson = createTodoSuccessful(testTodo);

        mockMvc.perform(get("/api/v1/todos/{id}", getId(todoJson)))
               .andExpect(status().isOk())
               .andExpect(matchesTodo(todoJson));
    }

    @Test
    @DisplayName("retrieve all todos (check for the newly created one)")
    public void retrieveAllTodosEx2() throws Exception {
        JSONObject todoJson = createTodoSuccessful(testTodo);

        mockMvc.perform(get("/api/v1/todos"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$.length()").value(1))
               .andExpect(jsonPath("$[0].id").value(getId(todoJson)))
               .andExpect(jsonPath("$[0].title").value(getTitle(todoJson)))
               .andExpect(jsonPath("$[0].priority").value(getPriority(todoJson)))
               .andExpect(jsonPath("$[0].description").value(getDescription(todoJson)))
               .andExpect(jsonPath("$[0].finished").value(getFinished(todoJson)))
               .andExpect(jsonPath("$[0].assigneeList").isArray())
               .andExpect(jsonPath("$[0].assigneeList.length()").value(getAssigneeList(todoJson).length()))
               .andExpect(jsonPath("$[0].dueDate").value(getDueDate(todoJson)))
               .andExpect(jsonPath("$[0].createdDate").value(getCreatedDate(todoJson)));
    }

    @Test
    @DisplayName("delete an assignee (200) and check if the todo is updated")
    public void deleteAssigneeEx2() throws Exception {
        JSONObject todoJson = createTodoSuccessful(testTodo);
        long todoId = getId(todoJson);
        int originalAssigneeListSize = getAssigneeList(todoJson).length();

        mockMvc.perform(delete("/api/v1/assignees/{id}", getId(assigneeList.getFirst()))).andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/todos/{id}", todoId))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.assigneeList").isArray())
               .andExpect(jsonPath("$.assigneeList.length()").value(originalAssigneeListSize - 1));
    }

    @Test
    @DisplayName("edit the created todo (200) and retrieve it with the change")
    public void editTodo() throws Exception {
        JSONObject todoJson = createTodoSuccessful(testTodo);

        String[] newDetails = getRandomTodoDetails();
        setTitle(todoJson, newDetails[0]);
        setDescription(todoJson, newDetails[1]);
        setFinished(todoJson, true);
        setPriority(todoJson, "HIGH");
        setDueDate(todoJson, LocalDate.now().plusDays(new Random().nextInt(10, 30)).format(DateTimeFormatter.ISO_LOCAL_DATE));
        setAssigneeIdList(todoJson, new JSONArray(List.of(getId(assigneeList.get(1)), getId(assigneeList.get(2)))));

        MvcResult result = mockMvc.perform(put("/api/v1/todos/{id}", getId(todoJson)).contentType(MediaType.APPLICATION_JSON_VALUE).content(todoJson.toString()))
                                  .andExpect(status().isOk())
                                  .andExpect(matchesTodo(todoJson)).andReturn();

        todoJson = new JSONObject(result.getResponse().getContentAsString());

        setFinished(todoJson, false);
        JSONArray newAssigneeIdList = new JSONArray();
        newAssigneeIdList.put(assigneeList.getFirst().getLong("id"));
        setAssigneeIdList(todoJson, newAssigneeIdList);
        setPriority(todoJson, "LOW");

        mockMvc.perform(put("/api/v1/todos/{id}", getId(todoJson)).contentType(MediaType.APPLICATION_JSON_VALUE).content(todoJson.toString()))
               .andExpect(status().isOk())
               .andExpect(matchesTodo(todoJson)).andReturn();
    }

    @Test
    @DisplayName("finishing a recurring todo via PUT spawns the next occurrence")
    public void editRecurringTodoSpawnsNextOccurrence() throws Exception {
        testTodo.put("recurrenceRule", "WEEKLY");
        JSONObject todoJson = createTodoSuccessful(testTodo);
        LocalDate dueDate = LocalDate.parse(getDueDate(todoJson));
        setFinished(todoJson, true);

        mockMvc.perform(put("/api/v1/todos/{id}", getId(todoJson)).contentType(MediaType.APPLICATION_JSON_VALUE).content(todoJson.toString()))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.finished").value(true));

        JSONArray todos = new JSONArray(mockMvc.perform(get("/api/v1/todos"))
                                              .andExpect(status().isOk())
                                              .andReturn().getResponse().getContentAsString());
        Assertions.assertEquals(2, todos.length(), "finishing a recurring todo must spawn the next occurrence");
        JSONObject successor = todos.getJSONObject(0).getLong("id") == getId(todoJson) ? todos.getJSONObject(1) : todos.getJSONObject(0);
        Assertions.assertFalse(successor.getBoolean("finished"), "the successor must start unfinished");
        Assertions.assertEquals(getTitle(todoJson), successor.getString("title"));
        Assertions.assertEquals("WEEKLY", successor.getString("recurrenceRule"));
        Assertions.assertEquals(dueDate.plusWeeks(1).format(DateTimeFormatter.ISO_LOCAL_DATE), successor.getString("dueDate"));
    }

    @Test
    @DisplayName("validation: todo with empty title fails (400)")
    public void createInvalidTodoEmptyTitle() throws Exception {
        setTitle(testTodo, "");
        createTodo(testTodo, status().isBadRequest());

        setTitle(testTodo, null);
        createTodo(testTodo, status().isBadRequest());
    }

    @Test
    @DisplayName("validation: todo with invalid assigneeIds fails (400)")
    public void createInvalidTodoInvalidAssigneeIds() throws Exception {
        JSONArray invalidAssigneeIdList = new JSONArray();
        invalidAssigneeIdList.put(Long.MAX_VALUE);
        setAssigneeIdList(testTodo, invalidAssigneeIdList);
        createTodo(testTodo, status().isBadRequest());
    }

    @Test
    @DisplayName("validation: todo with 3x the same assigneeId fails (400)")
    public void createInvalidTodoDuplicateAssigneeIds() throws Exception {
        JSONArray duplicateAssigneeIdList = new JSONArray();
        duplicateAssigneeIdList.put(getId(assigneeList.getFirst()));
        duplicateAssigneeIdList.put(getId(assigneeList.getFirst()));
        duplicateAssigneeIdList.put(getId(assigneeList.getFirst()));
        setAssigneeIdList(testTodo, duplicateAssigneeIdList);
        createTodo(testTodo, status().isBadRequest());
    }

    @Test
    @DisplayName("validation: todo with invalid dueDate fails (400)")
    public void createInvalidTodoInvalidDueDate() throws Exception {
        setDueDate(testTodo, "YOU SHALL NOT PASS!!!");
        createTodo(testTodo, status().isBadRequest());

        setDueDate(testTodo, LocalDate.now().minusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE));
        createTodo(testTodo, status().isBadRequest());
    }

    @Test
    @DisplayName("completing an overdue todo with its unchanged due date succeeds (200)")
    public void editOverdueTodoWithUnchangedDueDate() throws Exception {
        JSONObject todoJson = createTodoSuccessful(testTodo);
        String pastDueDate = LocalDate.now().minusDays(3).format(DateTimeFormatter.ISO_LOCAL_DATE);
        backdateDueDate(getId(todoJson), pastDueDate);

        setFinished(todoJson, true);
        setDueDate(todoJson, pastDueDate);

        mockMvc.perform(put("/api/v1/todos/{id}", getId(todoJson)).contentType(MediaType.APPLICATION_JSON_VALUE).content(todoJson.toString()))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.finished").value(true))
               .andExpect(jsonPath("$.dueDate").value(pastDueDate));
    }

    @Test
    @DisplayName("validation: moving an overdue todo to a new past due date fails (400)")
    public void editOverdueTodoWithNewPastDueDate() throws Exception {
        JSONObject todoJson = createTodoSuccessful(testTodo);
        backdateDueDate(getId(todoJson), LocalDate.now().minusDays(3).format(DateTimeFormatter.ISO_LOCAL_DATE));
        setDueDate(todoJson, LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE));

        mockMvc.perform(put("/api/v1/todos/{id}", getId(todoJson)).contentType(MediaType.APPLICATION_JSON_VALUE).content(todoJson.toString()))
               .andExpect(status().isBadRequest());
    }

    // Moves the stored due date into the past; the API does not allow creating
    // an already overdue todo, so an overdue todo can only be arranged here.
    private void backdateDueDate(long todoId, String dueDate) {
        Todo todo = todoRepository.findById(todoId).orElseThrow();
        todo.setDueDate(LocalDate.parse(dueDate));
        todoRepository.save(todo);
    }

    @Test
    @DisplayName("delete the created todo (200) and try to retrieve it (404)")
    public void deleteTodo() throws Exception {
        JSONObject todoJson = createTodoSuccessful(testTodo);

        mockMvc.perform(delete("/api/v1/todos/{id}", getId(todoJson))).andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/todos/{id}", getId(todoJson))).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("edit for non-existing todo fails (404)")
    public void editNonExistingTodo() throws Exception {
        mockMvc.perform(put("/api/v1/todos/{id}", new Random().nextLong(0, Long.MAX_VALUE)).contentType(MediaType.APPLICATION_JSON_VALUE).content(testTodo.toString()))
               .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("delete for non-existing todo fails (404)")
    public void deleteNonExistingTodo() throws Exception {
        mockMvc.perform(delete("/api/v1/todos/{id}", new Random().nextLong(0, Long.MAX_VALUE))).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("transition todo status to IN_PROGRESS (200) sets finished=false")
    public void transitionStatusToInProgress() throws Exception {
        JSONObject todoJson = createTodoSuccessful(testTodo);

        mockMvc.perform(patch("/api/v1/todos/{id}/status", getId(todoJson))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("\"IN_PROGRESS\""))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
               .andExpect(jsonPath("$.finished").value(false));
    }

    @Test
    @DisplayName("transition todo status to DONE (200) sets finished=true and finishedDate")
    public void transitionStatusToDone() throws Exception {
        JSONObject todoJson = createTodoSuccessful(testTodo);

        mockMvc.perform(patch("/api/v1/todos/{id}/status", getId(todoJson))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("\"DONE\""))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.status").value("DONE"))
               .andExpect(jsonPath("$.finished").value(true))
               .andExpect(jsonPath("$.finishedDate").isNotEmpty());
    }

    @Test
    @DisplayName("transition todo status to OPEN from DONE clears finished and finishedDate")
    public void transitionStatusFromDoneToOpen() throws Exception {
        JSONObject todoJson = createTodoSuccessful(testTodo);

        // First move to DONE
        mockMvc.perform(patch("/api/v1/todos/{id}/status", getId(todoJson))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("\"DONE\""))
               .andExpect(status().isOk());

        // Then re-open by moving back to OPEN
        mockMvc.perform(patch("/api/v1/todos/{id}/status", getId(todoJson))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("\"OPEN\""))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.status").value("OPEN"))
               .andExpect(jsonPath("$.finished").value(false))
               .andExpect(jsonPath("$.finishedDate").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    @DisplayName("transition status for non-existing todo fails (404)")
    public void transitionStatusNonExistingTodo() throws Exception {
        mockMvc.perform(patch("/api/v1/todos/{id}/status", new Random().nextLong(0, Long.MAX_VALUE))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("\"DONE\""))
               .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("transition status with invalid status value fails (400)")
    public void transitionStatusInvalidValue() throws Exception {
        JSONObject todoJson = createTodoSuccessful(testTodo);

        mockMvc.perform(patch("/api/v1/todos/{id}/status", getId(todoJson))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("\"INVALID\""))
               .andExpect(status().isBadRequest());
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

    private JSONObject createTodo(JSONObject testTodo, ResultMatcher... resultMatchers) throws Exception {
        MockHttpServletResponse response = mockMvc.perform(post("/api/v1/todos").contentType(MediaType.APPLICATION_JSON_VALUE).content(testTodo.toString()))
                                                  .andExpectAll(resultMatchers)
                                                  .andReturn().getResponse();
        if ((response.getStatus() != 201 && response.getStatus() != 200) || response.getContentAsString().isEmpty()) {
            return null;
        }
        return new JSONObject(response.getContentAsString());
    }

    private JSONObject createTodoSuccessful(JSONObject testTodo) throws Exception {
        return createTodo(testTodo,
            status().isCreated(),
            jsonPath("$.title").value(testTodo.getString("title")),
            jsonPath("$.description").value(testTodo.getString("description")),
            jsonPath("$.finished").value(testTodo.getBoolean("finished")),
            compareToDateWithRange("createdDate", LocalDate.now()),
            jsonPath("$.dueDate").value(testTodo.getString("dueDate")),
            checkFinishedDate(testTodo.getBoolean("finished")),
            jsonPath("$.assigneeList").isArray(),
            jsonPath("$.assigneeList.length()").value(testTodo.getJSONArray("assigneeIdList").length()),
            jsonPath("$.priority").value(testTodo.getString("priority")));
    }

    private boolean compareDatesWithTolerance(LocalDate date1, LocalDate date2) {
        return Math.max(date1.toEpochDay(), date2.toEpochDay()) - Math.min(date1.toEpochDay(), date2.toEpochDay()) <= 1;
    }

    private ResultMatcher compareToDateWithRange(String jsonPath, LocalDate toCompare) {
        return result -> {
            MockHttpServletResponse response = result.getResponse();
            JSONObject todo = new JSONObject(response.getContentAsString());
            Assertions.assertTrue(todo.has(jsonPath) && !todo.isNull(jsonPath) && !todo.getString(jsonPath).isBlank(), jsonPath + " is null or empty but it should not be!");
            LocalDate given = LocalDate.parse(todo.getString(jsonPath), DateTimeFormatter.ISO_LOCAL_DATE);
            Assertions.assertTrue(compareDatesWithTolerance(toCompare, given), jsonPath + " is not in the acceptable range!");
        };
    }

    private ResultMatcher checkFinishedDate(boolean shouldExist) {
        return result -> {
            if (shouldExist) {
                compareToDateWithRange("finishedDate", LocalDate.now()).match(result);
            } else {
                MockHttpServletResponse response = result.getResponse();
                JSONObject todo = new JSONObject(response.getContentAsString());
                Assertions.assertTrue(todo.isNull("finishedDate") || todo.getString("finishedDate").isBlank(), "finishedDate is not null or empty but it should be because finished == false!");
            }
        };
    }

    private ResultMatcher matchesTodo(JSONObject expected) throws Exception {
        List<ResultMatcher> matchers = new ArrayList<>();
        matchers.add(jsonPath("$.id").value(expected.getLong("id")));
        matchers.add(jsonPath("$.title").value(expected.getString("title")));
        matchers.add(jsonPath("$.description").value(expected.getString("description")));
        matchers.add(jsonPath("$.finished").value(expected.getBoolean("finished")));
        matchers.add(compareToDateWithRange("createdDate", LocalDate.parse(getCreatedDate(expected))));
        matchers.add(jsonPath("$.dueDate").value(expected.getString("dueDate")));
        matchers.add(checkFinishedDate(expected.getBoolean("finished")));
        matchers.add(jsonPath("$.assigneeList").isArray());
        if (expected.has("assigneeIdList")) {
            matchers.add(jsonPath("$.assigneeList.length()").value(expected.getJSONArray("assigneeIdList").length()));
        } else {
            matchers.add(jsonPath("$.assigneeList.length()").value(expected.getJSONArray("assigneeList").length()));
        }
        matchers.add(jsonPath("$.priority").value(expected.getString("priority")));

        return result -> {
            for (ResultMatcher m : matchers) {
                m.match(result);
            }
        };
    }
}
