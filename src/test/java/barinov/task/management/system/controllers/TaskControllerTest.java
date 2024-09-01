package barinov.task.management.system.controllers;

import barinov.task.management.system.dto.CommentDTO;
import barinov.task.management.system.dto.TaskDTO;
import barinov.task.management.system.models.Person;
import barinov.task.management.system.models.Priority;
import barinov.task.management.system.models.Status;
import barinov.task.management.system.repositories.PeopleRepository;
import barinov.task.management.system.security.PersonDetails;
import barinov.task.management.system.services.RegistrationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
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
    public static void createPersonDetails(@Autowired RegistrationService registrationService) {
        Person person = new Person();

        person.setEmail("testTaskController@gmail.com");
        person.setPassword("password");

        registrationService.register(person);

        personDetails = new PersonDetails(person);
    }

    @AfterAll
    public static void deletePersonDetails(@Autowired PeopleRepository peopleRepository) {
        peopleRepository.delete(personDetails.getPerson());
    }

    @Test
    @DirtiesContext
    public void createTaskTest() throws Exception {
        TaskDTO taskDTO = new TaskDTO("Test task 1", Status.WAITING.toString(), Priority.LOW.toString(), 0);

        mockMvc.perform(post("/task")
                        .with(user(personDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.taskId").exists())
                .andReturn();
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "testTaskController2@gmail.com")
    public void editTaskTest() throws Exception {
        TaskDTO taskDTO = new TaskDTO("Test task 1", Status.WAITING.toString(), Priority.LOW.toString(), 0);

        MvcResult mvcResult = mockMvc.perform(post("/task")
                        .with(user(personDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.taskId").exists())
                .andReturn();

        int taskId = objectMapper.readTree(mvcResult.getResponse().getContentAsString())
                .get("taskId")
                .asInt();

        mockMvc.perform(patch("/task/{id}", taskId)
                        .with(user(personDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[{ \"op\": \"replace\", \"path\": \"/description\", \"value\": \"Изменение\" }]"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/task/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Изменение"));
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "testTaskController2@gmail.com")
    public void editTaskByNotAuthorTest() throws Exception {
        TaskDTO taskDTO = new TaskDTO("Test task 1", Status.WAITING.toString(), Priority.LOW.toString(), 0);

        MvcResult mvcResult = mockMvc.perform(post("/task")
                        .with(user(personDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.taskId").exists())
                .andReturn();

        int taskId = objectMapper.readTree(mvcResult.getResponse().getContentAsString())
                .get("taskId")
                .asInt();

        mockMvc.perform(patch("/task/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[{ \"op\": \"replace\", \"path\": \"/description\", \"value\": \"Изменение\" }]"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "testTaskController2@gmail.com")
    public void showTaskTest() throws Exception {
        TaskDTO taskDTO = new TaskDTO("Test task 1", Status.WAITING.toString(), Priority.LOW.toString(), 0);

        MvcResult mvcResult = mockMvc.perform(post("/task")
                        .with(user(personDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.taskId").exists())
                .andReturn();

        int taskId = objectMapper.readTree(mvcResult.getResponse().getContentAsString())
                .get("taskId")
                .asInt();

        mockMvc.perform(get("/task/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId));
    }

    @Test
    @DirtiesContext
    public void deleteTaskTest() throws Exception {
        TaskDTO taskDTO = new TaskDTO("Test task 1", Status.WAITING.toString(), Priority.LOW.toString(), 0);

        MvcResult mvcResult = mockMvc.perform(post("/task")
                        .with(user(personDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.taskId").exists())
                .andReturn();

        int taskId = objectMapper.readTree(mvcResult.getResponse().getContentAsString())
                .get("taskId")
                .asInt();

        mockMvc.perform(delete("/task/{id}", taskId)
                        .with(user(personDetails)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "testTaskController2@gmail.com")
    public void deleteTaskByNotAuthorTest() throws Exception {
        TaskDTO taskDTO = new TaskDTO("Test task 1", Status.WAITING.toString(), Priority.LOW.toString(), 0);

        MvcResult mvcResult = mockMvc.perform(post("/task")
                        .with(user(personDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.taskId").exists())
                .andReturn();

        int taskId = objectMapper.readTree(mvcResult.getResponse().getContentAsString())
                .get("taskId")
                .asInt();

        mockMvc.perform(delete("/task/{id}", taskId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "testTaskController2@gmail.com")
    public void changeStatusTest() throws Exception {
        TaskDTO taskDTO = new TaskDTO("Test task 1", Status.WAITING.toString(), Priority.LOW.toString(), 0);

        MvcResult mvcResult = mockMvc.perform(post("/task")
                        .with(user(personDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.taskId").exists())
                .andReturn();

        int taskId = objectMapper.readTree(mvcResult.getResponse().getContentAsString())
                .get("taskId")
                .asInt();

        mockMvc.perform(patch("/task/status/{id}", taskId)
                        .with(user(personDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(Status.PROGRESS.toString()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/task/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(Status.PROGRESS.toString()));
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "testTaskController2@gmail.com")
    public void changeStatusByNotAuthorTest() throws Exception {
        TaskDTO taskDTO = new TaskDTO("Test task 1", Status.WAITING.toString(), Priority.LOW.toString(), 0);

        MvcResult mvcResult = mockMvc.perform(post("/task")
                        .with(user(personDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.taskId").exists())
                .andReturn();

        int taskId = objectMapper.readTree(mvcResult.getResponse().getContentAsString())
                .get("taskId")
                .asInt();

        mockMvc.perform(patch("/task/status/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(Status.PROGRESS.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "testTaskController2@gmail.com")
    public void addCommentTest() throws Exception {
        TaskDTO taskDTO = new TaskDTO("Test task 1", Status.WAITING.toString(), Priority.LOW.toString(), 0);

        MvcResult mvcResult = mockMvc.perform(post("/task")
                        .with(user(personDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.taskId").exists())
                .andReturn();

        int taskId = objectMapper.readTree(mvcResult.getResponse().getContentAsString())
                .get("taskId")
                .asInt();

        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setText("Комментарий");

        mockMvc.perform(post("/task/comment/{id}", taskId)
                        .with(user(personDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDTO)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/task/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments[:1].text").value("Комментарий"));
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "testTaskController2@gmail.com")
    public void getTaskByFiltersTest() throws Exception {
        TaskDTO taskDTO = new TaskDTO("Test task 1", Status.WAITING.toString(), Priority.LOW.toString(), 0);

        mockMvc.perform(post("/task")
                        .with(user(personDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.taskId").exists())
                .andReturn();

        mockMvc.perform(get("/task?authorId={authorId}&priority={priority}&status={status}",
                        personDetails.getPerson().getId(), taskDTO.getPriority(), taskDTO.getStatus()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[:1].id").exists());
    }

    @Test
    @DirtiesContext
    public void unauthorizedAccessTest() throws Exception {
        mockMvc.perform(get("/task"))
                .andExpect(status().is3xxRedirection());
    }
}
