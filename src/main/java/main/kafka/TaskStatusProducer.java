package main.kafka;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.DTO.TaskStatusUpdate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskStatusProducer {

    private final KafkaTemplate<Long, TaskStatusUpdate> kafkaTemplate;

    @Value("${kafka.topic.task-status-updates}")
    private String topicName;

    public void sendTaskStatusUpdate(Long taskId, String newStatus) {
        TaskStatusUpdate update = new TaskStatusUpdate(taskId, newStatus);
        log.info("Sending task status update to topic {}: {}", topicName, update);
        kafkaTemplate.send(topicName, taskId, update);
    }
}