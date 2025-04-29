package main.services;


import main.aspect.annotation.HandleExceptions;
import main.aspect.annotation.LogReturnValue;
import main.aspect.annotation.Loggable;
import main.aspect.annotation.TrackExecutionTime;
import main.entities.Task;
import lombok.AllArgsConstructor;
import main.repositories.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;


    public TaskService (TaskRepository taskRepository){
    this.taskRepository = taskRepository;
    }

    @Loggable("создание задачи")
    @TrackExecutionTime
    public Task createTask(Task task){
       return taskRepository.save(task);
    }

    @Loggable("нахождение всех задач")
    @TrackExecutionTime
    @HandleExceptions
    public List<Task> findAllTasks(){
        return taskRepository.findAll();
    }

    @Loggable("удаление задачи")
    @TrackExecutionTime

    public void deleteTask(Long id){
        taskRepository.deleteById(id);
    }

    @Loggable("Поиск задачи по ID")
    @TrackExecutionTime
    @HandleExceptions
    @LogReturnValue
    public Optional<Task> findByIDTask(Long id){
        return  taskRepository.findById(id);
    }

    @Loggable("обновление задачи")
    @TrackExecutionTime
    @HandleExceptions
    public Optional<Task> updateByIdTask(Long id, Task updatingTask){
        return taskRepository.findById(id)
                .map(existingTask -> {
                    existingTask.setTitle(updatingTask.getTitle());
                    existingTask.setDescription(updatingTask.getDescription());
                    existingTask.setUserId(updatingTask.getUserId());
                    return taskRepository.save(existingTask);
                });
    }
}
