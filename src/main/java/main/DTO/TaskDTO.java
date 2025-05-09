package main.DTO;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskDTO {
    private long id;
    private String title;
    private String description;
    private int userId;
}
