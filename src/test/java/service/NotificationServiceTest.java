package service;


import main.services.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Модульные тесты для NotificationService")
class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private JavaMailSender mailSender;

    private final String TEST_FROM_EMAIL = "test@example.com";
    private final String TEST_RECIPIENT_EMAIL = "recipient@example.com";


    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(notificationService, "from", TEST_FROM_EMAIL);
        ReflectionTestUtils.setField(notificationService, "recipientEmail", TEST_RECIPIENT_EMAIL);
    }

    @Test
    @DisplayName("Должен успешно отправить уведомление по электронной почте")
    void sendNotificationEmail_shouldSendEmailSuccessfully() {
        Long taskId = 1L;
        String newStatus = "DONE";

        notificationService.sendNotificationEmail(taskId, newStatus);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage capturedMessage = messageCaptor.getValue();

        assertEquals(TEST_RECIPIENT_EMAIL, capturedMessage.getTo()[0]);
        assertEquals("Статус задачи обновлен", capturedMessage.getSubject());
        assertEquals(String.format("Статус задачи с ID %d был обновлен на: %s", taskId, newStatus), capturedMessage.getText());
        assertEquals(TEST_FROM_EMAIL, capturedMessage.getFrom());
    }

    @Test
    @DisplayName("Тест: должен НЕ отправлять уведомление, если recipientEmail пустой (гипотетический сценарий)")
    void sendNotificationEmail_shouldNotSend_whenRecipientEmailIsEmpty() {
        Long taskId = 2L;
        String newStatus = "NEW";

        ReflectionTestUtils.setField(notificationService, "recipientEmail", "");

        notificationService.sendNotificationEmail(taskId, newStatus);

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }
}