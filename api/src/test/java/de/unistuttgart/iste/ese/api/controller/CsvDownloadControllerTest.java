package de.unistuttgart.iste.ese.api.controller;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static de.unistuttgart.iste.ese.api.controller.TestUtil.setAssigneeIdList;
import static de.unistuttgart.iste.ese.api.controller.TestUtil.testAssigneeReq;
import static de.unistuttgart.iste.ese.api.controller.TestUtil.testTodoReq;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class CsvDownloadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("download CSV with no todos returns header row only (200)")
    void downloadCsvEmpty() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/csv-downloads/todos"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "text/csv; charset=UTF-8"))
            .andExpect(header().string("Content-Disposition", "attachment; filename=\"todos.csv\""))
            .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content).contains("id,title,description,finished,assignees,createdDate,dueDate,finishedDate,category,priority");
        String[] lines = content.trim().split("\\r?\\n");
        assertThat(lines).hasSize(1);
    }

    @Test
    @DisplayName("download CSV with existing todos returns header and formatted records (200)")
    void downloadCsvWithTodos() throws Exception {
        // Create an assignee
        JSONObject assignee = testAssigneeReq();
        MvcResult assigneeResult = mockMvc.perform(post("/api/v1/assignees")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(assignee.toString()))
            .andExpect(status().isCreated())
            .andReturn();
        long assigneeId = new JSONObject(assigneeResult.getResponse().getContentAsString()).getLong("id");

        // Create a todo with the assignee
        JSONObject todo = testTodoReq();
        JSONArray assigneeIds = new JSONArray();
        assigneeIds.put(assigneeId);
        setAssigneeIdList(todo, assigneeIds);

        mockMvc.perform(post("/api/v1/todos")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(todo.toString()))
            .andExpect(status().isCreated());

        MvcResult csvResult = mockMvc.perform(get("/api/v1/csv-downloads/todos"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "text/csv; charset=UTF-8"))
            .andExpect(header().string("Content-Disposition", "attachment; filename=\"todos.csv\""))
            .andReturn();

        String content = csvResult.getResponse().getContentAsString();
        String[] lines = content.trim().split("\\r?\\n");
        assertThat(lines).hasSize(2);
        assertThat(lines[0]).isEqualTo("id,title,description,finished,assignees,createdDate,dueDate,finishedDate,category,priority");
        assertThat(lines[1]).contains(todo.getString("title"));
    }
}
