package com.diogobaptista.order_manager_api.service;

import com.diogobaptista.order_manager_api.entity.Order;
import com.diogobaptista.order_manager_api.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;
    private final FileLogService fileLogService;

    public EmailService(JavaMailSender mailSender, FileLogService fileLogService) {
        this.mailSender = mailSender;
        this.fileLogService = fileLogService;
    }

    private void logAndWrite(String message, boolean isError) {
        if (isError) {
            log.error(message);
        } else {
            log.info(message);
        }
        fileLogService.appendLine(message);
    }
    public void sendOrderCompleted(User user, Order order) {
        try {
            SimpleMailMessage message = getSimpleMailMessage(user, order);

            mailSender.send(message);

            log.info("Email sent to {} for order {}", user.getEmail(), order.getId());
            logAndWrite(String.format("Email sent to %s for order %d", user.getEmail(), order.getId()), false);
        } catch (Exception e) {
            logAndWrite(String.format("FAILED to send email to %s for order %d: %s",
                    user.getEmail(), order.getId(), e.getMessage()), true);        }
    }

    private static SimpleMailMessage getSimpleMailMessage(User user, Order order) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Order Completed: #" + order.getId());
        message.setText("Hello " + user.getName() + ",\n\n" +
                "Your order #" + order.getId() + " has been completed.\n" +
                "Quantity: " + order.getQuantity() + "\n" +
                "Fulfilled: " + order.getFulfilledQuantity() + "\n\n" +
                "Thank you for using our service!");
        return message;
    }
}
