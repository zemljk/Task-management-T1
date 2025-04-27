package main.services;

import main.DTO.TaskDTO;
import main.entities.Task;
import lombok.AllArgsConstructor;
import main.repositories.TaskRepository;

import java.util.List;

@AllArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;

    public Task create(TaskDTO taskDTO){
        Task task = Task.builder()
                .title(taskDTO.getTitle())
                .description(taskDTO.getDescription())
                .build();
        return taskRepository.save(task);
    }

    public List<Task> findAll(){
        return taskRepository.findAll();
    }



}
