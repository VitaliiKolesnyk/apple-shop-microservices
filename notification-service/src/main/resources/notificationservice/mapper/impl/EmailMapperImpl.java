package org.service.notificationservice.mapper.impl;

import lombok.RequiredArgsConstructor;
import org.service.notificationservice.dto.EmailResponse;
import org.service.notificationservice.dto.EmailResponseWithReplies;
import org.service.notificationservice.entity.Email;
import org.service.notificationservice.mapper.EmailMapper;
import org.service.notificationservice.mapper.ReplyMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EmailMapperImpl implements EmailMapper {

    private final ReplyMapper replyMapper;

    @Override
    public EmailResponse mapToResponse(Email email) {
        return new EmailResponse(email.getEmailId(), email.getSubject(),
                email.getEmail(), email.getStatus(), email.getTimestamp(), email.getLastReplyFrom(), email.getLastReplyAt());
    }

    @Override
    public List<EmailResponse> map(List<Email> emails) {
        return emails.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public EmailResponseWithReplies map(Email email) {
        return new EmailResponseWithReplies(email.getEmailId(), email.getSubject(), email.getBody(),
                email.getEmail(), email.getStatus(), email.getTimestamp(), replyMapper.map(email.getReplies()));
    }
}
