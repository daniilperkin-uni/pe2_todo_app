package de.unistuttgart.iste.ese.api.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.hamcrest.Matchers;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import static de.unistuttgart.iste.ese.api.controller.TestUtil.setAssigneeIdList;
import static de.unistuttgart.iste.ese.api.controller.TestUtil.setFinished;
import static de.unistuttgart.iste.ese.api.controller.TestUtil.setTitle;
import static de.unistuttgart.iste.ese.api.controller.TestUtil.testAssigneeReq;
import static de.unistuttgart.iste.ese.api.controller.TestUtil.testTodoReq;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class AdditionalEx4Test {

    @Autowired private MockMvc mockMvc;

    private List<JSONObject> assigneeList = new ArrayList<>();
    private JSONObject testTodo;
    @BeforeEach
    public void setUp() throws Exception {
        for (int i = 0; i < 2; i++) {
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
    @DisplayName("EX4 create a valid todo (201)")
    public void createValidTodoEx4() throws Exception {
        createTodoSuccessful(testTodo);
    }

    @Test
    @DisplayName("creating a todo yields the correct ML category")
    public void createTodoWithCorrectCategory() throws Exception {
        setTitle(testTodo, "set deadline");
        assertEquals("work", createTodoSuccessful(testTodo).getString("category"), "Category should be 'work' because of the title");
    }

    @Test
    @DisplayName("editing a todo yields the correct ML category")
    public void editTodoWithCorrectCategory() throws Exception {
        setTitle(testTodo, "set deadline");
        JSONObject todoJson = createTodoSuccessful(testTodo);

        setTitle(testTodo, "Weltherrschaft");
        assertEquals("private", createTodoSuccessful(testTodo).getString("category"), "Category should be 'private' because of the title");
    }

    @Test
    @DisplayName("CSV export for todos is valid (200, application/csv or text/csv)")
    public void exportCSV() throws Exception {
        JSONObject todo1 = createTodoSuccessful(testTodo);
        JSONObject todo2 = testTodoReq();
        setFinished(todo2, true);
        todo2 = createTodoSuccessful(todo2);

        MvcResult result = mockMvc.perform(get("/api/v1/csv-downloads/todos"))
                                  .andExpect(status().isOk())
                                  .andExpect(result1 -> {
                                      String contentType = result1.getResponse().getContentType();
                                      MediaType mediaType = MediaType.parseMediaType(contentType);
                                      assertTrue(
                                          mediaType.isCompatibleWith(MediaType.valueOf("text/csv"))
                                              || mediaType.isCompatibleWith(MediaType.valueOf("application/csv")),
                                          "Content-Type was not compatible with text/csv or application/csv but was " + contentType
                                      );
                                  })
                                  .andReturn();

        CSVFormat fmt = CSVFormat.RFC4180
            .builder()
            .setSkipHeaderRecord(false)
            .setHeader().get();

        try (CSVParser parser = CSVParser.parse(result.getResponse().getContentAsString(), fmt)) {
            String[] headers = parser.getHeaderNames().toArray(new String[0]);
            String[] expectedHeaders = {
                "id", "title", "description", "finished", "assignees",
                "createdDate", "dueDate", "finishedDate", "category", "priority"
            };

            Arrays.sort(headers);
            Arrays.sort(expectedHeaders);
            assertArrayEquals(expectedHeaders, headers);

            List<CSVRecord> rows = parser.getRecords();
            assertEquals(2, rows.size());

            Map<String, CSVRecord> byId = rows.stream()
                                              .collect(Collectors.toMap(r -> r.get("id"), Function.identity()));

            String id1 = String.valueOf(todo1.getLong("id"));
            String id2 = String.valueOf(todo2.getLong("id"));
            assertTrue(byId.containsKey(id1));
            assertTrue(byId.containsKey(id2));

            CSVRecord r1 = byId.get(id1);
            assertEquals(todo1.getString("title"), r1.get("title"), "Title does not match for todo 1");
            assertEquals(todo1.getString("description"), r1.get("description"), "Description does not match for todo 1");
            assertEquals(String.valueOf(todo1.getBoolean("finished")), r1.get("finished"), "Finished does not match for todo 1");
            try {
                assertEquals(getAssigneeString(assigneeList), r1.get("assignees"), "Assignees do not match for todo 1");
            } catch (AssertionError e) {
                assertEquals(getAssigneeString(assigneeList.reversed()), r1.get("assignees"), "Assignees do not match for todo 1");
            }
            assertEquals(todo1.getString("createdDate"), r1.get("createdDate"), "Created date does not match for todo 1");
            assertEquals(todo1.getString("dueDate"), r1.get("dueDate"), "Due date does not match for todo 1");
            assertEquals("", r1.get("finishedDate"), "Finished date does not match for todo 1");
            assertEquals(todo1.getString("category"), r1.get("category"), "Category does not match for todo 1");
            assertEquals(todo1.getString("priority"), r1.get("priority"), "Priority does not match for todo 1");
            assertEquals("false", r1.get("finished").toLowerCase(), "Finished does not match for todo 1");

            CSVRecord r2 = byId.get(id2);
            assertEquals(todo2.getString("title"), r2.get("title"), "title does not match for todo 2");
            assertEquals(todo2.getString("description"), r2.get("description"), "description does not match for todo 2");
            assertEquals(String.valueOf(todo2.getBoolean("finished")), r2.get("finished"), "finished does not match for todo 2");
            assertEquals("", r2.get("assignees"), "assignees do not match for todo 2");
            assertEquals(todo2.getString("createdDate"), r2.get("createdDate"), "createdDate does not match for todo 2");
            assertEquals(todo2.getString("dueDate"), r2.get("dueDate"), "dueDate does not match for todo 2");
            assertEquals(todo2.getString("finishedDate"), r2.get("finishedDate"), "finishedDate does not match for todo 2");
            assertEquals(todo2.getString("category"), r2.get("category"), "category does not match for todo 2");
            assertEquals(todo2.getString("priority"), r2.get("priority"), "priority does not match for todo 2");
            assertEquals("true", r2.get("finished").toLowerCase(), "finished does not match for todo 2");
        } catch (Exception e) {
            Assertions.fail("Parsing CSV failed: " + e.getMessage());
        }
    }

    private String getAssigneeString(List<JSONObject> assignees) {
        return String.join("+", assignees.stream().map(assignee -> assignee.optString("prename") + " " + assignee.optString("name")).toList());
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
            jsonPath("$.priority").value(testTodo.getString("priority")),
            jsonPath("$.category").value(Matchers.either(Matchers.is("work")).or(Matchers.is("private"))));
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
}
