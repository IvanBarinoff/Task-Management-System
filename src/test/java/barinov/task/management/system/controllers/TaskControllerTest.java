package barinov.task.management.system.controllers;

import barinov.task.management.system.dto.TaskDTO;
import barinov.task.management.system.models.Person;
import barinov.task.management.system.models.Priority;
import barinov.task.management.system.models.Status;
import barinov.task.management.system.security.PersonDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static PersonDetails personDetails;

    @BeforeAll
    public static void createPersonDetails() {
        Person person = new Person();

        person.setId(1);
        person.setEmail("test2@gmail.com");
        person.setPassword("password");

        personDetails = new PersonDetails(person);
    }

    @Test
    @DirtiesContext
    public void createTaskTest() throws Exception {
        TaskDTO taskDTO = new TaskDTO("Test task 1", Status.WAITING.toString(), Priority.LOW.toString(), 0);

        mockMvc.perform(post("/task")
                        .with(user(personDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").exists())
                .andReturn();


    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "test1@gmail.com")
    public void showTaskTest() throws Exception {
        TaskDTO taskDTO = new TaskDTO("Test task 1", Status.WAITING.toString(), Priority.LOW.toString(), 0);

        MvcResult mvcResult = mockMvc.perform(post("/task")
                        .with(user(personDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").exists())
                .andReturn();

        int taskId = objectMapper.readTree(mvcResult.getResponse().getContentAsString())
                .get("taskId")
                .asInt();

        mockMvc.perform(get("/task/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId));
    }
}
