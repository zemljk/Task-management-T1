package controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import main.dto.TaskDTO;
import main.entities.Task;
import main.entities.TaskStatus;
import main.repositories.TaskRepository;
import main.services.NotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;


import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = main.App.class )
@AutoConfigureMockMvc
@Testcontainers
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    @MockitoBean
    private NotificationService notificationService;

    @Container
    public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void setDatasourceProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        dynamicPropertyRegistry.add("spring.datasource.username", postgresContainer::getUsername);
        dynamicPropertyRegistry.add("spring.datasource.password", postgresContainer::getPassword);
        dynamicPropertyRegistry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        dynamicPropertyRegistry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @BeforeEach
    void setUp() {

        doNothing().when(notificationService).sendNotificationEmail(anyLong(), anyString());
    }

    @AfterEach
    void tearDown() {
        taskRepository.deleteAll();
    }

    private Task createTask(String title, String description, TaskStatus status) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(status);
        return taskRepository.save(task);
    }

    @Test
    @DisplayName("POST /tasks - Должен создать новую задачу и вернуть статус 201 CREATED")
    void shouldCreateTaskAndReturnCreatedStatus() throws Exception {
        TaskDTO newTaskDTO = new TaskDTO();
        newTaskDTO.setTitle("New Task");
        newTaskDTO.setDescription("Description for new task");
        newTaskDTO.setStatus("TODO");

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTaskDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title", is("New Task")))
                .andExpect(jsonPath("$.description", is("Description for new task")))
                .andExpect(jsonPath("$.status", is("TODO")));
    }

    @Test
    @DisplayName("GET /tasks/{id} - Должен вернуть задачу по ID с 200 OK")
    void shouldReturnTaskById() throws Exception {
        Task existingTask = createTask("Existing Task", "Description", TaskStatus.IN_PROGRESS);

        mockMvc.perform(get("/tasks/{id}", existingTask.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(existingTask.getId())))
                .andExpect(jsonPath("$.title", is(existingTask.getTitle())))
                .andExpect(jsonPath("$.description", is(existingTask.getDescription())))
                .andExpect(jsonPath("$.status", is(existingTask.getStatus().name())));
    }

    @Test
    @DisplayName("GET /tasks/{id} - Должен вернуть 404 NOT FOUND, если задача не найдена")
    void shouldReturnNotFoundWhenTaskNotFound() throws Exception {
        mockMvc.perform(get("/tasks/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /tasks - Должен вернуть список всех задач с 200 OK")
    void shouldReturnAllTasks() throws Exception {
        createTask("Task 1", "Desc 1", TaskStatus.TODO);
        createTask("Task 2", "Desc 2", TaskStatus.DONE);

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Task 1")))
                .andExpect(jsonPath("$[1].title", is("Task 2")));
    }

    @Test
    @DisplayName("PUT /tasks/{id} - Должен обновить существующую задачу и вернуть 200 OK")
    void shouldUpdateExistingTask() throws Exception {
        Task existingTask = createTask("Task to Update", "Old Description", TaskStatus.TODO);

        TaskDTO updatedTaskDTO = new TaskDTO();
        updatedTaskDTO.setTitle("Updated Task Title");
        updatedTaskDTO.setDescription("New Description");
        updatedTaskDTO.setStatus("DONE");

        mockMvc.perform(put("/tasks/{id}", existingTask.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTaskDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(existingTask.getId())))
                .andExpect(jsonPath("$.title", is("Updated Task Title")))
                .andExpect(jsonPath("$.description", is("New Description")))
                .andExpect(jsonPath("$.status", is("DONE")));

        Task taskInDb = taskRepository.findById(existingTask.getId()).orElseThrow();
        assertEquals("Updated Task Title", taskInDb.getTitle());
        assertEquals(TaskStatus.DONE, taskInDb.getStatus());

        verify(notificationService, times(1)).sendNotificationEmail(taskInDb.getId(), "DONE");
    }

    @Test
    @DisplayName("PUT /tasks/{id} - Должен вернуть 404 NOT FOUND при попытке обновить несуществующую задачу")
    void shouldReturnNotFoundWhenUpdatingNonExistentTask() throws Exception {
        TaskDTO updatedTaskDTO = new TaskDTO();
        updatedTaskDTO.setTitle("Non Existent Task");
        updatedTaskDTO.setStatus("TODO");

        mockMvc.perform(put("/tasks/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTaskDTO)))
                .andExpect(status().isNotFound());
        verify(notificationService, times(0)).sendNotificationEmail(anyLong(), anyString());
    }

    @Test
    @DisplayName("DELETE /tasks/{id} - Должен удалить задачу и вернуть 204 NO CONTENT")
    void shouldDeleteTaskAndReturnNoContent() throws Exception {
        Task existingTask = createTask("Task to Delete", "Description", TaskStatus.TODO);

        mockMvc.perform(delete("/tasks/{id}", existingTask.getId()))
                .andExpect(status().isNoContent());

        List<Task> remainingTasks = taskRepository.findAll();
        assertTrue(remainingTasks.isEmpty());
    }

    @Test
    @DisplayName("DELETE /tasks/{id} - Должен вернуть 204 NO CONTENT, даже если задача не найдена (идемпотентность)")
    void shouldReturnNoContentEvenIfTaskNotFoundOnDelete() throws Exception {
        mockMvc.perform(delete("/tasks/{id}", 999L))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /tasks - Должен вернуть 400 BAD REQUEST для невалидных входных данных (например, отсутствующее поле)")
    void shouldReturnBadRequestForInvalidInputOnCreate() throws Exception {
        String invalidJson = "{ \"description\": \"Invalid task\", \"status\": \"TODO\" }";

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /tasks/{id} - Должен вернуть 400 BAD REQUEST для невалидных входных данных (например, неверный статус)")
    void shouldReturnBadRequestForInvalidStatusOnUpdate() throws Exception {
        Task existingTask = createTask("Valid Task", "Description", TaskStatus.TODO);

        TaskDTO updatedTaskDTO = new TaskDTO();
        updatedTaskDTO.setTitle("Updated Task");
        updatedTaskDTO.setDescription("Description");
        updatedTaskDTO.setStatus("INVALID_STATUS");

        mockMvc.perform(put("/tasks/{id}", existingTask.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTaskDTO)))
                .andExpect(status().isBadRequest());
    }
}