package main.kafka;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.dto.TaskStatusUpdate;
import main.services.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskStatusConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "${kafka.topic.task-status-updates}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeTaskStatusUpdate(TaskStatusUpdate update) {
        log.info("Received task status update: {}", update);
        notificationService.sendNotificationEmail(update.getId(), update.getNewStatus());
    }
}