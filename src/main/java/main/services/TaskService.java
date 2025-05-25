package main.services;


import main.dto.TaskDTO;
import main.dto.TaskMapper;
import main.aspect.annotation.HandleExceptions;
import main.aspect.annotation.LogReturnValue;
import main.aspect.annotation.Loggable;
import main.aspect.annotation.TrackExecutionTime;
import main.entities.Task;
import main.entities.TaskStatus;
import main.kafka.TaskStatusProducer;
import main.repositories.TaskRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final NotificationService notificationService;
    private final TaskStatusProducer taskStatusProducer;

    public TaskService(TaskRepository taskRepository, TaskMapper taskMapper, NotificationService notificationService, TaskStatusProducer taskStatusProducer) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
        this.notificationService = notificationService;
        this.taskStatusProducer = taskStatusProducer;
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
    public TaskDTO findByIDTask(Long id) {
        return taskRepository
                .findById(id)
                .map(taskMapper::toDTO)
                .orElseThrow(() -> {
                    return new ResponseStatusException(HttpStatus.NOT_FOUND);
                });
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
    public TaskDTO updateTask(Long id, TaskDTO updatedTaskDTO) {
        return taskRepository.findById(id)
                .map(existingTask -> {
                    TaskStatus oldStatus = existingTask.getStatus();
                    taskMapper.updateEntityFromDTO(updatedTaskDTO, existingTask);
                    Task savedTask = taskRepository.save(existingTask);
                    TaskDTO sevedTaskDTO = taskMapper.toDTO(savedTask);
                    String newStatus = sevedTaskDTO.getStatus();
                    if (!newStatus.equals(oldStatus.name())) {
                        taskStatusProducer.sendTaskStatusUpdate(sevedTaskDTO.getId(), newStatus);
                    }
                    return sevedTaskDTO;
                }).orElseThrow(() -> {
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Задача с ID " + id + " не найдена для обновления");
                });
    }
}