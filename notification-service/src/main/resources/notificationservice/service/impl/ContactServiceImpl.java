package org.service.notificationservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.service.notificationservice.dto.*;
import org.service.notificationservice.entity.Email;
import org.service.notificationservice.entity.Reply;
import org.service.notificationservice.mapper.EmailMapper;
import org.service.notificationservice.repository.EmailRepository;
import org.service.notificationservice.service.ContactService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactServiceImpl implements ContactService {

    private final EmailRepository emailRepository;

    private final EmailMapper emailMapper;

    private final JavaMailSender javaMailSender;

    @Value("${email.from}")
    private String emailFrom;

    @Override
    public EmailResponse saveContactMessage(ContactRequest contactRequest) {
        log.info("Start - Saving contact message from user: {}", contactRequest.userId());

        Email newEmail = new Email();
        newEmail.setEmail(contactRequest.email());
        newEmail.setSubject(contactRequest.subject());
        newEmail.setBody(contactRequest.emailBody());
        newEmail.setTimestamp(LocalDateTime.now());
        newEmail.setStatus(Status.OPEN);
        newEmail.setUserId(contactRequest.userId());
        newEmail.setLastReplyFrom(LastReplyFrom.USER);
        newEmail.setLastReplyAt(LocalDateTime.now());

        log.info("Successfully saved contact message for user: {}", contactRequest.userId());

       return emailMapper.mapToResponse(emailRepository.save(newEmail));
    }

    public List<EmailResponse> getEmails(String userId) {
        log.info("Start - Retrieving emails for userId: {}", userId);

        List<EmailResponse> emails;
        if (userId != null && !userId.isEmpty()) {
            emails = emailMapper.map(emailRepository.findAllByUserId(userId));
        } else {
            emails = emailMapper.map(emailRepository.findAll());
        }

        log.info("Successfully retrieved {} emails for userId: {}", emails.size(), userId);
        return emails;
    }

    public EmailResponseWithReplies getEmail(Long id) {
        log.info("Start - Retrieving email with ID: {}", id);

        Email email = emailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Email not found"));

        log.info("Successfully retrieved email with ID: {}", id);
        return emailMapper.map(email);
    }

    @Override
    public void reply(Long emailId, ContactRequest contactRequest) {
        log.info("Start - Replying to email with ID: {}", emailId);

        Email email = emailRepository.findById(emailId).orElseThrow(
                () -> new RuntimeException("Email not found")
        );

        Reply reply = new Reply();
        reply.setBody(contactRequest.emailBody());
        reply.setTimestamp(LocalDateTime.now());
        reply.setFromEmail(contactRequest.email());

        if (email.getEmail().equals(contactRequest.email())) {
            reply.setEmailStatus(EmailStatus.CUSTOMER_REPLIED);
        } else {
            reply.setEmailStatus(EmailStatus.ADMIN_REPLIED);
        }

        reply.setEmail(email);
        email.getReplies().add(reply);
        email.setLastReplyFrom(contactRequest.isAdmin() ? LastReplyFrom.ADMIN : LastReplyFrom.USER);
        email.setLastReplyAt(LocalDateTime.now());

        emailRepository.save(email);

        log.info("Successfully replied to email with ID: {}", emailId);

        //sendEmail(reply.getSubject(), reply.getBody(), emailFrom, reply.getFromEmail());
    }

    @Override
    public EmailResponse update(Long id, EmailUpdateRequest emailUpdateRequest) {
        log.info("Start - Updating email with ID: {}", id);

        Email email = emailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Email not found"));

        email.setStatus(emailUpdateRequest.status());

        EmailResponse emailResponse = emailMapper.mapToResponse(emailRepository.save(email));

        log.info("Successfully updated email status with ID: {}", id);
        return emailResponse;
    }

    @Override
    public void delete(Long id) {
        log.info("Start - Deleting email with ID: {}", id);

        emailRepository.deleteById(id);

        log.info("Successfully deleted email with ID: {}", id);
    }

    private void sendEmail(String subject, String body, String emailFrom, String emailTo) {
        log.info("Start - Sending email to: {}", emailTo);

        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom(emailFrom);
            messageHelper.setTo(emailTo);
            messageHelper.setSubject(subject);
            messageHelper.setText(body);
        };

        try {
            javaMailSender.send(messagePreparator);
            log.info("Contact email to {} was sent successfully", emailTo);
        } catch (MailException e) {
            log.error("Exception occurred when sending mail to {}: {}", emailTo, e.getMessage(), e);
            throw new RuntimeException("Exception occurred when sending mail", e);
        }
    }
}
