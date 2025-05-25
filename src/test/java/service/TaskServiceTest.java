package service;

import main.dto.TaskDTO;
import main.dto.TaskMapper;
import main.entities.Task;
import main.entities.TaskStatus;
import main.kafka.TaskStatusProducer;
import main.repositories.TaskRepository;
import main.services.NotificationService;
import main.services.TaskService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private NotificationService notificationService;

    @Mock
    private TaskStatusProducer taskStatusProducer;

    @InjectMocks
    private TaskService taskService;

    private Task createTestTask(Long id, String title, String description, TaskStatus status) {
        Task task = new Task();
        task.setId(id);
        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(status);
        return task;
    }

    private TaskDTO createTestTaskDTO(Long id, String title, String description, String status) {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setId(id);
        taskDTO.setTitle(title);
        taskDTO.setDescription(description);
        taskDTO.setStatus(status);
        return taskDTO;
    }

    @Test
    @DisplayName("Должен успешно создать задачу")
    void shouldCreateTaskSuccessfully() {
        TaskDTO inputTaskDTO = createTestTaskDTO(null, "Test Task", "Description", "TODO");
        Task taskToSave = createTestTask(null, "Test Task", "Description", TaskStatus.TODO);
        Task savedTask = createTestTask(1L, "Test Task", "Description", TaskStatus.TODO);
        TaskDTO expectedTaskDTO = createTestTaskDTO(1L, "Test Task", "Description", "TODO");

        when(taskMapper.toEntity(inputTaskDTO)).thenReturn(taskToSave);
        when(taskRepository.save(taskToSave)).thenReturn(savedTask);
        when(taskMapper.toDTO(savedTask)).thenReturn(expectedTaskDTO);

        TaskDTO actualTaskDTO = taskService.createTask(inputTaskDTO);

        assertNotNull(actualTaskDTO.getId());
        assertEquals(expectedTaskDTO, actualTaskDTO);
        verify(taskMapper, times(1)).toEntity(inputTaskDTO);
        verify(taskRepository, times(1)).save(taskToSave);
        verify(taskMapper, times(1)).toDTO(savedTask);
    }

    @Test
    @DisplayName("Должен успешно удалить задачу по ID")
    void shouldDeleteTaskByIdSuccessfully() {
        Long taskId = 1L;
        doNothing().when(taskRepository).deleteById(taskId);

        taskService.deleteTask(taskId);

        verify(taskRepository, times(1)).deleteById(taskId);
    }

    @Test
    @DisplayName("Должен найти задачу по ID")
    void shouldFindTaskByIdSuccessfully() {
        Long taskId = 1L;
        Task foundTask = createTestTask(taskId, "Found Task", "Description", TaskStatus.TODO);
        TaskDTO expectedTaskDTO = createTestTaskDTO(taskId, "Found Task", "Description", "TODO");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(foundTask));
        when(taskMapper.toDTO(foundTask)).thenReturn(expectedTaskDTO);

        TaskDTO actualTaskDTO = taskService.findByIDTask(taskId);

        assertEquals(expectedTaskDTO, actualTaskDTO);
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskMapper, times(1)).toDTO(foundTask);
    }

    @Test
    @DisplayName("Должен выбросить исключение, если задача не найдена по ID")
    void shouldThrowExceptionWhenTaskNotFoundById() {
        Long taskId = 99L;

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> taskService.findByIDTask(taskId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskMapper, never()).toDTO(any(Task.class));
    }

    @Test
    @DisplayName("Должен найти все задачи")
    void shouldFindAllTasksSuccessfully() {
        Task task1 = createTestTask(1L, "Task 1", "Desc 1", TaskStatus.TODO);
        Task task2 = createTestTask(2L, "Task 2", "Desc 2", TaskStatus.IN_PROGRESS);
        List<Task> tasks = Arrays.asList(task1, task2);

        TaskDTO dto1 = createTestTaskDTO(1L, "Task 1", "Desc 1", "TODO");
        TaskDTO dto2 = createTestTaskDTO(2L, "Task 2", "Desc 2", "IN_PROGRESS");
        List<TaskDTO> expectedDTOs = Arrays.asList(dto1, dto2);

        when(taskRepository.findAll()).thenReturn(tasks);
        when(taskMapper.toDTO(task1)).thenReturn(dto1);
        when(taskMapper.toDTO(task2)).thenReturn(dto2);

        List<TaskDTO> actualDTOs = taskService.findAllTasks();

        assertEquals(expectedDTOs.size(), actualDTOs.size());
        assertEquals(expectedDTOs, actualDTOs);
        verify(taskRepository, times(1)).findAll();
        verify(taskMapper, times(1)).toDTO(task1);
        verify(taskMapper, times(1)).toDTO(task2);
    }

    @Test
    @DisplayName("Должен успешно обновить задачу без изменения статуса")
    void shouldUpdateTaskSuccessfullyWithoutStatusChange() {
        Long taskId = 1L;
        TaskDTO updatedTaskDTO = createTestTaskDTO(taskId, "Updated Task", "Updated Description", "TODO");
        Task existingTask = createTestTask(taskId, "Original Task", "Original Description", TaskStatus.TODO);
        Task savedTask = createTestTask(taskId, "Updated Task", "Updated Description", TaskStatus.TODO);
        TaskDTO expectedTaskDTO = createTestTaskDTO(taskId, "Updated Task", "Updated Description", "TODO");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        doNothing().when(taskMapper).updateEntityFromDTO(updatedTaskDTO, existingTask);
        when(taskRepository.save(existingTask)).thenReturn(savedTask);
        when(taskMapper.toDTO(savedTask)).thenReturn(expectedTaskDTO);

        TaskDTO result = taskService.updateTask(taskId, updatedTaskDTO);

        assertEquals(expectedTaskDTO, result);
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskMapper, times(1)).updateEntityFromDTO(updatedTaskDTO, existingTask);
        verify(taskRepository, times(1)).save(existingTask);
        verify(taskMapper, times(1)).toDTO(savedTask);
        verify(taskStatusProducer, never()).sendTaskStatusUpdate(anyLong(), anyString());
    }

    @Test
    @DisplayName("Должен успешно обновить задачу и отправить уведомление об изменении статуса")
    void shouldUpdateTaskSuccessfullyAndSendStatusUpdate() {
        Long taskId = 1L;
        TaskDTO updatedTaskDTO = createTestTaskDTO(taskId, "Updated Task", "Updated Description", "DONE");
        Task existingTask = createTestTask(taskId, "Original Task", "Original Description", TaskStatus.TODO);
        Task savedTask = createTestTask(taskId, "Updated Task", "Updated Description", TaskStatus.DONE);
        TaskDTO expectedTaskDTO = createTestTaskDTO(taskId, "Updated Task", "Updated Description", "DONE");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        doAnswer(invocation -> {
            TaskDTO dto = invocation.getArgument(0);
            Task entity = invocation.getArgument(1);
            entity.setTitle(dto.getTitle());
            entity.setDescription(dto.getDescription());
            entity.setStatus(TaskStatus.valueOf(dto.getStatus()));
            return null;
        }).when(taskMapper).updateEntityFromDTO(updatedTaskDTO, existingTask);
        when(taskRepository.save(existingTask)).thenReturn(savedTask);
        when(taskMapper.toDTO(savedTask)).thenReturn(expectedTaskDTO);
        doNothing().when(taskStatusProducer).sendTaskStatusUpdate(taskId, "DONE");

        TaskDTO result = taskService.updateTask(taskId, updatedTaskDTO);

        assertEquals(expectedTaskDTO, result);
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskMapper, times(1)).updateEntityFromDTO(updatedTaskDTO, existingTask);
        verify(taskRepository, times(1)).save(existingTask);
        verify(taskMapper, times(1)).toDTO(savedTask);
        verify(taskStatusProducer, times(1)).sendTaskStatusUpdate(taskId, "DONE");
    }


    @Test
    @DisplayName("Должен выбросить исключение при обновлении, если задача не найдена")
    void shouldThrowExceptionWhenUpdateTaskNotFound() {
        Long taskId = 99L;
        TaskDTO updatedTaskDTO = createTestTaskDTO(taskId, "Updated Task", "Updated Description", "DONE");

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> taskService.updateTask(taskId, updatedTaskDTO));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Задача с ID " + taskId + " не найдена для обновления", exception.getReason());
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskMapper, never()).updateEntityFromDTO(any(TaskDTO.class), any(Task.class));
        verify(taskRepository, never()).save(any(Task.class));
        verify(taskMapper, never()).toDTO(any(Task.class));
        verify(taskStatusProducer, never()).sendTaskStatusUpdate(anyLong(), anyString());
    }
}