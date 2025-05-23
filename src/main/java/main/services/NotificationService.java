package main.services;

// NotificationService.java
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String from;

    @Value("${notification.email.to}")
    private String recipientEmail;

    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }



    public void sendNotificationEmail(Long taskId, String newStatus) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipientEmail);
        message.setSubject("Статус задачи обновлен");
        message.setText(String.format("Статус задачи с ID %d был обновлен на: %s", taskId, newStatus));
        message.setFrom(from);

        mailSender.send(message);
        System.out.println("Уведомление по электронной почте отправлено для задачи ID: " + taskId + ", Статус: " + newStatus);
    }
}