package notificationservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.service.notificationservice.event.LimitExceedEvent;
import org.service.notificationservice.event.OrderEvent;
import org.service.notificationservice.event.PaymentEvent;
import org.service.notificationservice.service.NotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final JavaMailSender javaMailSender;

    @Value("${admin.user.email}")
    private String adminUserEmail;

    @Value("${email.from}")
    private String emailFrom;

    @Override
    public void handleOrderEvent(OrderEvent orderEvent) {
        log.info("Handling OrderEvent for order number: {}", orderEvent.getOrderNumber());

        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom(emailFrom);
            messageHelper.setTo(orderEvent.getEmail());
            messageHelper.setSubject(getOrderMailSubject(orderEvent.getStatus(), orderEvent.getOrderNumber()));
            messageHelper.setText(getOrderMailBody(orderEvent.getStatus(), orderEvent.getName(), orderEvent.getOrderNumber()));
        };
        try {
            javaMailSender.send(messagePreparator);
            log.info("Order Placed Notification email to {} was sent", orderEvent.getEmail());
        } catch (MailException e) {
            log.error("Exception occurred when sending mail", e);
            throw new RuntimeException("Exception occurred when sending mail", e);
        }
    }

    private String getOrderMailSubject(String status, String orderNumber) {
        log.info("Status is - " + status);
        String result = switch (status) {
            case "Placed" -> String.format("Order %s Received", orderNumber);
            case "Cancelled" -> String.format("Order %s Cancelled", orderNumber);
            case "Not Paid" -> String.format("Order %s Not Paid", orderNumber);
            default -> throw new RuntimeException("Unknown status: " + status);
        };

        log.info("Result is - " + status);

        return result;
    }

    private String getOrderMailBody(String status, String name, String orderNumber) {
        String bodyEnd = switch (status) {
            case "Placed" -> "was received and currently is being processed";
            case "Cancelled" -> "was cancelled";
            case "Not Paid" -> "is not paid";
            default -> throw new RuntimeException("Unknown status: " + status);
        };

        return String.format("""
                    Dear %s,
                    
                    Please be informed that order %s %s.
                    
                    Thank you.
                    
                    Best regards,
                    AppleShop
                    """, orderNumber, name, bodyEnd);
    }

    @Override
    public void handleLimitExceedEvent(LimitExceedEvent limitExceedEvent) {
        log.info("Handling LimitExceedEvent for SKU code: {}", limitExceedEvent.getSkuCode());

        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom(emailFrom);
            messageHelper.setTo(adminUserEmail);
            messageHelper.setSubject(String.format("Product %s limit exceed", limitExceedEvent.getSkuCode()));
            messageHelper.setText(String.format("""
                    Dear Admin,
                    
                    Please be informed that current quantity of product %s is less that limit %d.
                    
                    Thank you.
                    
                    Best regards,
                    AppleShop
                    """, limitExceedEvent.getSkuCode(), limitExceedEvent.getLimit()));
        };
        try {
            javaMailSender.send(messagePreparator);
            log.info("Limit Exceeds Notification email to {} was sent", limitExceedEvent.getEmail());
        } catch (MailException e) {
            log.error("Exception occurred when sending mail", e);
            throw new RuntimeException("Exception occurred when sending mail to springshop@email.com", e);
        }
    }

    @Override
    public void handlePaymentEvent(PaymentEvent paymentEvent) {
        log.info("Handling PaymentEvent for order number: {}", paymentEvent.orderNumber());

        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom(emailFrom);
            messageHelper.setTo(paymentEvent.email());
            messageHelper.setSubject(String.format("Payment for order %s was %s", paymentEvent.orderNumber(), paymentEvent.status().toLowerCase()));
            messageHelper.setText(String.format("""
                    Dear Customer,
                    
                    Payment for order %s status is %s
                    
                    Thank you.
                    
                    Best regards,
                    AppleShop
                    """, paymentEvent.orderNumber(), paymentEvent.status().toLowerCase()));
        };
        try {
            javaMailSender.send(messagePreparator);
            log.info("Payment Status Notification email to {} was sent", paymentEvent.email());
        } catch (MailException e) {
            log.error("Exception occurred when sending mail", e);
            throw new RuntimeException("Exception occurred when sending mail to springshop@email.com", e);
        }
    }

    private <T> T deserialize(String json, Class<T> targetType) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            T result = objectMapper.readValue(json, targetType);
            log.info("Deserialized message to {}: {}", targetType.getSimpleName(), result);
            return result;
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize JSON message: {}", json, e);
            throw new RuntimeException("Failed to deserialize JSON message", e);
        }
    }
}
