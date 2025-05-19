package main.services;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import main.DTO.TaskDTO;
import main.DTO.TaskMapper;
import main.aspect.annotation.HandleExceptions;
import main.aspect.annotation.LogReturnValue;
import main.aspect.annotation.Loggable;
import main.aspect.annotation.TrackExecutionTime;
import main.entities.Task;
import main.repositories.TaskRepository;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.MailSender;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final NotificationService notificationService;

    public TaskService(TaskRepository taskRepository, TaskMapper taskMapper, NotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
        this.notificationService = notificationService;
    }

    @Loggable("создание задачи")
    @TrackExecutionTime
    public TaskDTO createTask(TaskDTO taskDTO) {
        Task taskToSave = taskMapper.toEntity(taskDTO);
        Task savedTask = taskRepository.save(taskToSave);
        return taskMapper.toDTO(savedTask);
    }

    @Loggable("удаление задачи")
    @TrackExecutionTime
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    @HandleExceptions
    @LogReturnValue
    public Optional<TaskDTO> findByIDTask(Long id) {
        return taskRepository.findById(id).map(taskMapper::toDTO);
    }

    @Loggable("нахождение всех задач")
    @TrackExecutionTime
    @HandleExceptions
    public List<TaskDTO> findAllTasks() {
        return taskRepository.findAll()
                .stream()
                .map(taskMapper::toDTO)
                .collect(Collectors.toList());
    }


    @Loggable("обновление задачи")
    @TrackExecutionTime
    @HandleExceptions
    public Optional<TaskDTO> updateTask(Long id, TaskDTO updatedTaskDTO) {
        return taskRepository.findById(id)
                .map(existingTask -> {
                    String oldStatus = existingTask.getStatus();
                    taskMapper.updateEntityFromDTO(updatedTaskDTO, existingTask);
                    Task savedTask = taskRepository.save(existingTask);
                    TaskDTO sevedTaskDTO = taskMapper.toDTO(savedTask);
                    String newStatus = sevedTaskDTO.getStatus();
                    notificationService.sendNotificationEmail(sevedTaskDTO.getId(),newStatus);
                    return sevedTaskDTO;
                });
    }
}