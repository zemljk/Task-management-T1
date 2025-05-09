package main.services;


import main.DTO.TaskDTO;
import main.DTO.TaskMapper;
import main.aspect.annotation.HandleExceptions;
import main.aspect.annotation.LogReturnValue;
import main.aspect.annotation.Loggable;
import main.aspect.annotation.TrackExecutionTime;
import main.entities.Task;
import main.repositories.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    public TaskService(TaskRepository taskRepository, TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
    }

    @Loggable("создание задачи")
    @TrackExecutionTime
    public TaskDTO createTask(TaskDTO taskDTO) {
        Task taskToSave = taskMapper.toEntity(taskDTO);
        Task sevedTask = taskRepository.save(taskToSave);
        return taskMapper.toDTO(sevedTask);
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
                    taskMapper.updateEntityFromDTO(updatedTaskDTO, existingTask);
                    Task savedTask = taskRepository.save(existingTask);
                    return taskMapper.toDTO(savedTask);
                });
    }
}
