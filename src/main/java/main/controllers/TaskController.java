package main.controllers;



import main.dto.TaskDTO;
import main.dto.TaskMapper;
import main.services.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;
    private final TaskMapper taskMapper;

    public TaskController(TaskService taskService, TaskMapper taskMapper) {
        this.taskService = taskService;
        this.taskMapper = taskMapper;
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDTO createTask(@RequestBody TaskDTO taskDTO) {
        return taskService.createTask(taskDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable("id") Long id) {
        taskService.deleteTask(id);
    }

    @GetMapping("/{id}")
    public TaskDTO getTaskById(@PathVariable("id") Long id) {
       return taskService.findByIDTask(id);
    }

    @GetMapping()
    public List<TaskDTO> getTasks() {
        return taskService.findAllTasks();
    }

    @PutMapping("/{id}")
    public TaskDTO updateTask(@PathVariable("id") Long id, @RequestBody TaskDTO updatedTaskDTO){
        return taskService.updateTask(id,updatedTaskDTO);
    }

}
