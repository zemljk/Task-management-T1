package main.controllers;

import lombok.Getter;
import main.entities.Task;
import main.services.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping()
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        Task createdTask = taskService.createTask(task);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable("id") Long id) {
        taskService.deleteTask(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable("id") Long id) {
       Optional<Task> taskOptional = taskService.findByIDTask(id);
       return taskOptional.map(task -> new ResponseEntity<>(task,HttpStatus.OK))
               .orElse(new ResponseEntity<>(HttpStatus.NO_CONTENT));
    }

    @GetMapping()
    public ResponseEntity<List<Task>> getTasks() {
        List<Task> tasks = taskService.findAllTasks();
        return new ResponseEntity<>(tasks,HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Optional<Task>> updateTask(@PathVariable("id") Long id, @RequestBody Task updatedTask){
        Optional<Task> taskOptional = taskService.updateByIdTask(id,updatedTask);
        return taskOptional.map(task -> new ResponseEntity<>(taskOptional,HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NO_CONTENT));
    }

}
