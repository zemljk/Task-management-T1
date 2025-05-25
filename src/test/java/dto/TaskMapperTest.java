package dto;

import main.dto.TaskDTO;
import main.dto.TaskMapper;
import main.entities.Task;
import main.entities.TaskStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class TaskMapperTest {

    private final TaskMapper mapper = Mappers.getMapper(TaskMapper.class);

    @Test
    @DisplayName("Должен корректно преобразовать Task в TaskDTO")
    void shouldMapTaskToTaskDTO() {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setDescription("Description of test task.");
        task.setStatus(TaskStatus.TODO);

        TaskDTO dto = mapper.toDTO(task);

        assertNotNull(dto);
        assertEquals(task.getId(), dto.getId());
        assertEquals(task.getTitle(), dto.getTitle());
        assertEquals(task.getDescription(), dto.getDescription());
        assertEquals(task.getStatus().name(), dto.getStatus());
    }

    @Test
    @DisplayName("Должен корректно преобразовать TaskDTO в Task")
    void shouldMapTaskDTOToTask() {
        TaskDTO dto = new TaskDTO();
        dto.setId(1L);
        dto.setTitle("DTO Task");
        dto.setDescription("Description from DTO.");
        dto.setStatus("IN_PROGRESS");

        Task task = mapper.toEntity(dto);

        assertNotNull(task);
        assertNull(task.getId());
        assertEquals(dto.getTitle(), task.getTitle());
        assertEquals(dto.getDescription(), task.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
    }

    @Test
    @DisplayName("Должен корректно обновить существующую сущность Task из TaskDTO")
    void shouldUpdateTaskEntityFromDTO() {
        Task existingTask = new Task();
        existingTask.setId(10L);
        existingTask.setTitle("Old Title");
        existingTask.setDescription("Old Description");
        existingTask.setStatus(TaskStatus.TODO);

        TaskDTO updateDTO = new TaskDTO();
        updateDTO.setTitle("New Title");
        updateDTO.setDescription("New Description");
        updateDTO.setStatus("DONE");
        updateDTO.setId(99L);

        mapper.updateEntityFromDTO(updateDTO, existingTask);

        assertEquals(10L, existingTask.getId());
        assertEquals("New Title", existingTask.getTitle());
        assertEquals("New Description", existingTask.getDescription());
        assertEquals(TaskStatus.DONE, existingTask.getStatus());
    }

    @Test
    @DisplayName("Должен обработать null значения в TaskDTO при обновлении")
    void shouldHandleNullValuesInDTOWhenUpdating() {
        Task existingTask = new Task();
        existingTask.setId(1L);
        existingTask.setTitle("Initial Title");
        existingTask.setDescription("Initial Description");
        existingTask.setStatus(TaskStatus.TODO);

        TaskDTO updateDTO = new TaskDTO();
        updateDTO.setStatus("IN_PROGRESS");

        mapper.updateEntityFromDTO(updateDTO, existingTask);

        assertEquals(1L, existingTask.getId());
        assertEquals("Initial Title", existingTask.getTitle());
        assertEquals("Initial Description", existingTask.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, existingTask.getStatus());
    }

    @Test
    @DisplayName("Должен выбросить исключение при неверном статусе в TaskDTO")
    void shouldThrowIllegalArgumentExceptionForInvalidStatusInDTO() {
        TaskDTO dto = new TaskDTO();
        dto.setTitle("Bad Status Task");
        dto.setStatus("INVALID_STATUS");

        assertThrows(IllegalArgumentException.class, () -> mapper.toEntity(dto));

        Task existingTask = new Task();
        existingTask.setId(1L);
        existingTask.setTitle("Old Title");
        existingTask.setStatus(TaskStatus.TODO);

        TaskDTO updateDTO = new TaskDTO();
        updateDTO.setStatus("NON_EXISTENT");

        assertThrows(IllegalArgumentException.class, () -> mapper.updateEntityFromDTO(updateDTO, existingTask));
    }
}