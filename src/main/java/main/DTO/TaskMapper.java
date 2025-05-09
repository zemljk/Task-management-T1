package main.DTO;

import main.entities.Task;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TaskMapper {
    TaskDTO toDTO(Task task);
    Task toEntity(TaskDTO taskDTO);
    void updateEntityFromDTO(TaskDTO taskDTO,@MappingTarget Task task);
}
