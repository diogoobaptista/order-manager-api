package com.diogobaptista.order_manager_api;

import com.diogobaptista.order_manager_api.entity.Order;
import com.diogobaptista.order_manager_api.entity.User;
import com.diogobaptista.order_manager_api.service.EmailService;
import com.diogobaptista.order_manager_api.service.FileLogService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.Mockito.*;

public class EmailServiceTest {

    private JavaMailSender mailSender;
    private EmailService emailService;

    @BeforeEach
    public void setup() {
        mailSender = mock(JavaMailSender.class);
        FileLogService fileLogService = mock(FileLogService.class);
        emailService = new EmailService(mailSender, fileLogService);
    }

    @Test
    public void sendOrderCompleted_sendsEmail() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setName("Alice");

        Order order = new Order();
        order.setId(100L);
        order.setQuantity(5);
        order.setFulfilledQuantity(5);

        emailService.sendOrderCompleted(user, order);

        verify(mailSender).send(argThat((SimpleMailMessage message) ->
                {
                    Assertions.assertNotNull(message.getTo());
                    if (!message.getTo()[0].equals("test@example.com") ||
                            !message.getSubject().equals("Order Completed: #100")) return false;
                    Assertions.assertNotNull(message.getText());
                    return message.getText().contains("Hello Alice") &&
                            message.getText().contains("Quantity: 5") &&
                            message.getText().contains("Fulfilled: 5");
                }
        ));
    }

    @Test
    public void sendOrderCompleted_mailSenderThrows_exceptionIsHandled() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setName("Alice");

        Order order = new Order();
        order.setId(101L);
        order.setQuantity(3);
        order.setFulfilledQuantity(3);

        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));

        emailService.sendOrderCompleted(user, order);

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}
